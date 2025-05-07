package com.ensias.healthcareapp.model;

public class VideoLesson {
    private final String title;
    private boolean watched;

    public VideoLesson(String title, boolean watched) {
        this.title = title;
        this.watched = watched;
    }

    public String getTitle() {
        return title;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }
}


