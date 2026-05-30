package com.ElisaFox.TwoDots.objects;

import com.badlogic.gdx.utils.ObjectMap;

import java.util.Map;

public class LevelGoals {
    private final ObjectMap<ColorType, Integer> targets;
    private final ObjectMap<ColorType, Integer> collected;
    private int movesLeft;

    public LevelGoals() {
        this.targets = new ObjectMap<>();
        this.collected = new ObjectMap<>();
        this.movesLeft = 0;
    }

    public LevelGoals(int movesLeft, Map<ColorType, Integer> targetMap) {
        this();
        this.movesLeft = movesLeft;
        for (Map.Entry<ColorType, Integer> entry : targetMap.entrySet()) {
            targets.put(entry.getKey(), entry.getValue());
            collected.put(entry.getKey(), 0);
        }
    }

    public void increment(ColorType color, int amount) {
        if (targets.containsKey(color)) {
            int current = collected.get(color);
            int target = targets.get(color);

            int putting = Math.min(current + amount, target);

            collected.put(color, putting);
        }
    }

    public void setTarget(ColorType color, int amount) {
        if (targets.containsKey(color)) {
            targets.put(color, amount);
        } else {
            targets.put(color, amount);
            collected.put(color, 0);
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

    public int getMovesLeft() {
        return movesLeft;
    }

    public void setMovesLeft(int moves) {
        this.movesLeft = moves;
    }

    public int getCollected(ColorType color) {
        return collected.get(color, 0);
    }

    public int getTarget(ColorType color) {
        return targets.get(color, 0);
    }

    public ObjectMap.Keys<ColorType> getGoalColors() {
        return targets.keys();
    }
}
