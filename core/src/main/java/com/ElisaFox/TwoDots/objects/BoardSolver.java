package com.ElisaFox.TwoDots.objects;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

public class BoardSolver {

    private static final int MAX_CHAIN_DEPTH = 10;
    private static final int MAX_LOOP_DEPTH = 12;

    private Array<Dot> bestMove;
    private int bestScore;
    private LevelGoals prGoals;
    private boolean lastMoveWasSquare;

    public Array<Dot> findBestMove(GameBoard board, LevelGoals goals) {
        bestMove = new Array<>();
        bestScore = Integer.MIN_VALUE;
        prGoals = goals;
        lastMoveWasSquare = false;

        if (board == null || goals == null) {
            return bestMove;
        }

        Array<Dot> squareMove = findBestLoop(board, goals);
        if (squareMove.size >= 4) {
            bestMove.clear();
            bestMove.addAll(squareMove);
            lastMoveWasSquare = true;
            return bestMove;
        }

        findBestChain(board);
        return bestMove;
    }

    public boolean wasLastMoveSquare() {
        return lastMoveWasSquare;
    }

    private Array<Dot> findBestLoop(GameBoard board, LevelGoals goals) {
        Array<Dot> bestLoop = new Array<>();
        int bestLoopScore = Integer.MIN_VALUE;

        Array<Dot> dots = board.getActiveDots();
        for (int i = 0; i < dots.size; i++) {
            Dot start = dots.get(i);
            if (start == null) continue;

            Array<Dot> path = new Array<>();
            ObjectSet<Dot> visited = new ObjectSet<>();

            path.add(start);
            visited.add(start);

            bestLoopScore = dfsLoop(board, start, start.getColor(), path, visited, goals, bestLoop, bestLoopScore);
        }

        return bestLoop;
    }

    private int dfsLoop(GameBoard board, Dot current, ColorType color, Array<Dot> path, ObjectSet<Dot> visited, LevelGoals goals, Array<Dot> bestLoop, int bestLoopScore) {
        if (path.size >= MAX_LOOP_DEPTH) return bestLoopScore;

        Array<Dot> neighbors = getNeighbors(board, current);
        for (int i = 0; i < neighbors.size; i++) {
            Dot next = neighbors.get(i);
            if (next == null || next.getColor() != color) continue;

            Dot previous = path.size >= 2 ? path.get(path.size - 2) : null;
            if (next == previous) continue;

            if (visited.contains(next)) {
                int loopStartIndex = path.indexOf(next, true);
                if (loopStartIndex != -1 && path.size - loopStartIndex >= 3) {
                    Array<Dot> loopPath = new Array<>();
                    for (int j = loopStartIndex; j < path.size; j++) {
                        loopPath.add(path.get(j));
                    }
                    loopPath.add(next);

                    int score = calculateSquareScore(loopPath, goals);
                    if (score > bestLoopScore) {
                        bestLoopScore = score;
                        bestLoop.clear();
                        bestLoop.addAll(loopPath);
                    }
                }
            } else {
                visited.add(next);
                path.add(next);
                bestLoopScore = dfsLoop(board, next, color, path, visited, goals, bestLoop, bestLoopScore);
                path.pop();
                visited.remove(next);
            }
        }
        return bestLoopScore;
    }

    private void findBestChain(GameBoard board) {
        bestScore = Integer.MIN_VALUE;

        Array<Dot> dots = board.getActiveDots();
        for (int i = 0; i < dots.size; i++) {
            Dot start = dots.get(i);
            if (start == null) continue;

            Array<Dot> path = new Array<>();
            ObjectSet<Dot> visited = new ObjectSet<>();

            path.add(start);
            visited.add(start);

            dfsChain(board, start, start.getColor(), path, visited);
        }
    }

    private void dfsChain(GameBoard board, Dot current, ColorType color, Array<Dot> path, ObjectSet<Dot> visited) {
        if (path.size >= MAX_CHAIN_DEPTH) return;

        Array<Dot> neighbors = getNeighbors(board, current);

        for (int i = 0; i < neighbors.size; i++) {
            Dot next = neighbors.get(i);
            if (next == null || next.getColor() != color) continue;

            Dot previous = path.size >= 2 ? path.get(path.size - 2) : null;
            if (next == previous) continue;

            if (visited.contains(next)) continue;

            visited.add(next);
            path.add(next);

            if (path.size >= 2) {
                int score = calculateScore(path, prGoals);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove.clear();
                    bestMove.addAll(path);
                }
            }

            dfsChain(board, next, color, path, visited);

            path.pop();
            visited.remove(next);
        }
    }

    private Array<Dot> getNeighbors(GameBoard board, Dot dot) {
        Array<Dot> result = new Array<>();
        int row = dot.getTargetRow();
        int col = dot.getTargetCol();

        addIfExists(board, result, row + 1, col);
        addIfExists(board, result, row - 1, col);
        addIfExists(board, result, row, col + 1);
        addIfExists(board, result, row, col - 1);

        return result;
    }

    private void addIfExists(GameBoard board, Array<Dot> result, int row, int col) {
        Dot d = board.getDotAt(row, col);
        if (d != null) result.add(d);
    }

    private int calculateScore(Array<Dot> path, LevelGoals goals) {
        ColorType color = path.first().getColor();
        int score = path.size * 10;
        int target = goals.getTarget(color);
        int collected = goals.getCollected(color);

        if (collected < target) {
            int remaining = target - collected;
            score += 100;
            score += Math.min(path.size, remaining) * 20;
        }
        return score;
    }

    private int calculateSquareScore(Array<Dot> path, LevelGoals goals) {
        ColorType color = path.first().getColor();
        int score = 1000;
        int target = goals.getTarget(color);
        int collected = goals.getCollected(color);

        if (collected < target) {
            score += 500;
        }
        score += path.size * 25;
        return score;
    }
}
