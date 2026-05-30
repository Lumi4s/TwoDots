package com.ElisaFox.TwoDots.screen;

import com.ElisaFox.TwoDots.objects.ColorType;
import com.ElisaFox.TwoDots.objects.LevelData;
import com.ElisaFox.TwoDots.objects.LevelGoals;
import com.ElisaFox.TwoDots.objects.LevelSerializer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class LVLEditorController {

    private final int gridSize;
    private final boolean[][] grid;
    private final LevelGoals goals = new LevelGoals();
    private final LevelSerializer levelSerializer = new LevelSerializer();

    public interface Listener {
        void onCellChanged(int row, int col, boolean passable);
        void onMovesChanged(int newMoves);
        void onGoalChanged(ColorType color, int newValue);
        void onSaved();
    }

    private Listener listener;

    public LVLEditorController(int gridSize) {
        this.gridSize = gridSize;
        this.grid = new boolean[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++)
                grid[i][j] = true;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void toggleCell(int row, int col) {
        grid[row][col] = !grid[row][col];
        if (listener != null) listener.onCellChanged(row, col, grid[row][col]);
    }

    public void setMoves(int moves) {
        goals.setMovesLeft(moves);
        if (listener != null) listener.onMovesChanged(moves);
    }

    public void setGoal(ColorType color, int value) {
        goals.setTarget(color, value);
        if (listener != null) listener.onGoalChanged(color, value);
    }

    public void save(FileHandle file) {
        LevelData data = buildLevelData();
        levelSerializer.saveLevel(file, data);
        if (listener != null) listener.onSaved();
    }

    public void save(String filename) {
        save(Gdx.files.local("levels/" + filename));
    }

    private LevelData buildLevelData() {
        LevelData data = new LevelData(goals.getMovesLeft(), gridSize, gridSize);
        for (int r = 0; r < gridSize; r++) {
            int flippedR = gridSize - 1 - r;
            for (int c = 0; c < gridSize; c++)
                data.grid[flippedR][c] = grid[r][c] ? LevelData.CellType.NORMAL : LevelData.CellType.EMPTY;
        }
        for (ColorType ct : ColorType.values())
            data.targetGoals.put(ct, goals.getTarget(ct));
        return data;
    }

    public boolean isCellPassable(int row, int col) { return grid[row][col]; }
    public int getMoves() { return goals.getMovesLeft(); }
    public int getGoal(ColorType color) { return goals.getTarget(color); }
    public int getGridSize() { return gridSize; }
    public boolean[][] getGrid() { return grid; }
    public LevelGoals getGoals() { return goals; }
}