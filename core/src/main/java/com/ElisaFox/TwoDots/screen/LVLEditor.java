package com.ElisaFox.TwoDots.screen;

import com.ElisaFox.TwoDots.TwoDots;
import com.ElisaFox.TwoDots.objects.ColorType;
import com.ElisaFox.TwoDots.objects.LevelData;
import com.ElisaFox.TwoDots.objects.LevelGoals;
import com.ElisaFox.TwoDots.objects.LevelSerializer;
import com.ElisaFox.TwoDots.ui.ButtonStyleFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LVLEditor implements Screen {

    private final TwoDots game;
    private Stage stage;

    private static final int GRID_SIZE = 6;
    private boolean[][] grid = new boolean[GRID_SIZE][GRID_SIZE];
    private Image[][] cellImages = new Image[GRID_SIZE][GRID_SIZE];
    private Skin skin;
    private LevelGoals goals = new LevelGoals();
    private final ObjectMap<ColorType, TextureRegionDrawable> colorIcons = new com.badlogic.gdx.utils.ObjectMap<>();

    private Texture passableView;
    private Texture blockView;
    private Texture bgTexture;
    private final LevelSerializer levelSerializer = new LevelSerializer();

    public LVLEditor(TwoDots game) {
        this.game = game;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = true;
            }
        }

        passableView = createRectTexture(new Color(0.2f, 0.5f, 0.2f, 1f));
        blockView = createRectTexture(new Color(0.4f, 0.2f, 0.2f, 1f));
        bgTexture = createRectTexture(new Color(0.1f, 0.1f, 0.15f, 1f));
        for (ColorType ct : ColorType.values()) {
            colorIcons.put(ct, new TextureRegionDrawable(new TextureRegion(createRectTexture(getColor(ct)))));
        }
        init();
    }

    private void init() {
        stage = new Stage(new ScreenViewport());
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

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        TextButton closeBtn = new TextButton("X", skin);
        closeBtn.getLabel().setFontScale(0.7f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { game.setScreen(game.menu); }
        });

        Table topBar = new Table();
        topBar.add(closeBtn).size(50, 50).expandX().left().pad(30);

        Table gridTable = new Table();
        gridTable.setBackground(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        gridTable.pad(10);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                final int r = row;
                final int c = col;

                Image cell = new Image(new TextureRegionDrawable(new TextureRegion(passableView)));
                cellImages[r][c] = cell;

                cell.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        toggleCell(r, c);
                    }
                });
                gridTable.add(cell).size(60, 60).pad(2);
            }
            gridTable.row();
        }

        TextButton extra = new TextButton("EXTRA", skin);
        TextButton save = new TextButton("SAVE", skin);

        extra.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showGoalsDialog();
            }
        });

        save.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                saveCurrentLevel();
            }

        });

        extra.getLabel().setFontScale(1f);
        save.getLabel().setFontScale(1f);

        Table bottomBar = new Table();
        bottomBar.add(extra).size(250, 60).padRight(15);
        bottomBar.add(save).size(250, 60).padLeft(15);

        root.add(topBar).expandX().fillX();
        root.row();
        root.add(gridTable).expand().center();
        root.row();
        root.add(bottomBar).expandX().fillX().padBottom(40);
    }

    private void toggleCell(int r, int c) {
        grid[r][c] = !grid[r][c];
        Texture targetTexture = grid[r][c] ? passableView : blockView;
        cellImages[r][c].setDrawable(new TextureRegionDrawable(new TextureRegion(targetTexture)));
    }

    private Texture createRectTexture(Color color) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        ScreenUtils.clear(0.07f, 0.09f, 0.13f, 1f);
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        passableView.dispose();
        blockView.dispose();
        bgTexture.dispose();
        if (skin != null) skin.dispose();
        for (TextureRegionDrawable dr : colorIcons.values()) {
            dr.getRegion().getTexture().dispose();
        }
    }

    private void showGoalsDialog() {
        Dialog dialog = new Dialog("", skin);
        dialog.getContentTable().clear();
        dialog.getContentTable().pad(20);

        Table topTable = new Table();
        Label titleLabel = new Label("LEVEL SETTINGS", skin);
        titleLabel.setFontScale(0.8f);

        topTable.add(titleLabel).colspan(4).padBottom(20).center();
        topTable.row();

        Label movesTitle = new Label("MOVES:", skin);
        movesTitle.setFontScale(0.6f);

        TextButton movesMinus = new TextButton("-", skin);
        TextButton movesPlus = new TextButton("+", skin);
        Label movesCount = new Label(String.valueOf(goals.getMovesLeft()), skin);
        movesCount.setFontScale(0.8f);
        movesMinus.getLabel().setFontScale(0.7f);
        movesMinus.getLabelCell().padBottom(6f);
        movesPlus.getLabel().setFontScale(0.7f);


        movesMinus.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (goals.getMovesLeft() > 1) updateMoves(movesCount, goals.getMovesLeft() - 1);
            }
        });
        movesPlus.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                updateMoves(movesCount, goals.getMovesLeft() + 1);
            }
        });

        topTable.add(movesTitle).left().padRight(10);
        topTable.add(movesMinus).size(50);
        topTable.add(movesCount).width(80).center();
        topTable.add(movesPlus).size(50);

        dialog.getContentTable().add(topTable).row();

        Table goalsTable = new Table();
        goalsTable.padTop(20);
        int count = 0;

        for (final ColorType color : ColorType.values()) {
            Image icon = new Image(new TextureRegionDrawable(new TextureRegion(createRectTexture(getColor(color)))));
            TextButton btnM = new TextButton("-", skin);
            TextButton btnP = new TextButton("+", skin);
            final Label valLabel = new Label(String.valueOf(goals.getTarget(color)), skin);

            valLabel.setFontScale(0.5f);
            btnM.getLabel().setFontScale(0.7f);
            btnM.getLabelCell().padBottom(6f);
            btnP.getLabel().setFontScale(0.7f);

            btnM.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    int val = Math.max(0, goals.getTarget(color) - 1);
                    goals.setTarget(color, val);
                    valLabel.setText(String.valueOf(val));
                }
            });
            btnP.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    int val = goals.getTarget(color) + 1;
                    goals.setTarget(color, val);
                    valLabel.setText(String.valueOf(val));
                }
            });

            goalsTable.add(icon).size(30).padRight(5);
            goalsTable.add(btnM).size(45);
            goalsTable.add(valLabel).width(50).center();
            goalsTable.add(btnP).size(45).padRight(20);

            if (++count % 2 == 0) goalsTable.row().padTop(10);
        }

        dialog.getContentTable().add(goalsTable).row();

        TextButton okBtn = new TextButton("OK", skin);
        okBtn.getLabel().setFontScale(0.7f);

        dialog.getButtonTable().add(okBtn).size(220, 60).pad(20);
        dialog.setObject(okBtn, true);

        dialog.show(stage);
    }

    private void updateMoves(Label label, int newVal) {
        goals.setMovesLeft(newVal);
        label.setText(String.valueOf(newVal));
    }

    private Color getColor(ColorType type) {
        switch (type) {
            case BLUE: return Color.BLUE;
            case RED: return Color.RED;
            case YELLOW: return Color.YELLOW;
            case GREEN: return Color.GREEN;
            case PURPLE: return Color.PURPLE;
            default: return Color.WHITE;
        }
    }

    private void saveCurrentLevel() {
        LevelData data = new LevelData(goals.getMovesLeft(), GRID_SIZE, GRID_SIZE);

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                data.grid[r][c] = grid[r][c] ? LevelData.CellType.NORMAL : LevelData.CellType.EMPTY;
            }
        }

        for (ColorType ct : ColorType.values()) {
            int targetValue = goals.getTarget(ct);
            data.targetGoals.put(ct, targetValue);
        }

        levelSerializer.saveLevel(data, "my_level.json");
    }
}
