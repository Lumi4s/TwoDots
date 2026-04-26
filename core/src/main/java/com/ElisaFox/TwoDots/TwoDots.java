package com.ElisaFox.TwoDots;

import com.ElisaFox.TwoDots.screen.InGame;
import com.ElisaFox.TwoDots.screen.LVLEditor;
import com.ElisaFox.TwoDots.screen.Menu;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class TwoDots extends Game {
    public static int WORLD_WIDTH = 6;
    public static int WORLD_HEIGHT = 10;
    public TextureAtlas atlas;
    public SpriteBatch batch;
    public static BitmapFont font;

    public InGame inGame;
    public Menu menu;
    public LVLEditor lvlEditor;

    @Override
    public void create() {
        batch = new SpriteBatch();
        atlas = new TextureAtlas(Gdx.files.internal("pack.atlas"));
        font = new BitmapFont(Gdx.files.internal("font.fnt"));
        font.setUseIntegerPositions(false);
        font.getData().setScale(0.0043f);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        inGame = new InGame(this);
        menu = new Menu(this);
        lvlEditor = new LVLEditor(this);


        this.setScreen(menu);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        atlas.dispose();
        font.dispose();
    }
}
