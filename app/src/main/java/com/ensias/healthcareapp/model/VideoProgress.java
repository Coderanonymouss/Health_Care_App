package com.ensias.healthcareapp.model;

import java.util.Map;

public class VideoProgress {
    private Map<String, Long> viewed;
    private Long lastVideoDate;
    private int currentIndex;

    public VideoProgress() {}

    public Map<String, Long> getViewed() { return viewed; }
    public void setViewed(Map<String, Long> viewed) { this.viewed = viewed; }
    public Long getLastVideoDate() { return lastVideoDate; }
    public void setLastVideoDate(Long lastVideoDate) { this.lastVideoDate = lastVideoDate; }
    public int getCurrentIndex() { return currentIndex; }
    public void setCurrentIndex(int currentIndex) { this.currentIndex = currentIndex; }
}
