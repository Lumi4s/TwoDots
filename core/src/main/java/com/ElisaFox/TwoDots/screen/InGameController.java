package com.ElisaFox.TwoDots.screen;

import com.ElisaFox.TwoDots.TwoDots;
import com.ElisaFox.TwoDots.objects.*;
import com.badlogic.gdx.utils.Array;

public class InGameController {

    private final Array<Dot> selectedDots = new Array<>();
    private final BoardSolver solver = new BoardSolver();
    private boolean isDragging = false;
    private boolean isSquared = false;
    private GameBoard gameBoard;
    private LevelGoals goals;
    private LevelData currentLevelData;
    private boolean autoPlay = false;
    private float autoTimer = 0f;
    private boolean botAnimating = false;
    private float botDrawTimer = 0f;
    private Array<Dot> botMove;
    private int botMoveIndex = 0;
    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void startLevel(LevelData data) {
        this.currentLevelData = data;
        this.goals = new LevelGoals(data.steps, data.targetGoals);
        this.gameBoard = new GameBoard(data);
        this.gameBoard.fillBoard();

        selectedDots.clear();
        isDragging = false;
        isSquared = false;
        botAnimating = false;
        autoTimer = 0f;

        if (listener != null) listener.onGoalsChanged();
    }

    public void touchDown(float worldX, float worldY) {
        isDragging = true;
        Dot hit = getDotAtWorld(worldX, worldY);
        if (hit != null) {
            selectedDots.add(hit);
        }
    }

    public void touchDragged(float worldX, float worldY) {
        Dot hit = getDotAtWorld(worldX, worldY);
        if (hit == null || selectedDots.size == 0) return;

        if (selectedDots.size > 1 && hit == selectedDots.get(selectedDots.size - 2)) {
            selectedDots.pop();
            isSquared = false;
            return;
        }

        if (isSquared) return;

        if (hit.getColor().equals(selectedDots.get(0).getColor()) && isNear(hit)) {
            if (selectedDots.size >= 3 && isSquareDots(hit)) {
                selectedDots.add(hit);
                isSquared = true;
                return;
            }
            if (!selectedDots.contains(hit, true)) {
                selectedDots.add(hit);
            }
        }
    }

    public void touchUp() {
        isDragging = false;
        processDots();
    }

    public void updateAuto(float delta) {
        if (!autoPlay || goals == null) return;
        if (goals.isWin() || goals.isLose()) return;

        autoTimer += delta;

        if (botAnimating) {
            botDrawTimer += delta;
            if (botDrawTimer >= 0.5f) {
                botDrawTimer = 0f;
                if (botMoveIndex < botMove.size) {
                    selectedDots.add(botMove.get(botMoveIndex));
                    botMoveIndex++;
                } else {
                    botAnimating = false;
                    if (solver.wasLastMoveSquare()) {
                        isSquared = true;
                    }
                    processDots();
                }
            }
        } else {
            if (autoTimer >= 1f) {
                autoTimer = 0f;
                Array<Dot> move = solver.findBestMove(gameBoard, goals);
                if (move != null && move.size >= 2) {
                    selectedDots.clear();
                    isSquared = false;
                    botMove = new Array<>();
                    botMove.addAll(move);
                    botMoveIndex = 0;
                    botAnimating = true;
                } else {
                    rerollField();
                }
            }
        }
    }

    public boolean rerollField() {
        if (currentLevelData == null || goals == null) return false;
        if (goals.getMovesLeft() <= 2) return false;

        selectedDots.clear();
        isDragging = false;
        isSquared = false;

        goals.useMove();
        goals.useMove();

        gameBoard = new GameBoard(currentLevelData);
        gameBoard.fillBoard();

        if (listener != null) listener.onGoalsChanged();
        return true;
    }

    void processDots() {
        if (selectedDots.size >= 2) {
            ColorType color = selectedDots.get(0).getColor();

            if (isSquared) {
                selectedDots.clear();
                for (Dot d : gameBoard.getActiveDots()) {
                    if (d.getColor().equals(color)) {
                        selectedDots.add(d);
                    }
                }
            }

            goals.increment(color, selectedDots.size);
            goals.useMove();
            gameBoard.updateLogic(selectedDots);

            if (listener != null) listener.onGoalsChanged();

            if (goals.isWin()) {
                if (listener != null) listener.onGameEnd(true);
            } else if (goals.isLose()) {
                if (listener != null) listener.onGameEnd(false);
            }
        }
        selectedDots.clear();
        isSquared = false;
    }

    public Dot getDotAtWorld(float worldX, float worldY) {
        int col = (int) Math.floor(worldX);
        int row = (int) Math.floor(worldY);

        if (col < 0 || col >= TwoDots.COLS || row < 0 || row >= TwoDots.ROWS) return null;

        Dot dot = gameBoard.getDotAt(row, col);
        if (dot == null) return null;

        float centerX = col + 0.5f;
        float centerY = row + 0.5f;
        float distance = (float) Math.sqrt(
            (worldX - centerX) * (worldX - centerX) + (worldY - centerY) * (worldY - centerY)
        );
        return distance < 0.3f ? dot : null;
    }

    boolean isNear(Dot dot) {
        Dot latest = selectedDots.peek();
        int rowDiff = Math.abs(dot.getTargetRow() - latest.getTargetRow());
        int colDiff = Math.abs(dot.getTargetCol() - latest.getTargetCol());
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1);
    }

    boolean isSquareDots(Dot hitDot) {
        if (hitDot != selectedDots.get(0)) return false;

        int minR = hitDot.getTargetRow(), maxR = hitDot.getTargetRow();
        int minC = hitDot.getTargetCol(), maxC = hitDot.getTargetCol();

        for (Dot d : selectedDots) {
            minR = Math.min(minR, d.getTargetRow());
            maxR = Math.max(maxR, d.getTargetRow());
            minC = Math.min(minC, d.getTargetCol());
            maxC = Math.max(maxC, d.getTargetCol());
        }

        int sideR = maxR - minR;
        int sideC = maxC - minC;
        if (sideR == sideC && sideR > 0) {
            return selectedDots.size == sideR * 4;
        }
        return false;
    }

    public Array<Dot> getSelectedDots() {
        return selectedDots;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public boolean isSquared() {
        return isSquared;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public LevelGoals getGoals() {
        return goals;
    }

    public LevelData getCurrentLevelData() {
        return currentLevelData;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public boolean isBotAnimating() {
        return botAnimating;
    }

    void setIsSquared(boolean v) {
        this.isSquared = v;
    }

    public interface Listener {
        void onGoalsChanged();

        void onGameEnd(boolean win);
    }
}
