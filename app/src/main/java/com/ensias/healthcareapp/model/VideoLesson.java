// VideoLesson.java
package com.ensias.healthcareapp.model;

public class VideoLesson {
    private String id;
    private String title;
    private String url;
    private boolean watched;

    public VideoLesson() {} // Firestore

    public VideoLesson(String title, String url, boolean watched) {
        this.title = title;
        this.url = url;
        this.watched = watched;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }
}
