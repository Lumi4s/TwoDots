package com.ElisaFox.TwoDots.objects;

import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameBoardTest {

    private LevelData levelData;
    private GameBoard gameBoard;
    private final int ROWS = 6; // Based on TwoDots.ROWS
    private final int COLS = 6; // Based on TwoDots.COLS

    @BeforeEach
    void setUp() {
        // Initialize a simple level with all NORMAL cells
        levelData = new LevelData(10, ROWS, COLS);
        gameBoard = new GameBoard(levelData);
    }

    @Test
    void testFillBoard() {
        gameBoard.fillBoard();
        Array<Dot> activeDots = gameBoard.getActiveDots();

        // Since all cells are NORMAL, we expect ROWS * COLS dots
        assertEquals(ROWS * COLS, activeDots.size, "All normal cells should have a dot");

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                assertNotNull(gameBoard.getDotAt(r, c), "Every cell should contain a dot after fillBoard");
            }
        }
    }

    @Test
    void testGetDotAtBounds() {
        // Test valid bounds
        // Note: We need to fill the board first so getDotAt returns something non-null if we want to check content
        gameBoard.fillBoard();
        assertNotNull(gameBoard.getDotAt(0, 0));
        assertNotNull(gameBoard.getDotAt(ROWS - 1, COLS - 1));

        // Test invalid bounds
        assertNull(gameBoard.getDotAt(-1, 0), "Should return null for negative row");
        assertNull(gameBoard.getDotAt(0, -1), "Should return null for negative col");
        assertNull(gameBoard.getDotAt(ROWS, 0), "Should return null for out of bounds row");
        assertNull(gameBoard.getDotAt(0, COLS), "Should return null for out of bounds col");
    }

    @Test
    void testUpdateLogicRemovesAndRefills() {
        gameBoard.fillBoard();

        // Capture the dots currently at (0,0) and (0,1) so we can identify them
        Dot originalDot1 = gameBoard.getDotAt(0, 0);
        Dot originalDot2 = gameBoard.getDotAt(0, 1);

        Array<Dot> selected = new Array<>();
        selected.add(originalDot1);
        selected.add(originalDot2);

        gameBoard.updateLogic(selected);

        // 1. Verify the specific original dots are no longer in the active list
        assertFalse(gameBoard.getActiveDots().contains(originalDot1, true), "Original dot 1 should be removed");
        assertFalse(gameBoard.getActiveDots().contains(originalDot2, true), "Original dot 2 should be removed");

        // 2. Verify that (0,0) and (0,1) still have dots (because handleFalling refills them)
        // But they MUST be different instances than the originals
        assertNotSame(originalDot1, gameBoard.getDotAt(0, 0), "Cell (0,0) should contain a NEW dot instance");
        assertNotSame(originalDot2, gameBoard.getDotAt(0, 1), "Cell (0,1) should contain a NEW dot instance");

        // 3. Verify the total count remains stable (since we removed 2 and refilled 2)
        assertEquals(ROWS * COLS, gameBoard.getActiveDots().size, "Active dots count should remain constant due to refill");
    }

    @Test
    void testHandleFalling() {
        // Create a custom level with an EMPTY cell at (0,0) to test falling
        levelData = new LevelData(10, ROWS, COLS);
        levelData.grid[0][0] = LevelData.CellType.EMPTY;
        gameBoard = new GameBoard(levelData);

        // Fill board: dots will be placed in all NORMAL cells
        gameBoard.fillBoard();

        // Let's use updateLogic with a selection that targets (1,0) and (2,0)
        // This will trigger removal of those two, then handleFalling will shift
        // the dot from (3,0) up to (2,0), etc.
        Array<Dot> selected = new Array<>();
        selected.add(gameBoard.getDotAt(1, 0));
        selected.add(gameBoard.getDotAt(2, 0));

        gameBoard.updateLogic(selected);

        // After removing (1,0) and (2,0), the dot that was at (3,0) should fall to (2,0)
        // We check if there is still a dot at (2,0) after falling
        assertNotNull(gameBoard.getDotAt(2, 0), "There should be a dot at (2,0) after falling");
    }

    @Test
    void testFillBoardWithEmptyCells() {
        levelData = new LevelData(10, ROWS, COLS);
        levelData.grid[0][0] = LevelData.CellType.EMPTY;
        gameBoard = new GameBoard(levelData);

        gameBoard.fillBoard();

        assertNull(gameBoard.getDotAt(0, 0), "Empty cell should not have a dot");
    }
    @Test
    void testUpdateLogicWithEmptySelection() {
        gameBoard.fillBoard();
        Array<Dot> emptySelection = new Array<>();

        // Не должно упасть и не должно ничего изменить
        assertDoesNotThrow(() -> gameBoard.updateLogic(emptySelection));
        assertEquals(ROWS * COLS, gameBoard.getActiveDots().size);
    }

    @Test
    void testUpdateLogicWithNullInSelection() {
        gameBoard.fillBoard();
        Array<Dot> badSelection = new Array<>();
        badSelection.add(null);

        // Проверяем, что метод устойчив к null в списке (если это предусмотрено логикой)
        // В текущем коде removeSelectedDots может упасть на dot.getTargetRow(),
        // если не добавить проверку на null. Это отличный способ найти баг!
        assertDoesNotThrow(() -> gameBoard.updateLogic(badSelection));
    }

    @Test
    void testRefillDotsAppearAboveBoard() {
        levelData = new LevelData(10, ROWS, COLS);
        gameBoard = new GameBoard(levelData);
        gameBoard.fillBoard();

        // Удаляем все точки в первом столбце
        Array<Dot> allInCol0 = new Array<>();
        for (int r = 0; r < ROWS; r++) {
            allInCol0.add(gameBoard.getDotAt(r, 0));
        }

        gameBoard.updateLogic(allInCol0);

        // Проверяем, что столбец заполнился новыми точками
        for (int r = 0; r < ROWS; r++) {
            Dot dot = gameBoard.getDotAt(r, 0);
            assertNotNull(dot, "Column should be refilled");
            // Проверяем, что Y координата указывает на то, что точка "прилетела" сверху
            // В твоем коate: newDot.setY(rows + (r * 0.3f) + 1);
            assertTrue(dot.getY() >= ROWS, "New dot should have a Y coordinate above the board");
        }
    }
}

