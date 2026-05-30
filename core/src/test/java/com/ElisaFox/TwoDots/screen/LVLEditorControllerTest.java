package com.ElisaFox.TwoDots.screen;

import com.ElisaFox.TwoDots.objects.ColorType;
import com.ElisaFox.TwoDots.objects.LevelData;
import com.ElisaFox.TwoDots.objects.LevelSerializer;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LVLEditorControllerTest {

    private LVLEditorController controller;

    @BeforeEach
    void setUp() {
        controller = new LVLEditorController(6);
    }

    @Test
    void initialState_allCellsPassable() {
        for (int r = 0; r < 6; r++)
            for (int c = 0; c < 6; c++)
                assertTrue(controller.isCellPassable(r, c),
                        "Все клетки должны быть проходимыми по умолчанию");
    }

    @Test
    void initialState_movesZero() {
        assertEquals(0, controller.getMoves());
    }

    @Test
    void initialState_allGoalsZero() {
        for (ColorType ct : ColorType.values())
            assertEquals(0, controller.getGoal(ct));
    }

    @Test
    void initialState_gridSizeCorrect() {
        assertEquals(6, controller.getGridSize());
        assertEquals(6, controller.getGrid().length);
        assertEquals(6, controller.getGrid()[0].length);
    }

    @Test
    void toggleCell_passableBecomesBlocked() {
        controller.toggleCell(0, 0);
        assertFalse(controller.isCellPassable(0, 0));
    }

    @Test
    void toggleCell_blockedBecomesPassable() {
        controller.toggleCell(2, 3);
        assertFalse(controller.isCellPassable(2, 3));
        controller.toggleCell(2, 3);
        assertTrue(controller.isCellPassable(2, 3));
    }

    @Test
    void toggleCell_onlyAffectsTargetCell() {
        controller.toggleCell(1, 1);
        assertTrue(controller.isCellPassable(0, 0), "Соседняя клетка не должна измениться");
        assertTrue(controller.isCellPassable(1, 2), "Соседняя клетка не должна измениться");
    }

    @Test
    void toggleCell_firesCallback() {
        boolean[] fired = {false};
        boolean[] passableValue = {true};
        controller.setListener(new LVLEditorController.Listener() {
            @Override public void onCellChanged(int row, int col, boolean passable) {
                fired[0] = true;
                passableValue[0] = passable;
            }
            @Override public void onMovesChanged(int newMoves) {}
            @Override public void onGoalChanged(ColorType color, int newValue) {}
            @Override public void onSaved() {}
        });

        controller.toggleCell(0, 0);
        assertTrue(fired[0]);
        assertFalse(passableValue[0], "Callback должен сообщить новое состояние: blocked");
    }

    @Test
    void setMoves_updatesValue() {
        controller.setMoves(15);
        assertEquals(15, controller.getMoves());
    }

    @Test
    void setMoves_firesCallback() {
        int[] received = {-1};
        controller.setListener(new LVLEditorController.Listener() {
            @Override public void onCellChanged(int r, int c, boolean p) {}
            @Override public void onMovesChanged(int newMoves) { received[0] = newMoves; }
            @Override public void onGoalChanged(ColorType color, int newValue) {}
            @Override public void onSaved() {}
        });

        controller.setMoves(10);
        assertEquals(10, received[0]);
    }

    @Test
    void setGoal_updatesValue() {
        controller.setGoal(ColorType.RED, 7);
        assertEquals(7, controller.getGoal(ColorType.RED));
    }

    @Test
    void setGoal_doesNotAffectOtherColors() {
        controller.setGoal(ColorType.RED, 5);
        assertEquals(0, controller.getGoal(ColorType.BLUE));
        assertEquals(0, controller.getGoal(ColorType.GREEN));
    }

    @Test
    void setGoal_firesCallback() {
        ColorType[] receivedColor = {null};
        int[] receivedValue = {-1};
        controller.setListener(new LVLEditorController.Listener() {
            @Override public void onCellChanged(int r, int c, boolean p) {}
            @Override public void onMovesChanged(int m) {}
            @Override public void onGoalChanged(ColorType color, int newValue) {
                receivedColor[0] = color;
                receivedValue[0] = newValue;
            }
            @Override public void onSaved() {}
        });

        controller.setGoal(ColorType.BLUE, 3);
        assertEquals(ColorType.BLUE, receivedColor[0]);
        assertEquals(3, receivedValue[0]);
    }

    @Test
    void setGoal_canSetToZero() {
        controller.setGoal(ColorType.RED, 5);
        controller.setGoal(ColorType.RED, 0);
        assertEquals(0, controller.getGoal(ColorType.RED));
    }

    @Test
    void save_gridReflectsToggle(@TempDir Path tempDir) {
        controller.toggleCell(0, 0);
        controller.toggleCell(3, 3);

        FileHandle file = new FileHandle(tempDir.resolve("test_level.json").toFile());
        controller.save(file);

        LevelSerializer serializer = new LevelSerializer();
        LevelData loaded = serializer.loadLevel(file);
        int size = controller.getGridSize();

        assertNotNull(loaded);
        assertEquals(LevelData.CellType.EMPTY, loaded.grid[size - 1][0],
                "Editor row=0 должен сохраниться как row=5 в файле");
        assertEquals(LevelData.CellType.EMPTY, loaded.grid[size - 1 - 3][3],
                "Editor row=3 должен сохраниться как row=2 в файле");
        assertEquals(LevelData.CellType.NORMAL, loaded.grid[size - 1][1],
                "Нетронутая клетка должна быть NORMAL");
    }

    @Test
    void save_movesAndGoalsArePreserved(@TempDir Path tempDir) {
        controller.setMoves(12);
        controller.setGoal(ColorType.RED, 4);
        controller.setGoal(ColorType.BLUE, 6);

        FileHandle file = new FileHandle(tempDir.resolve("test_level2.json").toFile());
        controller.save(file);

        LevelSerializer serializer = new LevelSerializer();
        LevelData loaded = serializer.loadLevel(file);

        assertNotNull(loaded);
        assertEquals(12, loaded.steps);
        assertEquals(4, (int) loaded.targetGoals.get(ColorType.RED));
        assertEquals(6, (int) loaded.targetGoals.get(ColorType.BLUE));
    }

    @Test
    void save_firesSavedCallback(@TempDir Path tempDir) {
        boolean[] fired = {false};
        controller.setListener(new LVLEditorController.Listener() {
            @Override public void onCellChanged(int r, int c, boolean p) {}
            @Override public void onMovesChanged(int m) {}
            @Override public void onGoalChanged(ColorType color, int v) {}
            @Override public void onSaved() { fired[0] = true; }
        });

        FileHandle file = new FileHandle(tempDir.resolve("cb_test.json").toFile());
        controller.save(file);
        assertTrue(fired[0]);
    }

    @Test
    void customGridSize_works() {
        LVLEditorController c4 = new LVLEditorController(4);
        assertEquals(4, c4.getGridSize());
        assertEquals(4, c4.getGrid().length);
        assertEquals(4, c4.getGrid()[0].length);
        assertTrue(c4.isCellPassable(3, 3));
    }
}
