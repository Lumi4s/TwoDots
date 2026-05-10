package com.ElisaFox.TwoDots.objects;

import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class BoardSolverTest {

    private BoardSolver solver;
    private LevelData levelData;
    private LevelGoals goals;
    private GameBoard board;

    @BeforeEach
    void setUp() {
        solver = new BoardSolver();
        levelData = new LevelData(10, 6, 6);

        Map<ColorType, Integer> targetMap = new HashMap<>();
        targetMap.put(ColorType.RED, 5);
        goals = new LevelGoals(10, targetMap);

        board = new GameBoard(levelData);
    }

    @Test
    void testFindBestMoveNullInputs() {
        Array<Dot> move = solver.findBestMove(null, null);
        assertNotNull(move);
        assertEquals(0, move.size);
    }

    @Test
    void testFindSimpleChain() {
        board = new GameBoard(levelData);

        Dot d1 = new Dot(ColorType.RED, 0, 0);
        Dot d2 = new Dot(ColorType.RED, 0, 1);
        Dot d3 = new Dot(ColorType.RED, 0, 2);

        board.setDotAt(0, 0, d1);
        board.setDotAt(0, 1, d2);
        board.setDotAt(0, 2, d3);
        Array<Dot> move = solver.findBestMove(board, goals);

        assertNotNull(move);
        assertEquals(3, move.size, "Should find the chain of 3 dots");
    }

    @Test
    void testSquareDetection() {
        board = new GameBoard(levelData);

        board.setDotAt(0, 0, new Dot(ColorType.BLUE, 0, 0));
        board.setDotAt(0, 1, new Dot(ColorType.BLUE, 0, 1));
        board.setDotAt(1, 0, new Dot(ColorType.BLUE, 1, 0));
        board.setDotAt(1, 1, new Dot(ColorType.BLUE, 1, 1));

        Array<Dot> move = solver.findBestMove(board, goals);

        assertNotNull(move);
        assertTrue(move.size >= 4, "Should detect the square move");
    }

    @Test
    void testWasLastMoveSquare() {
        board.fillBoard();
        solver.findBestMove(board, goals);
        boolean result = solver.wasLastMoveSquare();
    }

    @Test
    void testNoPossibleMoves() {
        board = new GameBoard(levelData);
        Array<Dot> move = solver.findBestMove(board, goals);
        assertEquals(0, move.size, "Should return empty move when no dots exist");
    }

    @Test
    void testScoringPrefersNeededColors() {
        Map<ColorType, Integer> targetMap = new HashMap<>();
        targetMap.put(ColorType.RED, 5);
        targetMap.put(ColorType.BLUE, 5);
        goals = new LevelGoals(10, targetMap);
        board = new GameBoard(levelData);

        board.setDotAt(0,0, new Dot(ColorType.RED, 0, 0));
        board.setDotAt(0,1, new Dot(ColorType.RED, 0, 1));

        board.setDotAt(5,0, new Dot(ColorType.BLUE, 5, 0));
        board.setDotAt(5,1, new Dot(ColorType.BLUE, 5, 1));
        board.setDotAt(5,2, new Dot(ColorType.BLUE, 5, 2));

        Array<Dot> move = solver.findBestMove(board, goals);
        assertEquals(3, move.size, "Should pick the longer chain");
    }

    @Test
    void testMaxDepthLimit() {
        board = new GameBoard(levelData);
        for (int i = 0; i < 15; i++) {
            board.setDotAt(0, i % 6, new Dot(ColorType.RED, 0, i % 6));
        }

        Array<Dot> move = solver.findBestMove(board, goals);
        assertTrue(move.size <= 10, "Should not exceed MAX_CHAIN_DEPTH");
    }
}

