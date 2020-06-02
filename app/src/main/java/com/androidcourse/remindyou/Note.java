package com.androidcourse.remindyou;

import androidx.annotation.NonNull;

public class Note {
    private long id;
    private String title;
    private String content;
    private String time;
    private int tag;
    private String loca;
    private String path;

    public Note() {

    }

    public Note(String title, String content, String time, int tag, String loca, String path) {
        this.title = title;
        this.content = content;
        this.time = time;
        this.tag = tag;
        this.loca = loca;
        this.path = path;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }



    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    public int getTag() {
        return tag;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getLoca() {
        return loca;
    }

    public void setLoca(String loca) {
        this.loca = loca;
    }

    @NonNull
    @Override
    public String toString() {
        return content + "\n" + time.substring(5, 16) + " " + id;
    }
}
