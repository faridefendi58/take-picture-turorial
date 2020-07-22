package com.slightsite.tutorialuploadimage.controllers;

import android.content.ContentValues;

import com.slightsite.tutorialuploadimage.models.Pictures;
import com.slightsite.tutorialuploadimage.utils.Database;
import com.slightsite.tutorialuploadimage.utils.DatabaseContents;
import com.slightsite.tutorialuploadimage.utils.DateTimeStrategy;

import java.util.List;

public class PictureController {
    private static Database database;
    private static PictureController instance;

    private PictureController() {}

    public static PictureController getInstance() {
        if (instance == null)
            instance = new PictureController();
        return instance;
    }

    /**
     * Sets database for use in this class.
     * @param db database.
     */
    public static void setDatabase(Database db) {
        database = db;
    }

    public List<Object> getItems() {
        List<Object> contents = database.select("SELECT * FROM " + DatabaseContents.TABLE_PICTURES);

        return contents;
    }

    public int addPicture(Pictures picture) {
        ContentValues content = new ContentValues();
        content.put("file_name", picture.getName());
        content.put("file_type", picture.getType());
        content.put("file_size", picture.getSize());
        content.put("file_dir", picture.getDir());
        content.put("group_id", 0);
        content.put("date_added", DateTimeStrategy.getCurrentTime());

        int id = database.insert(DatabaseContents.TABLE_PICTURES.toString(), content);

        return id;
    }

    public boolean removePicture(int id) {
        return database.delete(DatabaseContents.TABLE_PICTURES.toString(), id);
    }
}
