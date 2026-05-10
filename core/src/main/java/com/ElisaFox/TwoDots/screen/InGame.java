package com.ElisaFox.TwoDots.screen;

import com.ElisaFox.TwoDots.TwoDots;
import com.ElisaFox.TwoDots.objects.*;
import com.ElisaFox.TwoDots.ui.ButtonStyleFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class InGame implements Screen {
    private final TwoDots game;
    private ExtendViewport viewport;
    private OrthographicCamera camera;
    private final InputAdapter input;

    private Stage uiStage;
    private TextButton closeBtn;
    private TextButton rerollBtn;

    private Array<Dot> botMove;
    private int botMoveIndex = 0;

    private boolean botAnimating = false;
    private float botDrawTimer = 0f;

    private Table root;
    private Table goalsTable;

    private GameBoard gameBoard;
    private Sprite backgroundSprite;
    private Sprite dotSprite;
    private Array<Dot> selectedDots;
    private float gridX;
    private float gridY;
    private Vector2 currentTouch;
    private ShapeRenderer shapeRenderer;
    private boolean isDragging;
    private boolean isSquared;
    private LevelData currentLevelData;
    private LevelGoals goals;
    private Skin skin;
    private Texture bgTexture;

    private TextButton autoBtn;

    private boolean autoPlay = false;
    private float autoTimer = 0f;

    private BoardSolver solver;

    private static final Color DOT_BLUE = new Color(0.337f, 0.706f, 0.914f, 1f);
    private static final Color DOT_RED = new Color(0.902f, 0.298f, 0.235f, 1f);
    private static final Color DOT_YELLOW = new Color(0.945f, 0.769f, 0.059f, 1f);
    private static final Color DOT_GREEN = new Color(0.18f, 0.8f, 0.443f, 1f);
    private static final Color DOT_PURPLE = new Color(0.608f, 0.349f, 0.714f, 1f);
    private static final Color DOT_DARK = new Color(0.173f, 0.243f, 0.314f, 1f);
    private static final Color CELL_EMPTY_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.5f);
    private static final Color CELL_NORMAL_COLOR = new Color(0.9f, 0.9f, 0.9f, 0.1f);

    public InGame(TwoDots game) {
        this.game = game;

        solver = new BoardSolver();

        uiStage = new Stage(new com.badlogic.gdx.utils.viewport.ScreenViewport());
        bgTexture = createRectTexture(new Color(0.1f, 0.1f, 0.15f, 1f));

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(TwoDots.WORLD_WIDTH, TwoDots.WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        backgroundSprite = game.atlas.createSprite("square");

        root = new Table();
        root.setFillParent(true);
        uiStage.addActor(root);

        this.skin = ButtonStyleFactory.createBaseSkin(TwoDots.font);

        if (!skin.has("default", Window.WindowStyle.class)) {
            Window.WindowStyle windowStyle = new Window.WindowStyle();
            windowStyle.background = new TextureRegionDrawable(new TextureRegion(bgTexture));
            windowStyle.titleFont = TwoDots.font;
            windowStyle.titleFontColor = Color.WHITE;

            skin.add("default", windowStyle);
        }

        if (!skin.has("default", Label.LabelStyle.class)) {
            Label.LabelStyle labelStyle = new Label.LabelStyle(TwoDots.font, Color.WHITE);
            skin.add("default", labelStyle);
        }

        closeBtn = new TextButton("X", skin);
        closeBtn.getLabel().setFontScale(0.7f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.menu);
            }
        });

        goalsTable = new Table();
        goalsTable.pad(8f);

        root.top().left();
        root.add(closeBtn).size(60, 60).pad(20);
        root.add().expandX();
        root.add(goalsTable).top().padTop(18f);
        root.add().expandX();

        root.row();
        root.add().expandY();
        root.row();

        Table bottomPanel = new Table();
        rerollBtn = new TextButton("REROLL (-2)", skin);
        rerollBtn.getLabel().setFontScale(0.65f);
        rerollBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                rerollField();
            }
        });
        autoBtn = new TextButton("AUTO: OFF", skin);
        autoBtn.getLabel().setFontScale(0.65f);

        autoBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                autoPlay = !autoPlay;
                autoBtn.setText(autoPlay
                    ? "AUTO: ON"
                    : "AUTO: OFF");
            }
        });

        bottomPanel.add(rerollBtn)
            .size(270, 64)
            .padRight(15f)
            .padBottom(20f);

        bottomPanel.add(autoBtn)
            .size(270, 64)
            .padLeft(15f)
            .padBottom(20f);
        root.add(bottomPanel).colspan(4).bottom();

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
        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(input);
        Gdx.input.setInputProcessor(multiplexer);

        LevelSerializer serializer = new LevelSerializer();
        LevelData loadedData = serializer.loadLevel("my_level.json");

        if (loadedData != null) {
            startLevel(loadedData);
        } else {
            LevelData defaultData = new LevelData(20, TwoDots.ROWS, TwoDots.COLS);
            defaultData.targetGoals.put(ColorType.RED, 5);
            defaultData.targetGoals.put(ColorType.BLUE, 5);
            startLevel(defaultData);
        }
    }

    private void startLevel(LevelData data) {
        this.currentLevelData = data;

        this.goals = new LevelGoals(data.steps, data.targetGoals);

        this.gameBoard = new GameBoard(data);
        this.gameBoard.fillBoard();

        rebuildGoalsHud();
    }

    private void rebuildGoalsHud() {
        if (goalsTable == null || goals == null) return;

        goalsTable.clear();

        Table hud = new Table();

        Label movesLabel = new Label("MOVES: " + goals.getMovesLeft(), skin);
        movesLabel.setColor(Color.BLACK);
        movesLabel.setFontScale(0.5f);
        hud.add(movesLabel).padRight(24f);

        int counter = 0;

        for (ColorType color : goals.getGoalColors()) {
            counter++;
            Table goalCell = new Table();
            Image icon = new Image(new TextureRegionDrawable(new TextureRegion(dotSprite)));
            icon.setColor(convertColor(color));

            Label progress = new Label(
                goals.getCollected(color) + "/" + goals.getTarget(color),
                skin
            );
            progress.setColor(Color.BLACK);
            progress.setFontScale(0.5f);

            goalCell.add(icon).size(20, 20).padRight(6f);
            goalCell.add(progress);

            hud.add(goalCell).padRight(18f);

            if (counter % 2 == 0) {
                hud.row();
                hud.add().width(0);
            }
        }
        goalsTable.add(hud);
    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(1, 1, 1, 1);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(backgroundSprite, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        float centerX = viewport.getWorldWidth() / 2f;
        float worldHeight = viewport.getWorldHeight();

        gridX = centerX - 3f;
        gridY = (worldHeight / 2f) - 3f;
        drawGrid(game.batch, gridX, gridY);
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

        uiStage.act(delta);
        uiStage.draw();
        if (autoPlay && goals != null) {
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

                    if (!goals.isWin() && !goals.isLose()) {
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
        }
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
            rebuildGoalsHud();

            gameBoard.updateLogic(selectedDots);

            if (goals.isWin()) {
                showEndDialog(true);
            } else if (goals.isLose()) {
                showEndDialog(false);
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

        if (col >= 0 && col < TwoDots.COLS && row >= 0 && row < TwoDots.ROWS) {
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
        for (int r = 0; r < currentLevelData.grid.length; r++) {
        for (int c = 0; c < currentLevelData.grid[0].length; c++) {
            float cellX = gridX + c;
            float cellY = gridY + r;
            float cellSize = 1.0f;

            if (currentLevelData.grid[r][c] == LevelData.CellType.EMPTY) {
                batch.setColor(CELL_EMPTY_COLOR);
                batch.draw(backgroundSprite, cellX, cellY, cellSize, cellSize);
            } else {
                batch.setColor(CELL_NORMAL_COLOR);
                batch.draw(backgroundSprite, cellX, cellY, cellSize, cellSize);
            }
        }
    }

        for (Dot dot : gameBoard.getActiveDots()) {
            dot.update(Gdx.graphics.getDeltaTime());

            batch.setColor(convertColor(dot.getColor()));

            float dotSize = 0.5f;
            batch.draw(dotSprite, gridX + dot.getX() - dotSize / 2f, gridY + dot.getY() - dotSize / 2f, dotSize, dotSize);
        }
        batch.setColor(Color.WHITE);
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

    private Texture createRectTexture(Color color) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void rerollField() {
        if (currentLevelData == null || goals == null) return;
        if (goals.getMovesLeft() <= 2) return;

        selectedDots.clear();
        isDragging = false;
        isSquared = false;

        goals.useMove();
        goals.useMove();

        gameBoard = new GameBoard(currentLevelData);
        gameBoard.fillBoard();

        rebuildGoalsHud();
    }

    private void showEndDialog(boolean win) {
        Dialog dialog = new Dialog("", skin);
        dialog.getContentTable().clear();
        dialog.getContentTable().pad(25);

        Label title = new Label(win ? "WIN" : "LOSE", skin);
        title.setFontScale(1.2f);
        title.setColor(win ? Color.GREEN : Color.RED);

        dialog.getContentTable().add(title).padBottom(30).center();
        dialog.getContentTable().row();

        TextButton retryBtn = new TextButton("RETRY", skin);
        retryBtn.getLabel().setFontScale(0.7f);

        TextButton exitBtn = new TextButton("EXIT", skin);
        exitBtn.getLabel().setFontScale(0.7f);

        retryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();

                if (currentLevelData != null) {
                    startLevel(currentLevelData);
                }
            }
        });

        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.remove();
                game.setScreen(game.menu);
            }
        });

        Table buttons = new Table();
        buttons.add(retryBtn).size(220, 60).padBottom(15);
        buttons.row();
        buttons.add(exitBtn).size(220, 60);

        dialog.getContentTable().add(buttons);

        dialog.show(uiStage);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
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

