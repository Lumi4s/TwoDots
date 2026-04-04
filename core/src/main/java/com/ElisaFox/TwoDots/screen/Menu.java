package com.ElisaFox.TwoDots.screen;

import com.ElisaFox.TwoDots.TwoDots;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;

public class Menu implements Screen {
    private final TwoDots game;
    private final InputAdapter input;

    public Menu(TwoDots game) {
        this.game = game;
        input = new InputAdapter() {
            @Override
            public boolean touchDown(int x, int y, int pointer, int button) {

                return true;
            }

            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {
                game.setScreen(game.inGame);
                return true;
            }
        };
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input);
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

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

    }
}
