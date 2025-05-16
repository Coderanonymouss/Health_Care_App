package com.ensias.healthcareapp.model;

public class VideoLesson {
    private String title;
    private String url;
    private boolean watched;

    public VideoLesson() {}

    public VideoLesson(String title, String url, boolean watched) {
        this.title = title;
        this.url = url;
        this.watched = watched;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }
}
