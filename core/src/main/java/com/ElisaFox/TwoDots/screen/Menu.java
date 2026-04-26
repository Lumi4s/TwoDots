package com.ElisaFox.TwoDots.screen;

import com.ElisaFox.TwoDots.TwoDots;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Menu implements Screen {

    private final TwoDots game;
    private Stage stage;

    public Menu(TwoDots game) {
        this.game = game;
        init();
    }

    private void init() {
        stage = new Stage(new ScreenViewport());

        Skin skin = new Skin();

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

        Label title = new Label("TWO DOTS", skin, "title");
        title.setFontScale(1.5f);

        TextButton play = new TextButton("PLAY", skin);
        TextButton edit = new TextButton("LEVEL EDITOR", skin);
        TextButton exit = new TextButton("EXIT", skin);

        play.getLabel().setFontScale(1.2f);
        edit.getLabel().setFontScale(1.2f);
        exit.getLabel().setFontScale(1.2f);

        play.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.inGame);
            }
        });

        edit.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.lvlEditor);
            }
        });

        exit.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.debug();

        table.padLeft(40);
        table.padRight(40);
        table.padTop(40);
        table.padBottom(40);

        table.center();

        table.add(title).padBottom(80).row();

        table.defaults()
            .width(260)
            .height(70)
            .pad(15);

        table.add(play).row();
        table.add(edit).row();
        table.add(exit).row();

        stage.addActor(table);
    }

    private Drawable createRoundedButton(Color color) {

        int width = 260;
        int height = 180;
        int radius = 35;

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

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
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.07f, 0.09f, 0.13f, 1f);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
