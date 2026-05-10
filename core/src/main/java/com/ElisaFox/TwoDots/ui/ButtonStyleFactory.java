package com.ElisaFox.TwoDots.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class ButtonStyleFactory {
    public static Skin createBaseSkin(BitmapFont font) {
        Skin skin = new Skin();

        // Белая текстура для стандартных элементов (если понадобятся)
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        skin.add("default-font", font);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;

        style.up = createRectangularDrawable(new Color(0.18f, 0.55f, 0.95f, 1f));
        style.over = createRectangularDrawable(new Color(0.28f, 0.65f, 1f, 1f));
        style.down = createRectangularDrawable(new Color(0.10f, 0.40f, 0.80f, 1f));

        skin.add("default", style);
        return skin;
    }

    private static Drawable createRectangularDrawable(Color color) {
        int width = 260;
        int height = 180;

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();

        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}
