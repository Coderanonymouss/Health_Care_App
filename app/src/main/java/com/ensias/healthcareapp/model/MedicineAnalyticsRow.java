package com.ensias.healthcareapp.model;

public class MedicineAnalyticsRow {
    public String name;
    public int totalDays;
    public int timesPerDay;
    public int totalIntakes;      // всего приёмов (totalDays * timesPerDay)
    public int takenCount;        // принято
    public int missedCount;       // пропущено
    public float progressPercent; // прогресс %

    public MedicineAnalyticsRow(String name, int totalDays, int timesPerDay, int takenCount, int missedCount) {
        this.name = name;
        this.totalDays = totalDays;
        this.timesPerDay = timesPerDay;
        this.totalIntakes = totalDays * timesPerDay;
        this.takenCount = takenCount;
        this.missedCount = missedCount;
        this.progressPercent = totalIntakes == 0 ? 0 : 100f * takenCount / totalIntakes;
    }
}
