package com.ElisaFox.TwoDots.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LevelSerializer {
    private final Gson gson;

    public LevelSerializer() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    }

    public void saveLevel(FileHandle file, LevelData data) {
        if (!file.parent().exists()) {
            file.parent().mkdirs();
        }
        String jsonString = gson.toJson(data);
        file.writeString(jsonString, false);
    }

    public void saveLevel(LevelData data, String fileName) {
        saveLevel(Gdx.files.local("levels/" + fileName), data);
    }

    public LevelData loadLevel(FileHandle file) {
        if (!file.exists()) {
            return null;
        }

        String jsonString = file.readString();
        return gson.fromJson(jsonString, LevelData.class);
    }

    public LevelData loadLevel(String fileName) {
        return loadLevel(Gdx.files.local("levels/" + fileName));
    }
}
