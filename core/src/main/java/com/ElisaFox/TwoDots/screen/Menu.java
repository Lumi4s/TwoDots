package com.ElisaFox.TwoDots.screen;

import com.ElisaFox.TwoDots.TwoDots;
import com.ElisaFox.TwoDots.ui.ButtonStyleFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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

        Skin skin = ButtonStyleFactory.createBaseSkin(TwoDots.font);

        Label.LabelStyle titleStyle = new Label.LabelStyle(TwoDots.font, Color.WHITE);
        skin.add("title", titleStyle);

        Label title = new Label("TWO DOTS", skin, "title");
        title.setFontScale(1.5f);

        TextButton play = new TextButton("PLAY", skin);
        TextButton edit = new TextButton("EDITOR", skin);
        TextButton exit = new TextButton("EXIT", skin);

        play.getLabel().setFontScale(1.2f);
        edit.getLabel().setFontScale(1f);
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
