package com.ElisaFox.TwoDots.screen;

import com.ElisaFox.TwoDots.TwoDots;
import com.ElisaFox.TwoDots.objects.ColorType;
import com.ElisaFox.TwoDots.objects.LevelData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LVLEditor implements Screen {
    private final TwoDots game;
    private ExtendViewport viewport;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private Stage uiStage;
    private Skin skin;

    private LevelData currentLevel;
    private final int ROWS = 6;
    private final int COLS = 6;
    private boolean isSettingsOpen = false;

    public LVLEditor(TwoDots game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(TwoDots.WORLD_WIDTH, TwoDots.WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        uiStage = new Stage(new ScreenViewport());
        currentLevel = new LevelData(20, ROWS, COLS);
        initSkin();
    }

    private void initSkin() {
        skin = new Skin();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();
        skin.add("default-font", TwoDots.font);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = TwoDots.font;
        style.up = createRoundedButton(new Color(0.18f, 0.55f, 0.95f, 1f));
        style.over = createRoundedButton(new Color(0.28f, 0.65f, 1f, 1f));
        style.down = createRoundedButton(new Color(0.10f, 0.40f, 0.80f, 1f));
        style.fontColor = Color.WHITE;
        skin.add("default", style);

        Label.LabelStyle titleStyle = new Label.LabelStyle(TwoDots.font, Color.WHITE);
        skin.add("title", titleStyle);
    }

    private Drawable createRoundedButton(Color color) {
        int width = 260; int height = 180; int radius = 35;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); pixmap.fill();
        pixmap.setColor(color);
        pixmap.fillRectangle(radius, 0, width - radius * 2, height);
        pixmap.fillRectangle(0, radius, width, height - radius * 2);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius, radius, radius);
        pixmap.fillCircle(radius, height - radius, radius);
        pixmap.fillCircle(width - radius, height - radius, radius);
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);
        if (!isSettingsOpen) {
            handleGridInput();
            drawGrid();
        }
        uiStage.act(delta);
        uiStage.draw();
    }

    private void handleGridInput() {
        if (Gdx.input.isTouched()) {
            Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            // Кнопка Settings в углу экрана
            if (touch.x < 1.5f && touch.y > TwoDots.WORLD_HEIGHT - 1.5f) {
                openSettings();
                return;
            }

            int col = (int) Math.floor(touch.x);
            int row = (int) Math.floor(touch.y);

            if (col >= 0 && col < COLS && row >= 0 && row < ROWS) {
                if (Gdx.input.justTouched()) toggleCell(row, col);
            }
        }
    }

    private void toggleCell(int r, int c) {
        currentLevel.grid[r][c] = (currentLevel.grid[r][c] == LevelData.CellType.NORMAL)
            ? LevelData.CellType.EMPTY : LevelData.CellType.NORMAL;
    }

    private void drawGrid() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (currentLevel.grid[r][c] == LevelData.CellType.EMPTY) shapeRenderer.setColor(Color.BLACK);
                else shapeRenderer.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));
                shapeRenderer.rect(c + 0.05f, r + 0.05f, 0.9f, 0.9f);
            }
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GRAY);
        for (int i = 0; i <= ROWS; i++) shapeRenderer.line(0, i, COLS, i);
        for (int i = 0; i <= COLS; i++) shapeRenderer.line(i, 0, i, ROWS);
        shapeRenderer.end();
    }
    private void openSettings() {
        isSettingsOpen = true;
        initSettingsMenu();
    }

    private void initSettingsMenu() {
        uiStage.clear();

        Skin localSkin = new Skin();
        localSkin.add("default-font", TwoDots.font);

        TextField.TextFieldStyle tfStyle = new TextField.TextFieldStyle();
        tfStyle.font = TwoDots.font;
        tfStyle.fontColor = Color.WHITE;
        localSkin.add("default", tfStyle);

        Label.LabelStyle defaultLabelStyle = new Label.LabelStyle(TwoDots.font, Color.WHITE);
        localSkin.add("default", defaultLabelStyle);

        Label.LabelStyle titleStyle = new Label.LabelStyle(TwoDots.font, Color.WHITE);
        localSkin.add("title", titleStyle);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = TwoDots.font;
        btnStyle.up = createRoundedButton(new Color(0.18f, 0.55f, 0.95f, 1f));
        btnStyle.over = createRoundedButton(new Color(0.28f, 0.65f, 1f, 1f));
        btnStyle.down = createRoundedButton(new Color(0.10f, 0.40f, 0.80f, 1f));
        btnStyle.fontColor = Color.WHITE;
        localSkin.add("button", btnStyle);

        Table menuTable = new Table();
        menuTable.setFillParent(true);
        uiStage.addActor(menuTable);

        Pixmap bgPixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0.15f, 0.15f, 0.15f, 1f);
        bgPixmap.fill();
        Texture bgTexture = new Texture(bgPixmap);
        bgPixmap.dispose();

        TextureRegion whiteRegion = game.atlas.findRegion("white");
        if (whiteRegion == null) {
            whiteRegion = new TextureRegion(bgTexture);
        }

        Table panel = new Table();
        panel.setBackground(new TextureRegionDrawable(whiteRegion));
        panel.pad(30);

        Label title = new Label("SETTINGS", localSkin, "title");
        panel.add(title).padBottom(20).row();

        TextField stepsField = new TextField(String.valueOf(currentLevel.steps), localSkin);
        panel.add(new Label("Steps:", localSkin)).left().padRight(10);
        panel.add(stepsField).width(100).row();

        Label goalsTitle = new Label("GOALS", localSkin, "title");
        panel.add(goalsTitle).padTop(20).padBottom(10).row();

        Table goalsTable = new Table();
        goalsTable.add(new Label("No goals added", localSkin)).colspan(2);
        panel.add(goalsTable).row();

        TextButton addGoalBtn = new TextButton("+ Add Goal", localSkin, "button");
        addGoalBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentLevel.goals.put(ColorType.RED, currentLevel.goals.getOrDefault(ColorType.RED, 0) + 1);
                refreshGoalsTable(goalsTable, goalsTitle, localSkin); // Передаем skin для обновления
            }
        });

        TextButton saveBtn = new TextButton("SAVE", localSkin, "button");
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    currentLevel.steps = Integer.parseInt(stepsField.getText());
                } catch (Exception e) { /* ignore */ }
                isSettingsOpen = false;
                uiStage.clear();
            }
        });

        TextButton closeBtn = new TextButton("CLOSE", localSkin, "button");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isSettingsOpen = false;
                uiStage.clear();
            }
        });

        panel.add(addGoalBtn).pad(5).row();
        panel.add(saveBtn).pad(5).row();
        panel.add(closeBtn).padTop(10).row();

        menuTable.add(panel);
    }

    private void refreshGoalsTable(Table goalsTable, Label title, Skin skin) {
    goalsTable.clear();
    if (currentLevel.goals.isEmpty()) {
        goalsTable.add(new Label("No goals", skin)).colspan(2);
    } else {
        for (ColorType type : currentLevel.goals.keySet()) {
            goalsTable.add(new Label(type.name() + ": " + currentLevel.goals.get(type), skin)).colspan(2).pad(2);
        }
    }
}

    @Override public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        shapeRenderer.dispose();
        uiStage.dispose();
    }
}
