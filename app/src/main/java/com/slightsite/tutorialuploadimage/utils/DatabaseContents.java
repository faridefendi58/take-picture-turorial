package com.slightsite.tutorialuploadimage.utils;

public enum DatabaseContents {

    DATABASE("uploadimage.db"),
    TABLE_PICTURES("tbl_pictures");

    private String name;

    /**
     * Constructs DatabaseContents.
     * @param name name of this content for using in database.
     */
    private DatabaseContents(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
