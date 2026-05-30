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

    private static final Color DOT_BLUE = new Color(0.337f, 0.706f, 0.914f, 1f);
    private static final Color DOT_RED = new Color(0.902f, 0.298f, 0.235f, 1f);
    private static final Color DOT_YELLOW = new Color(0.945f, 0.769f, 0.059f, 1f);
    private static final Color DOT_GREEN = new Color(0.18f, 0.8f, 0.443f, 1f);
    private static final Color DOT_PURPLE = new Color(0.608f, 0.349f, 0.714f, 1f);
    private static final Color DOT_DARK = new Color(0.173f, 0.243f, 0.314f, 1f);
    private static final Color CELL_EMPTY_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.5f);
    private static final Color CELL_NORMAL_COLOR = new Color(0.9f, 0.9f, 0.9f, 0.1f);
    // ── логика (всё что тестируется) ─────────────────────────────────────────
    final InGameController controller = new InGameController();
    // ── rendering ────────────────────────────────────────────────────────────
    private final TwoDots game;
    // ── input ────────────────────────────────────────────────────────────────
    private final InputAdapter input;
    private final ExtendViewport viewport;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;
    private final Sprite backgroundSprite;
    private final Sprite dotSprite;
    private final Texture bgTexture;
    private float gridX;
    private float gridY;
    private final Vector2 currentTouch = new Vector2();
    // ── ui ───────────────────────────────────────────────────────────────────
    private final Stage uiStage;
    private final Skin skin;
    private final Table root;
    private final Table goalsTable;
    private final TextButton closeBtn;
    private final TextButton rerollBtn;
    private final TextButton autoBtn;

    public InGame(TwoDots game) {
        this.game = game;

        // controller слушает нас для обновления HUD и показа диалогов
        controller.setListener(new InGameController.Listener() {
            @Override
            public void onGoalsChanged() {
                rebuildGoalsHud();
            }

            @Override
            public void onGameEnd(boolean win) {
                showEndDialog(win);
            }
        });

        uiStage = new Stage(new com.badlogic.gdx.utils.viewport.ScreenViewport());
        bgTexture = createRectTexture(new Color(0.1f, 0.1f, 0.15f, 1f));

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(TwoDots.WORLD_WIDTH, TwoDots.WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        backgroundSprite = game.atlas.createSprite("square");
        dotSprite = game.atlas.createSprite("dot");

        skin = ButtonStyleFactory.createBaseSkin(TwoDots.font);

        if (!skin.has("default", Window.WindowStyle.class)) {
            Window.WindowStyle ws = new Window.WindowStyle();
            ws.background = new TextureRegionDrawable(new TextureRegion(bgTexture));
            ws.titleFont = TwoDots.font;
            ws.titleFontColor = Color.WHITE;
            skin.add("default", ws);
        }
        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(TwoDots.font, Color.WHITE));
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

        root = new Table();
        root.setFillParent(true);
        uiStage.addActor(root);

        root.top().left();
        root.add(closeBtn).size(60, 60).pad(20);
        root.add().expandX();
        root.add(goalsTable).top().padTop(18f);
        root.add().expandX();
        root.row();
        root.add().expandY();
        root.row();

        rerollBtn = new TextButton("REROLL (-2)", skin);
        rerollBtn.getLabel().setFontScale(0.65f);
        rerollBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.rerollField();
            }
        });

        autoBtn = new TextButton("AUTO: OFF", skin);
        autoBtn.getLabel().setFontScale(0.65f);
        autoBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.setAutoPlay(!controller.isAutoPlay());
                autoBtn.setText(controller.isAutoPlay() ? "AUTO: ON" : "AUTO: OFF");
            }
        });

        Table bottomPanel = new Table();
        bottomPanel.add(rerollBtn).size(270, 64).padRight(15f).padBottom(20f);
        bottomPanel.add(autoBtn).size(270, 64).padLeft(15f).padBottom(20f);
        root.add(bottomPanel).colspan(4).bottom();

        input = new InputAdapter() {
            @Override
            public boolean touchDown(int x, int y, int pointer, int button) {
                Vector2 touch = viewport.unproject(new Vector2(x, y));
                currentTouch.set(touch);
                controller.touchDown(touch.x - gridX, touch.y - gridY);
                return true;
            }

            @Override
            public boolean touchDragged(int x, int y, int pointer) {
                Vector2 touch = viewport.unproject(new Vector2(x, y));
                currentTouch.set(touch);
                controller.touchDragged(touch.x - gridX, touch.y - gridY);
                return true;
            }

            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {
                controller.touchUp();
                return true;
            }
        };
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
            controller.startLevel(loadedData);
        } else {
            LevelData defaultData = new LevelData(20, TwoDots.ROWS, TwoDots.COLS);
            defaultData.targetGoals.put(ColorType.RED, 5);
            defaultData.targetGoals.put(ColorType.BLUE, 5);
            controller.startLevel(defaultData);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.07f, 0.09f, 0.13f, 1f);

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
        Array<Dot> sel = controller.getSelectedDots();
        if (sel.size > 0) {
            shapeRenderer.setColor(convertColor(sel.first().getColor()));
            float lw = 0.12f;
            for (int i = 0; i < sel.size - 1; i++) {
                Dot s = sel.get(i), e = sel.get(i + 1);
                drawLine(s.getX() + gridX, s.getY() + gridY,
                    e.getX() + gridX, e.getY() + gridY, lw);
            }
            if (controller.isDragging()) {
                Dot last = sel.peek();
                drawLine(last.getX() + gridX, last.getY() + gridY,
                    currentTouch.x, currentTouch.y, lw);
            }
        }
        shapeRenderer.end();

        uiStage.act(delta);
        uiStage.draw();

        controller.updateAuto(delta);
    }

    private void drawLine(float x1, float y1, float x2, float y2, float width) {
        shapeRenderer.rectLine(x1, y1, x2, y2, width);
        shapeRenderer.circle(x1, y1, width / 2f, 20);
        shapeRenderer.circle(x2, y2, width / 2f, 20);
    }

    private void drawGrid(Batch batch, float gx, float gy) {
        LevelData ld = controller.getCurrentLevelData();
        for (int r = 0; r < ld.grid.length; r++) {
            for (int c = 0; c < ld.grid[0].length; c++) {
                if (ld.grid[r][c] == LevelData.CellType.EMPTY) {
                    batch.setColor(CELL_EMPTY_COLOR);
                } else {
                    batch.setColor(CELL_NORMAL_COLOR);
                }
                batch.draw(backgroundSprite, gx + c, gy + r, 1f, 1f);
            }
        }
        for (Dot dot : controller.getGameBoard().getActiveDots()) {
            dot.update(Gdx.graphics.getDeltaTime());
            batch.setColor(convertColor(dot.getColor()));
            float ds = 0.5f;
            batch.draw(dotSprite, gx + dot.getX() - ds / 2f, gy + dot.getY() - ds / 2f, ds, ds);
        }
        batch.setColor(Color.WHITE);
    }

    private void rebuildGoalsHud() {
        LevelGoals goals = controller.getGoals();
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
                goals.getCollected(color) + "/" + goals.getTarget(color), skin);
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
                LevelData ld = controller.getCurrentLevelData();
                if (ld != null) controller.startLevel(ld);
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

