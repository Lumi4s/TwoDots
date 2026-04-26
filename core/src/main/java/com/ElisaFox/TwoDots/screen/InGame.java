package com.ElisaFox.TwoDots.screen;

import com.ElisaFox.TwoDots.TwoDots;
import com.ElisaFox.TwoDots.objects.ColorType;
import com.ElisaFox.TwoDots.objects.Dot;
import com.ElisaFox.TwoDots.objects.GameBoard;
import com.ElisaFox.TwoDots.objects.LevelGoals;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class InGame implements Screen {
    private final TwoDots game;
    private ExtendViewport viewport;
    private OrthographicCamera camera;
    private final InputAdapter input;

    private Stage GameStage;
    private Stage uiStage;

    private GameBoard gameBoard;
    private final int rows = 6;
    private final int cols = 6;
    private Sprite backgroundSprite;
    private Sprite dotSprite;
    private Array<Dot> selectedDots;
    private float gridX;
    private float gridY;
    private Vector2 currentTouch;
    private ShapeRenderer shapeRenderer;
    private boolean isDragging;
    private boolean isSquared;
    private LevelGoals goals;

    private static final Color DOT_BLUE = new Color(0.337f, 0.706f, 0.914f, 1f);
    private static final Color DOT_RED = new Color(0.902f, 0.298f, 0.235f, 1f);
    private static final Color DOT_YELLOW = new Color(0.945f, 0.769f, 0.059f, 1f);
    private static final Color DOT_GREEN = new Color(0.18f, 0.8f, 0.443f, 1f);
    private static final Color DOT_PURPLE = new Color(0.608f, 0.349f, 0.714f, 1f);
    private static final Color DOT_DARK = new Color(0.173f, 0.243f, 0.314f, 1f);


    public InGame(TwoDots game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(TwoDots.WORLD_WIDTH, TwoDots.WORLD_HEIGHT, camera);
        gameBoard = new GameBoard(rows, cols);
        shapeRenderer = new ShapeRenderer();

        backgroundSprite = game.atlas.createSprite("square");
        dotSprite = game.atlas.createSprite("dot");
        selectedDots = new Array<>();
        currentTouch = new Vector2();
        input = new InputAdapter() {
            @Override
            public boolean touchDown(int x, int y, int pointer, int button) {
                isDragging = true;
                Vector2 touch = new Vector2(x, y);
                viewport.unproject(touch);
                currentTouch = touch;

                Dot hitDot = getDot(x, y);
                if (hitDot != null) {
                    selectedDots.add(hitDot);
                }
                return true;
            }

            @Override
            public boolean touchDragged(int x, int y, int pointer) {
                Vector2 touch = new Vector2(x, y);
                viewport.unproject(touch);
                currentTouch = touch;
                Dot hitDot = getDot(x, y);

                if (hitDot == null || selectedDots.size == 0) return true;

                if (selectedDots.size > 1 && hitDot == selectedDots.get(selectedDots.size - 2)) {
                    selectedDots.pop();
                    isSquared = false;
                    return true;
                }

                if (isSquared) return true;

                if (hitDot.getColor().equals(selectedDots.get(0).getColor()) && isNear(hitDot)) {
                    if (selectedDots.size >= 3 && isSquareDots(hitDot)) {
                        selectedDots.add(hitDot);
                        isSquared = true;
                        return true;
                    }

                    if (!selectedDots.contains(hitDot, true)) {
                        selectedDots.add(hitDot);
                    }
                }

                return true;
            }

            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {
                isDragging = false;

                processDots();
                return true;
            }
        };
    }

    private boolean isNear(Dot dot) {
        Dot latestDot = selectedDots.peek();
        int hitRow = dot.getTargetRow();
        int hitCol = dot.getTargetCol();
        int latestRow = latestDot.getTargetRow();
        int latestCol = latestDot.getTargetCol();
        int rowRange = Math.abs(hitRow - latestRow);
        int colRange = Math.abs(hitCol - latestCol);
        return ((rowRange == 1 && colRange == 0) || (rowRange == 0 && colRange == 1));
    }

    private boolean isSquareDots(Dot hitDot) {
        if (hitDot != selectedDots.get(0)) return false;

        int minR = hitDot.getTargetRow();
        int maxR = hitDot.getTargetRow();
        int minC = hitDot.getTargetCol();
        int maxC = hitDot.getTargetCol();

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

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
        goals = new LevelGoals(20);
        goals.addGoal(ColorType.RED, 15);
        goals.addGoal(ColorType.BLUE, 15);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(1, 1, 1, 1);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float centerX = worldWidth / 2f;
        float centerY = worldHeight / 2f;

        game.batch.begin();

        game.batch.draw(backgroundSprite, 0, 0, worldWidth, worldHeight);

        float topY = worldHeight - 0.5f;
        drawTopUI(centerX, topY);

        gridX = centerX - 3f;
        gridY = centerY - 3f;
        drawGrid(game.batch, gridX, gridY);

        float bottomY = 0.5f;
        drawBottomButtons(centerX, bottomY);

        if (goals.isWin()) {
            game.font.getData().setScale(0.045f);
            game.font.setColor(Color.GREEN);
            game.font.draw(game.batch, "VICTORY!", centerX - 1f, centerY);
        } else if (goals.isLose()) {
            game.font.getData().setScale(0.045f);
            game.font.setColor(Color.RED);
            game.font.draw(game.batch, "GAME OVER", centerX - 1.5f, centerY);
        }
        game.batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (selectedDots.size > 0) {
            shapeRenderer.setColor(convertColor(selectedDots.first().getColor()));

            float lineWidth = 0.12f;

            for (int i = 0; i < selectedDots.size - 1; i++) {
                Dot start = selectedDots.get(i);
                Dot end = selectedDots.get(i + 1);

                drawLine(start.getX() + gridX, start.getY() + gridY, end.getX() + gridX, end.getY() + gridY, lineWidth);
            }

            if (isDragging) {
                Dot lastDot = selectedDots.peek();
                drawLine(lastDot.getX() + gridX, lastDot.getY() + gridY, currentTouch.x, currentTouch.y, lineWidth);

            }
        }
        shapeRenderer.end();
    }

    private void drawLine(float x1, float y1, float x2, float y2, float width) {
        shapeRenderer.rectLine(x1, y1, x2, y2, width);
        shapeRenderer.circle(x1, y1, width / 2f, 20);
        shapeRenderer.circle(x2, y2, width / 2f, 20);
    }

    private void processDots() {
        if (selectedDots.size >= 2) {
            ColorType color = selectedDots.get(0).getColor();
            int amount = 0;
            if (isSquared) {
                selectedDots.clear();
                for (Dot d : gameBoard.getActiveDots()) {
                    if (d.getColor().equals(color)) {
                        selectedDots.add(d);
                    }
                }
            }

            amount = selectedDots.size;
            goals.increment(color, amount);
            goals.useMove();

            gameBoard.updateLogic(selectedDots);

            if (goals.isWin()) {
                // логика победы
            } else if (goals.isLose()) {
                // логика поражения
            }
        }
        selectedDots.clear();
        isSquared = false;
    }

    private Dot getDot(int screenX, int screenY) {
        Vector2 touch = new Vector2(screenX, screenY);
        viewport.unproject(touch);
        float x = touch.x - gridX;
        float y = touch.y - gridY;

        int row = (int) Math.floor(y);
        int col = (int) Math.floor(x);

        if (col >= 0 && col < 6 && row >= 0 && row < 6) {
            Dot dot = gameBoard.getDotAt(row, col);
            if (dot == null) return null;
            float centerX = col + 0.5f;
            float centerY = row + 0.5f;

            float distance = Vector2.dst(x, y, centerX, centerY);
            if (distance < 0.3f) {
                return dot;
            }
        }
        return null;
    }

    private void drawGrid(Batch batch, float gridX, float gridY) {
        for (Dot dot : gameBoard.getActiveDots()) {
            dot.update(Gdx.graphics.getDeltaTime());

            batch.setColor(convertColor(dot.getColor()));

            float dotSize = 0.5f;
            batch.draw(dotSprite, gridX + dot.getX() - dotSize / 2f, gridY + dot.getY() - dotSize / 2f, dotSize, dotSize);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawTopUI(float centerX, float topY) {
        game.font.setColor(Color.BLACK);
        game.font.draw(game.batch, "MOVES: " + goals.getMovesLeft(), centerX - 2.8f, topY);

        float offset = 0;
        for (ColorType color : goals.getGoalColors()) {
            game.batch.setColor(convertColor(color));
            float iconSize = 0.35f;

            game.batch.draw(dotSprite, centerX + offset, topY - 0.45f, iconSize, iconSize);

            game.batch.setColor(Color.WHITE);

            String progress = goals.getCollected(color) + "/" + goals.getTarget(color);
            game.font.draw(game.batch, progress, centerX + offset + 0.5f, topY - 0.15f);

            offset += 1.8f;
        }
    }

    private void drawBottomButtons(float centerX, float bottomY) {

    }

    private Color convertColor(ColorType color) {
        switch (color) {
            case RED:
                return DOT_RED;
            case BLUE:
                return DOT_BLUE;
            case GREEN:
                return DOT_GREEN;
            case YELLOW:
                return DOT_YELLOW;
            case PURPLE:
                return DOT_PURPLE;
            default:
                return DOT_DARK;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}

