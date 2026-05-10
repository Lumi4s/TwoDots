package com.ElisaFox.TwoDots.objects;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.ElisaFox.TwoDots.objects.ColorType;

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

        // Based on Dot.java: this.x = col + 0.5f; this.y = row + 0.5f;
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

        // Change target to (2, 2) -> World coords (2.5, 2.5)
        dot.setTargetRow(2);
        dot.setTargetCol(2);

        // Update with a small delta
        dot.update(0.1f);

        // Position should have moved towards target but not reached it yet
        assertTrue(dot.getX() > initialX, "X should increase");
        assertTrue(dot.getY() > initialY, "Y should increase");
        assertTrue(dot.getX() < 2.5f, "X should not have reached target yet");
    }

    @Test
    void testUpdateSnapsToTarget() {
        // Start at (0,0) -> World (0.5, 0.5)
        Dot dot = new Dot(ColorType.GREEN, 0, 0);

        // Set target to (1,1) -> World (1.5, 1.5)
        dot.setTargetRow(1);
        dot.setTargetCol(1);

        // Instead of a loop that might cause NaN with large deltas,
        // we simulate the dot being VERY close to the target.
        // This forces the 'if (Math.abs(...) < 0.005f)' branch to execute.
        dot.setX(1.498f);
        dot.setY(1.498f);

        // One update should now trigger the snap logic
        dot.update(0.1f);

        assertEquals(1.5f, dot.getX(), 0.0001f, "X should snap exactly to targetCol + 0.5");
        assertEquals(1.5f, dot.getY(), 0.0001f, "Y should snap exactly to targetRow + 0.5");
    }

    @Test
    void testUpdateWithNoMovementRequired() {
        Dot dot = new Dot(ColorType.RED, 5, 5);
        float x = dot.getX();
        float y = dot.getY();

        // Update without changing target
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

        // Delta of 0 should result in no movement
        dot.update(0f);

        assertEquals(initialX, dot.getX(), "X should not move with zero delta");
        assertEquals(initialY, dot.getY(), "Y should not move with zero delta");
    }
}
