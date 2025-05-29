package com.ensias.healthcareapp.model;

import com.ensias.healthcareapp.patient.progress.adapter.VideoProgressAdapter;

// Для видео
public class VideoProgressRow {
    public String title;
    public boolean watched;
    public long watchedTime; // timestamp

    public VideoProgressRow(){}

    public VideoProgressRow(String title, boolean watched, long watchedTime) {
        this.title = title;
        this.watched = watched;
        this.watchedTime = watchedTime;
    }
}

