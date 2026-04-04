package com.ElisaFox.TwoDots.ui;

import com.ElisaFox.TwoDots.objects.ColorType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;


public class VisualLine {
    private Array<Vector2> positions;
    private ColorType color;

    public VisualLine(ColorType color){
        positions = new Array<>();
        this.color = color;
    }

    public ColorType getColor() {
        return color;
    }

    public Array<Vector2> getPositions() {
        return positions;
    }

    public void addNewPosition(Vector2 cords) {
        positions.add(cords);
    }
}
