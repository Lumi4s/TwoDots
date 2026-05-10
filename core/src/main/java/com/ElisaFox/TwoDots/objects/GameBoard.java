package com.ElisaFox.TwoDots.objects;

import com.ElisaFox.TwoDots.TwoDots;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GameBoard {
    private Dot[][] grid;
    private int rows, cols;
    private Array<Dot> activeDots;
    private LevelData levelData;

    public GameBoard(LevelData levelData) {
        this.rows = TwoDots.ROWS;
        this.cols = TwoDots.COLS;
        this.grid = new Dot[rows][cols];
        this.activeDots = new Array<>();
        this.levelData = levelData;
    }

    public void fillBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (levelData.grid[r][c] == LevelData.CellType.NORMAL) {
                    ColorType randomColor = ColorType.values()[MathUtils.random(ColorType.values().length - 1)];
                    Dot dot = new Dot(randomColor, r, c);
                    grid[r][c] = dot;
                    activeDots.add(dot);
                } else {
                    grid[r][c] = null;
                }
            }
        }
    }

    public Array<Dot> getActiveDots() {
        return activeDots;
    }

    public void updateLogic(Array<Dot> selected) {
        if (selected.size < 2) return;

        removeSelectedDots(selected);
        handleFalling();
    }

    private void removeSelectedDots(Array<Dot> selected) {
        for (Dot dot : selected) {
            grid[dot.getTargetRow()][dot.getTargetCol()] = null;
            activeDots.removeValue(dot, true);
        }
    }

    private void handleFalling() {
        for (int c = 0; c < cols; c++) {
            Array<Dot> survivors = new Array<>();
            for (int r = 0; r < rows; r++) {
                if (grid[r][c] != null) {
                    survivors.add(grid[r][c]);
                    grid[r][c] = null;
                }
            }

            int survivorIndex = 0;
            for (int r = 0; r < rows; r++) {
                if (levelData.grid[r][c] == LevelData.CellType.NORMAL) {
                    if (survivorIndex < survivors.size) {
                        Dot dot = survivors.get(survivorIndex);
                        grid[r][c] = dot;
                        dot.setTargetRow(r);
                        survivorIndex++;
                    } else {
                        ColorType newColor = ColorType.values()[MathUtils.random(ColorType.values().length - 1)];
                        Dot newDot = new Dot(newColor, r, c);
                        newDot.setY(rows + (r * 0.3f) + 1);

                        grid[r][c] = newDot;
                        activeDots.add(newDot);
                    }
                }
            }
        }
    }

    public Dot getDotAt(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return grid[row][col];
        }
        return null;
    }
}
