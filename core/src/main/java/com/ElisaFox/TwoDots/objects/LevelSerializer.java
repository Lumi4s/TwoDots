package com.ElisaFox.TwoDots.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class LevelSerializer {
    private final Gson gson;

    public LevelSerializer() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    }

    public void saveLevel(LevelData data, String fileName) {
        FileHandle file = Gdx.files.local("levels/" + fileName);

        if (!file.parent().exists()) {
            file.parent().mkdirs();
        }
        String jsonString = gson.toJson(data);
        file.writeString(jsonString, false);
    }

    public LevelData loadLevel(String fileName) {
        FileHandle file = Gdx.files.local("levels/" + fileName);
        if (!file.exists()) {
            return null;
        }

        String jsonString = file.readString();
        return gson.fromJson(jsonString, LevelData.class);
    }
}
