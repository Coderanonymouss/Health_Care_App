package com.ensias.healthcareapp.model;

public class VideoLesson {
    private String id;
    private String title;
    private String videoUrl;    // Должно быть videoUrl, а не url!
    private String createdBy;
    private Object createdAt;   // Можешь заменить на Timestamp, если используешь com.google.firebase.Timestamp
    private boolean available; // доступно для просмотра
    private boolean watched;   // просмотрено

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean a) { this.available = a; }
    public boolean isWatched() { return watched; }
    public void setWatched(boolean w) { this.watched = w; }
    public VideoLesson() {}

    public VideoLesson(String title, String videoUrl) {
        this.title = title;
        this.videoUrl = videoUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVideoUrl() { return videoUrl; } // Правильное имя!
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Object getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
}
