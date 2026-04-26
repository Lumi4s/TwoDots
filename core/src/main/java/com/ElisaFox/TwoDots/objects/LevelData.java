package com.ElisaFox.TwoDots.objects;

import java.util.HashMap;
import java.util.Map;

public class LevelData {
    
    public enum CellType {
        NORMAL,   // Обычная клетка (есть точка)
        EMPTY,    // Пустое пространство (точка пролетает сквозь него)
        BLOCK     // Препятствие (точка не может пройти)
    }

    public int steps;
    public Map<ColorType, Integer> goals = new HashMap<>();
    public CellType[][] grid;

    public LevelData() {} 

    public LevelData(int steps, int rows, int cols) {
        this.steps = steps;
        this.grid = new CellType[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = CellType.NORMAL;
            }
        }
    }
}