package com.ElisaFox.TwoDots.screen;

import com.ElisaFox.TwoDots.objects.*;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class InGameControllerTest {

    private InGameController controller;
    private LevelData levelData;

    private LevelData makeLevel(int moves) {
        LevelData data = new LevelData(moves, 6, 6);
        data.targetGoals.put(ColorType.RED, 5);
        data.targetGoals.put(ColorType.BLUE, 5);
        return data;
    }

    @BeforeEach
    void setUp() {
        controller = new InGameController();
        levelData = makeLevel(20);
        controller.startLevel(levelData);
    }

    @Test
    void startLevel_boardIsFilledAndGoalsInitialized() {
        assertNotNull(controller.getGameBoard(), "GameBoard должен быть создан");
        assertEquals(36, controller.getGameBoard().getActiveDots().size,
                "6x6 поле должно содержать 36 точек");

        LevelGoals goals = controller.getGoals();
        assertNotNull(goals);
        assertEquals(20, goals.getMovesLeft());
        assertEquals(5, goals.getTarget(ColorType.RED));
        assertEquals(5, goals.getTarget(ColorType.BLUE));
    }

    @Test
    void startLevel_clearsSelectionState() {
        controller.getSelectedDots().add(new Dot(ColorType.RED, 0, 0));
        controller.setIsSquared(true);

        controller.startLevel(levelData);

        assertEquals(0, controller.getSelectedDots().size,
                "После startLevel выделение должно быть пустым");
        assertFalse(controller.isSquared(), "isSquared должен сброситься");
        assertFalse(controller.isDragging(), "isDragging должен сброситься");
    }

    @Test
    void startLevel_firesOnGoalsChangedCallback() {
        int[] calls = {0};
        controller.setListener(new InGameController.Listener() {
            @Override public void onGoalsChanged() { calls[0]++; }
            @Override public void onGameEnd(boolean win) {}
        });

        controller.startLevel(levelData);
        assertEquals(1, calls[0], "Listener.onGoalsChanged должен быть вызван после startLevel");
    }

    @Test
    void processDots_singleDotDoesNothing() {
        Dot d = controller.getGameBoard().getDotAt(0, 0);
        controller.getSelectedDots().add(d);

        int movesBefore = controller.getGoals().getMovesLeft();
        controller.processDots();

        assertEquals(movesBefore, controller.getGoals().getMovesLeft(),
                "Одна точка не должна тратить ход");
        assertEquals(0, controller.getSelectedDots().size);
    }

    @Test
    void processDots_twoDotsConsumesOneMoveAndClearsSelection() {
        GameBoard board = controller.getGameBoard();
        Dot d1 = new Dot(ColorType.RED, 0, 0);
        Dot d2 = new Dot(ColorType.RED, 0, 1);
        board.setDotAt(0, 0, d1);
        board.setDotAt(0, 1, d2);

        controller.getSelectedDots().add(d1);
        controller.getSelectedDots().add(d2);

        int movesBefore = controller.getGoals().getMovesLeft();
        controller.processDots();

        assertEquals(movesBefore - 1, controller.getGoals().getMovesLeft(),
                "Должен быть потрачен один ход");
        assertEquals(0, controller.getSelectedDots().size,
                "После хода выделение должно быть пустым");
        assertFalse(controller.isSquared());
    }

    @Test
    void processDots_incrementsCollectedForColor() {
        GameBoard board = controller.getGameBoard();
        Dot d1 = new Dot(ColorType.RED, 0, 0);
        Dot d2 = new Dot(ColorType.RED, 0, 1);
        Dot d3 = new Dot(ColorType.RED, 0, 2);
        board.setDotAt(0, 0, d1);
        board.setDotAt(0, 1, d2);
        board.setDotAt(0, 2, d3);

        controller.getSelectedDots().add(d1);
        controller.getSelectedDots().add(d2);
        controller.getSelectedDots().add(d3);

        controller.processDots();

        assertEquals(3, controller.getGoals().getCollected(ColorType.RED),
                "Должно засчитаться 3 красных точки");
    }

    @Test
    void processDots_doesNotExceedTarget() {
        GameBoard board = controller.getGameBoard();
        Array<Dot> sel = controller.getSelectedDots();

        for (int c = 0; c < 6; c++) {
            Dot d = new Dot(ColorType.RED, 0, c);
            board.setDotAt(0, c, d);
            sel.add(d);
        }

        controller.processDots();

        assertEquals(5, controller.getGoals().getCollected(ColorType.RED),
                "Собранное не должно превышать цель");
    }

    @Test
    void processDots_squaredClearsAllDotsOfThatColor() {
        LevelData allRedData = new LevelData(20, 6, 6);
        allRedData.targetGoals.put(ColorType.RED, 36);
        controller.startLevel(allRedData);

        GameBoard board = controller.getGameBoard();
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                board.setDotAt(r, c, new Dot(ColorType.RED, r, c));
            }
        }

        controller.getSelectedDots().add(new Dot(ColorType.RED, 0, 0));
        controller.getSelectedDots().add(new Dot(ColorType.RED, 0, 1));
        controller.setIsSquared(true);

        controller.processDots();

        assertEquals(36, controller.getGoals().getCollected(ColorType.RED));
    }

    @Test
    void processDots_callsOnGameEndWinWhenGoalsMet() {
        boolean[] winFired = {false};
        controller.setListener(new InGameController.Listener() {
            @Override public void onGoalsChanged() {}
            @Override public void onGameEnd(boolean win) { winFired[0] = win; }
        });

        LevelData easy = new LevelData(10, 6, 6);
        easy.targetGoals.put(ColorType.RED, 2);
        controller.startLevel(easy);

        GameBoard board = controller.getGameBoard();
        Dot d1 = new Dot(ColorType.RED, 0, 0);
        Dot d2 = new Dot(ColorType.RED, 0, 1);
        board.setDotAt(0, 0, d1);
        board.setDotAt(0, 1, d2);

        controller.getSelectedDots().add(d1);
        controller.getSelectedDots().add(d2);

        controller.processDots();

        assertTrue(winFired[0], "Должен быть вызван onGameEnd(true)");
    }

    @Test
    void processDots_callsOnGameEndLoseWhenMovesExhausted() {
        boolean[] loseFired = {false};
        controller.setListener(new InGameController.Listener() {
            @Override public void onGoalsChanged() {}
            @Override public void onGameEnd(boolean win) { if (!win) loseFired[0] = true; }
        });

        LevelData hard = new LevelData(1, 6, 6);
        hard.targetGoals.put(ColorType.RED, 100);
        controller.startLevel(hard);

        GameBoard board = controller.getGameBoard();
        Dot d1 = new Dot(ColorType.RED, 0, 0);
        Dot d2 = new Dot(ColorType.RED, 0, 1);
        board.setDotAt(0, 0, d1);
        board.setDotAt(0, 1, d2);

        controller.getSelectedDots().add(d1);
        controller.getSelectedDots().add(d2);
        controller.processDots();

        assertTrue(loseFired[0], "Должен быть вызван onGameEnd(false)");
    }

    @Test
    void isNear_horizontalNeighborIsNear() {
        Dot base = new Dot(ColorType.RED, 2, 2);
        Dot right = new Dot(ColorType.RED, 2, 3);
        controller.getSelectedDots().add(base);
        assertTrue(controller.isNear(right));
    }

    @Test
    void isNear_verticalNeighborIsNear() {
        Dot base = new Dot(ColorType.RED, 2, 2);
        Dot above = new Dot(ColorType.RED, 3, 2);
        controller.getSelectedDots().add(base);
        assertTrue(controller.isNear(above));
    }

    @Test
    void isNear_diagonalIsNotNear() {
        Dot base = new Dot(ColorType.RED, 2, 2);
        Dot diag = new Dot(ColorType.RED, 3, 3);
        controller.getSelectedDots().add(base);
        assertFalse(controller.isNear(diag));
    }

    @Test
    void isNear_farDotIsNotNear() {
        Dot base = new Dot(ColorType.RED, 0, 0);
        Dot far  = new Dot(ColorType.RED, 5, 5);
        controller.getSelectedDots().add(base);
        assertFalse(controller.isNear(far));
    }

    @Test
    void isSquareDots_validSquareReturnsTrue() {
        Dot d00 = new Dot(ColorType.BLUE, 0, 0);
        Dot d01 = new Dot(ColorType.BLUE, 0, 1);
        Dot d11 = new Dot(ColorType.BLUE, 1, 1);
        Dot d10 = new Dot(ColorType.BLUE, 1, 0);

        Array<Dot> sel = controller.getSelectedDots();
        sel.add(d00);
        sel.add(d01);
        sel.add(d11);
        sel.add(d10);

        assertTrue(controller.isSquareDots(d00));
    }

    @Test
    void isSquareDots_nonClosingDotReturnsFalse() {
        Dot d00 = new Dot(ColorType.BLUE, 0, 0);
        Dot d01 = new Dot(ColorType.BLUE, 0, 1);
        Dot d11 = new Dot(ColorType.BLUE, 1, 1);
        Dot d10 = new Dot(ColorType.BLUE, 1, 0);
        Dot other = new Dot(ColorType.BLUE, 2, 2);

        Array<Dot> sel = controller.getSelectedDots();
        sel.add(d00);
        sel.add(d01);
        sel.add(d11);
        sel.add(d10);

        assertFalse(controller.isSquareDots(other),
                "Точка не из начала цепочки не должна образовывать квадрат");
    }

    @Test
    void isSquareDots_tooFewDotsReturnsFalse() {
        Dot d00 = new Dot(ColorType.BLUE, 0, 0);
        Dot d01 = new Dot(ColorType.BLUE, 0, 1);
        Dot d11 = new Dot(ColorType.BLUE, 1, 1);

        Array<Dot> sel = controller.getSelectedDots();
        sel.add(d00);
        sel.add(d01);
        sel.add(d11);

        assertFalse(controller.isSquareDots(d00),
                "3 точки не могут образовывать квадрат");
    }

    @Test
    void getDotAtWorld_centerOfCellHitsDot() {
        GameBoard board = controller.getGameBoard();
        Dot expected = new Dot(ColorType.GREEN, 2, 3);
        board.setDotAt(2, 3, expected);

        Dot hit = controller.getDotAtWorld(3.5f, 2.5f);
        assertSame(expected, hit);
    }

    @Test
    void getDotAtWorld_edgeOfCellMissesDot() {
        GameBoard board = controller.getGameBoard();
        Dot d = new Dot(ColorType.GREEN, 2, 3);
        board.setDotAt(2, 3, d);

        Dot hit = controller.getDotAtWorld(3.0f, 2.0f);
        assertNotSame(d, hit,
                "Угол клетки находится за радиусом 0.3f и не должен попадать в точку");
    }

    @Test
    void getDotAtWorld_outOfBoundsReturnsNull() {
        assertNull(controller.getDotAtWorld(-1f, 0f));
        assertNull(controller.getDotAtWorld(0f, -1f));
        assertNull(controller.getDotAtWorld(100f, 0f));
    }

    @Test
    void rerollField_costsTwoMoves() {
        int before = controller.getGoals().getMovesLeft();
        controller.rerollField();
        assertEquals(before - 2, controller.getGoals().getMovesLeft());
    }

    @Test
    void rerollField_createsNewBoard() {
        GameBoard boardBefore = controller.getGameBoard();
        controller.rerollField();
        assertNotSame(boardBefore, controller.getGameBoard(),
                "После reroll должна быть новая доска");
        assertEquals(36, controller.getGameBoard().getActiveDots().size);
    }

    @Test
    void rerollField_refusedWhenNotEnoughMoves() {
        LevelData tight = makeLevel(2);
        controller.startLevel(tight);

        GameBoard boardBefore = controller.getGameBoard();
        boolean result = controller.rerollField();

        assertFalse(result, "reroll должен вернуть false при <= 2 ходах");
        assertSame(boardBefore, controller.getGameBoard(),
                "Доска не должна измениться");
    }

    @Test
    void rerollField_clearsSelectionAndFlags() {
        controller.getSelectedDots().add(new Dot(ColorType.RED, 0, 0));
        controller.setIsSquared(true);

        controller.rerollField();

        assertEquals(0, controller.getSelectedDots().size);
        assertFalse(controller.isSquared());
        assertFalse(controller.isDragging());
    }

    @Test
    void touch_downStartsDragging() {
        GameBoard board = controller.getGameBoard();
        board.setDotAt(0, 0, new Dot(ColorType.RED, 0, 0));

        controller.touchDown(0.5f, 0.5f);

        assertTrue(controller.isDragging());
        assertEquals(1, controller.getSelectedDots().size);
    }

    @Test
    void touch_dragAddsAdjacentSameColor() {
        GameBoard board = controller.getGameBoard();
        Dot d1 = new Dot(ColorType.RED, 0, 0);
        Dot d2 = new Dot(ColorType.RED, 0, 1);
        board.setDotAt(0, 0, d1);
        board.setDotAt(0, 1, d2);

        controller.touchDown(0.5f, 0.5f);
        controller.touchDragged(1.5f, 0.5f);

        assertEquals(2, controller.getSelectedDots().size);
    }

    @Test
    void touch_dragDoesNotAddDifferentColor() {
        GameBoard board = controller.getGameBoard();
        Dot d1 = new Dot(ColorType.RED, 0, 0);
        Dot d2 = new Dot(ColorType.BLUE, 0, 1);
        board.setDotAt(0, 0, d1);
        board.setDotAt(0, 1, d2);

        controller.touchDown(0.5f, 0.5f);
        controller.touchDragged(1.5f, 0.5f);

        assertEquals(1, controller.getSelectedDots().size,
                "Точка другого цвета не должна добавляться");
    }

    @Test
    void touch_dragBackRemovesLastDot() {
        GameBoard board = controller.getGameBoard();
        Dot d1 = new Dot(ColorType.RED, 0, 0);
        Dot d2 = new Dot(ColorType.RED, 0, 1);
        Dot d3 = new Dot(ColorType.RED, 0, 2);
        board.setDotAt(0, 0, d1);
        board.setDotAt(0, 1, d2);
        board.setDotAt(0, 2, d3);

        controller.touchDown(0.5f, 0.5f);
        controller.touchDragged(1.5f, 0.5f);
        controller.touchDragged(2.5f, 0.5f);
        assertEquals(3, controller.getSelectedDots().size);

        controller.touchDragged(1.5f, 0.5f);
        assertEquals(2, controller.getSelectedDots().size);
        assertSame(d2, controller.getSelectedDots().peek());
    }

    @Test
    void touch_upProcessesAndClearsSelection() {
        GameBoard board = controller.getGameBoard();
        Dot d1 = new Dot(ColorType.RED, 0, 0);
        Dot d2 = new Dot(ColorType.RED, 0, 1);
        board.setDotAt(0, 0, d1);
        board.setDotAt(0, 1, d2);

        controller.touchDown(0.5f, 0.5f);
        controller.touchDragged(1.5f, 0.5f);
        controller.touchUp();

        assertFalse(controller.isDragging());
        assertEquals(0, controller.getSelectedDots().size,
                "После touchUp выделение должно быть очищено");
    }

    @Test
    void autoPlay_defaultIsOff() {
        assertFalse(controller.isAutoPlay());
    }

    @Test
    void autoPlay_toggleWorks() {
        controller.setAutoPlay(true);
        assertTrue(controller.isAutoPlay());
        controller.setAutoPlay(false);
        assertFalse(controller.isAutoPlay());
    }

    @Test
    void updateAuto_doesNothingWhenAutoPlayOff() {
        int movesBefore = controller.getGoals().getMovesLeft();
        controller.updateAuto(2f);
        assertEquals(movesBefore, controller.getGoals().getMovesLeft(),
                "При выключенном autoPlay ходы не должны тратиться");
    }

    @Test
    void updateAuto_doesNothingWhenGameAlreadyWon() {
        LevelData wonData = new LevelData(20, 6, 6);
        wonData.targetGoals.put(ColorType.RED, 0);
        controller.startLevel(wonData);
        controller.setAutoPlay(true);

        int movesBefore = controller.getGoals().getMovesLeft();
        controller.updateAuto(2f);
        assertEquals(movesBefore, controller.getGoals().getMovesLeft(),
                "После победы autoPlay не должен делать ходы");
    }

    @Test
    void updateAuto_makesAMoveAfterDelay() {
        controller.setAutoPlay(true);
        int movesBefore = controller.getGoals().getMovesLeft();

        controller.updateAuto(1.1f);

        for (int i = 0; i < 20; i++) {
            controller.updateAuto(0.5f);
            if (!controller.isBotAnimating()) break;
        }

        assertTrue(controller.getGoals().getMovesLeft() < movesBefore,
                "После полного цикла autoPlay должен быть потрачен хотя бы один ход");
    }
}
