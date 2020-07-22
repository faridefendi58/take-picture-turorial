package com.slightsite.tutorialuploadimage.models;

import java.util.HashMap;
import java.util.Map;

public class Pictures {
    private int id;
    private String file_name;
    private String file_type;
    private int file_size;
    private String file_dir;
    private int group_id;

    /**
     * Static value for UNDEFINED ID.
     */
    public static final int UNDEFINED_ID = -1;


    public Pictures(int id, String file_name, String file_type, int file_size, String file_dir, int group_id) {
        this.id = id;
        this.file_name = file_name;
        this.file_type = file_type;
        this.file_size = file_size;
        this.file_dir = file_dir;
        this.group_id = group_id;
    }

    public Pictures(String file_name, String file_type, int file_size, String file_dir, int group_id) {
        this(UNDEFINED_ID, file_name, file_type, file_size, file_dir, group_id);
    }

    public void setName(String file_name) {
        this.file_name = file_name;
    }

    public void setType(String file_type) {
        this.file_type = file_type;
    }

    public void setSize(String file_size) {
        this.file_size = Integer.parseInt(file_size);
    }

    public void setDir(String file_dir) {
        this.file_dir = file_dir;
    }

    public void setGroup(Integer group_id) {
        this.group_id = group_id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return file_name;
    }

    public String getType() {
        return file_type;
    }

    public int getSize() {
        return file_size;
    }

    public String getDir() {
        return file_dir;
    }

    public int gerGroup() {
        return group_id;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", id + "");
        map.put("file_name", file_name);
        map.put("file_type", file_type);
        map.put("file_size", ""+ file_size);
        map.put("file_dir", file_dir);
        map.put("group_id", group_id + "");

        return map;
    }
}
