package com.ElisaFox.TwoDots.objects;

import com.badlogic.gdx.math.MathUtils;

public class Dot {
    private final ColorType color;
    private float x, y;
    private int targetRow, targetCol;

    public Dot(ColorType color, int row, int col) {
        this.color = color;
        this.targetRow = row;
        this.targetCol = col;

        this.x = col + 0.5f;
        this.y = row + 0.5f;
    }

    public void update(float delta) {
        float targetXWorld = targetCol + 0.5f;
        float targetYWorld = targetRow + 0.5f;

        if (y != targetYWorld) {
            y = MathUtils.lerp(y, targetYWorld, delta * 4f);
            if (Math.abs(y - targetYWorld) < 0.005f) {
                y = targetYWorld;
            }
        }

        if (x != targetXWorld) {
            x = MathUtils.lerp(x, targetXWorld, delta * 8f);
            if (Math.abs(x - targetXWorld) < 0.005f) {
                x = targetXWorld;
            }
        }
    }

    public ColorType getColor() {
        return color;
    }

    public int getTargetRow() {
        return targetRow;
    }

    public void setTargetRow(int targetRow) {
        this.targetRow = targetRow;
    }

    public int getTargetCol() {
        return targetCol;
    }

    public void setTargetCol(int targetCol) {
        this.targetCol = targetCol;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

}
