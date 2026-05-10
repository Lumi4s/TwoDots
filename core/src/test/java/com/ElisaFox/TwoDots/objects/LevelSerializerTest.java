package com.ElisaFox.TwoDots.objects;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LevelSerializerTest {

    private Gson gson;
    private LevelData sampleLevel;

    @BeforeEach
    void setUp() {
        gson = new Gson();
        sampleLevel = new LevelData(10, 5, 5);

        sampleLevel.targetGoals.put(ColorType.RED, 3);
        sampleLevel.targetGoals.put(ColorType.BLUE, 5);
        sampleLevel.grid[0][0] = LevelData.CellType.EMPTY;
        sampleLevel.grid[1][1] = LevelData.CellType.NORMAL;
    }

    @Test
    void testGsonSerializationRoundTrip() {
        String json = gson.toJson(sampleLevel);
        assertNotNull(json, "JSON string should not be null");

        LevelData deserialized = gson.fromJson(json, LevelData.class);

        assertNotNull(deserialized, "Deserialized object should not be null");
        assertEquals(sampleLevel.steps, deserialized.steps, "Steps should match");
        assertEquals(sampleLevel.grid.length, deserialized.grid.length, "Rows should match");
        assertEquals(sampleLevel.grid[0].length, deserialized.grid[0].length, "Cols should match");
        assertEquals(sampleLevel.targetGoals.get(ColorType.RED), deserialized.targetGoals.get(ColorType.RED));
        assertEquals(sampleLevel.targetGoals.get(ColorType.BLUE), deserialized.targetGoals.get(ColorType.BLUE));
        assertEquals(LevelData.CellType.EMPTY, deserialized.grid[0][0], "Cell (0,0) should be EMPTY");
        assertEquals(LevelData.CellType.NORMAL, deserialized.grid[1][1], "Cell (1,1) should be NORMAL");
    }

    @Test
    void testEmptyGoals() {
        sampleLevel.targetGoals.clear();
        String json = gson.toJson(sampleLevel);
        LevelData deserialized = gson.fromJson(json, LevelData.class);
        assertTrue(deserialized.targetGoals.isEmpty(), "Goals should be empty");
    }
}
