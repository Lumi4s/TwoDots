package com.ElisaFox.TwoDots.objects;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GameBoard {
    private Dot[][] grid;
    private int rows, cols;
    private Array<Dot> activeDots;

    public GameBoard(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Dot[rows][cols];
        this.activeDots = new Array<>();
        fillBoard();
    }

    private void fillBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                ColorType randomColor = ColorType.values()[MathUtils.random(ColorType.values().length - 1)];
                Dot dot = new Dot(randomColor, r, c);
                grid[r][c] = dot;
                activeDots.add(dot);
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
            int emptySpaces = 0;
            for (int r = 0; r < rows; r++) {
                if (grid[r][c] == null) {
                    emptySpaces++;
                } else if (emptySpaces > 0) {
                    Dot dot = grid[r][c];
                    grid[r - emptySpaces][c] = dot;
                    grid[r][c] = null;
                    dot.setTargetRow(r - emptySpaces);
                }
            }

            for (int i = 0; i < emptySpaces; i++) {
                int targetRow = rows - emptySpaces + i;
                ColorType newColor = ColorType.values()[MathUtils.random(ColorType.values().length - 1)];

                Dot newDot = new Dot(newColor, targetRow, c);
                newDot.setY(rows + i + 0.5f);

                grid[targetRow][c] = newDot;
                activeDots.add(newDot);
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
