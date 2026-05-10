package com.ElisaFox.TwoDots.objects;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DotTest {

    @Test
    void testDotInitialization() {
        ColorType color = ColorType.RED;
        int row = 2;
        int col = 3;
        Dot dot = new Dot(color, row, col);

        assertEquals(color, dot.getColor(), "Color should match the initialized color");
        assertEquals(row, dot.getTargetRow(), "Target row should match the initialized row");
        assertEquals(col, dot.getTargetCol(), "Target column should match the initialized column");

        assertEquals(3.5f, dot.getX(), 0.001f, "X position should be col + 0.5");
        assertEquals(2.5f, dot.getY(), 0.001f, "Y position should be row + 0.5");
    }

    @Test
    void testSetters() {
        Dot dot = new Dot(ColorType.BLUE, 0, 0);

        dot.setTargetRow(5);
        dot.setTargetCol(10);
        assertEquals(5, dot.getTargetRow());
        assertEquals(10, dot.getTargetCol());

        dot.setX(123.4f);
        dot.setY(567.8f);
        assertEquals(123.4f, dot.getX());
        assertEquals(567.8f, dot.getY());
    }

    @Test
    void testUpdateMovementTowardsTarget() {
        Dot dot = new Dot(ColorType.GREEN, 0, 0);
        float initialX = dot.getX();
        float initialY = dot.getY();

        dot.setTargetRow(2);
        dot.setTargetCol(2);

        dot.update(0.1f);

        assertTrue(dot.getX() > initialX, "X should increase");
        assertTrue(dot.getY() > initialY, "Y should increase");
        assertTrue(dot.getX() < 2.5f, "X should not have reached target yet");
    }

    @Test
    void testUpdateSnapsToTarget() {
        Dot dot = new Dot(ColorType.GREEN, 0, 0);

        dot.setTargetRow(1);
        dot.setTargetCol(1);

        dot.setX(1.498f);
        dot.setY(1.498f);

        dot.update(0.1f);

        assertEquals(1.5f, dot.getX(), 0.0001f, "X should snap exactly to targetCol + 0.5");
        assertEquals(1.5f, dot.getY(), 0.0001f, "Y should snap exactly to targetRow + 0.5");
    }

    @Test
    void testUpdateWithNoMovementRequired() {
        Dot dot = new Dot(ColorType.RED, 5, 5);
        float x = dot.getX();
        float y = dot.getY();

        dot.update(0.1f);

        assertEquals(x, dot.getX(), "X should not change if target is current position");
        assertEquals(y, dot.getY(), "Y should not change if target is current position");
    }

    @Test
    void testUpdateWithZeroDelta() {
        Dot dot = new Dot(ColorType.RED, 0, 0);
        dot.setTargetRow(5);
        dot.setTargetCol(5);

        float initialX = dot.getX();
        float initialY = dot.getY();

        dot.update(0f);

        assertEquals(initialX, dot.getX(), "X should not move with zero delta");
        assertEquals(initialY, dot.getY(), "Y should not move with zero delta");
    }
}
