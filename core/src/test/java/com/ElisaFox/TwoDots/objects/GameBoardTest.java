package com.ElisaFox.TwoDots.objects;

import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameBoardTest {

    private LevelData levelData;
    private GameBoard gameBoard;
    private final int ROWS = 6;
    private final int COLS = 6;

    @BeforeEach
    void setUp() {
        levelData = new LevelData(10, ROWS, COLS);
        gameBoard = new GameBoard(levelData);
    }

    @Test
    void testFillBoard() {
        gameBoard.fillBoard();
        Array<Dot> activeDots = gameBoard.getActiveDots();

        assertEquals(ROWS * COLS, activeDots.size, "All normal cells should have a dot");

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                assertNotNull(gameBoard.getDotAt(r, c), "Every cell should contain a dot after fillBoard");
            }
        }
    }

    @Test
    void testGetDotAtBounds() {
        gameBoard.fillBoard();
        assertNotNull(gameBoard.getDotAt(0, 0));
        assertNotNull(gameBoard.getDotAt(ROWS - 1, COLS - 1));

        assertNull(gameBoard.getDotAt(-1, 0), "Should return null for negative row");
        assertNull(gameBoard.getDotAt(0, -1), "Should return null for negative col");
        assertNull(gameBoard.getDotAt(ROWS, 0), "Should return null for out of bounds row");
        assertNull(gameBoard.getDotAt(0, COLS), "Should return null for out of bounds col");
    }

    @Test
    void testUpdateLogicRemovesAndRefills() {
        gameBoard.fillBoard();

        Dot originalDot1 = gameBoard.getDotAt(0, 0);
        Dot originalDot2 = gameBoard.getDotAt(0, 1);

        Array<Dot> selected = new Array<>();
        selected.add(originalDot1);
        selected.add(originalDot2);

        gameBoard.updateLogic(selected);

        assertFalse(gameBoard.getActiveDots().contains(originalDot1, true), "Original dot 1 should be removed");
        assertFalse(gameBoard.getActiveDots().contains(originalDot2, true), "Original dot 2 should be removed");
        assertNotSame(originalDot1, gameBoard.getDotAt(0, 0), "Cell (0,0) should contain a NEW dot instance");
        assertNotSame(originalDot2, gameBoard.getDotAt(0, 1), "Cell (0,1) should contain a NEW dot instance");
        assertEquals(ROWS * COLS, gameBoard.getActiveDots().size, "Active dots count should remain constant due to refill");
    }

    @Test
    void testHandleFalling() {
        levelData = new LevelData(10, ROWS, COLS);
        levelData.grid[0][0] = LevelData.CellType.EMPTY;
        gameBoard = new GameBoard(levelData);
        gameBoard.fillBoard();

        Array<Dot> selected = new Array<>();
        selected.add(gameBoard.getDotAt(1, 0));
        selected.add(gameBoard.getDotAt(2, 0));

        gameBoard.updateLogic(selected);
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
        assertDoesNotThrow(() -> gameBoard.updateLogic(emptySelection));
        assertEquals(ROWS * COLS, gameBoard.getActiveDots().size);
    }

    @Test
    void testUpdateLogicWithNullInSelection() {
        gameBoard.fillBoard();
        Array<Dot> badSelection = new Array<>();
        badSelection.add(null);
        assertDoesNotThrow(() -> gameBoard.updateLogic(badSelection));
    }

    @Test
    void testRefillDotsAppearAboveBoard() {
        levelData = new LevelData(10, ROWS, COLS);
        gameBoard = new GameBoard(levelData);
        gameBoard.fillBoard();
        Array<Dot> allInCol0 = new Array<>();
        for (int r = 0; r < ROWS; r++) {
            allInCol0.add(gameBoard.getDotAt(r, 0));
        }

        gameBoard.updateLogic(allInCol0);
        for (int r = 0; r < ROWS; r++) {
            Dot dot = gameBoard.getDotAt(r, 0);
            assertNotNull(dot, "Column should be refilled");
            assertTrue(dot.getY() >= ROWS, "New dot should have a Y coordinate above the board");
        }
    }
}

