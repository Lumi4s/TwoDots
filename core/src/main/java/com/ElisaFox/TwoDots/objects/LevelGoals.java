package com.ElisaFox.TwoDots.objects;

import com.badlogic.gdx.utils.ObjectMap;

public class LevelGoals {
    private ObjectMap<ColorType, Integer> targets; // Что нужно собрать
    private ObjectMap<ColorType, Integer> collected; // Что уже собрано
    private int movesLeft;

    public LevelGoals(int moves) {
        this.targets = new ObjectMap<>();
        this.collected = new ObjectMap<>();
        this.movesLeft = moves;
    }

    public void addGoal(ColorType color, int amount) {
        targets.put(color, amount);
        collected.put(color, 0);
    }

    public void increment(ColorType color, int amount) {
        if (targets.containsKey(color)) {
            int current = collected.get(color);
            collected.put(color, current + amount);
        }
    }

    public void useMove() {
        movesLeft--;
    }

    public boolean isWin() {
        for (ColorType color : targets.keys()) {
            if (collected.get(color) < targets.get(color)) {
                return false;
            }
        }
        return true;
    }

    public boolean isLose() {
        return movesLeft <= 0 && !isWin();
    }

    public int getMovesLeft() { return movesLeft; }
    public int getCollected(ColorType color) { return collected.get(color, 0); }
    public int getTarget(ColorType color) { return targets.get(color, 0); }
    public ObjectMap.Keys<ColorType> getGoalColors() { return targets.keys(); }
}
