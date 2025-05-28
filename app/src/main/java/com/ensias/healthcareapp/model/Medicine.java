package com.ensias.healthcareapp.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Medicine {
    private String id;             // Уникальный идентификатор (UUID)
    private String name;
    private String dosage;
    private int timesPerDay;
    private int durationDays;
    private List<String> times;    // Список строк "08:00", "13:00", ...

    // Можно добавить историю приёмов (если требуется)
    private Map<String, Boolean> intakeHistory = new HashMap<>();

    // Пустой конструктор для Firestore
    public Medicine() {}

    public Medicine(String id, String name, String dosage,
                    int timesPerDay, int durationDays,
                    List<String> times) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.times = times;
        this.timesPerDay = timesPerDay;
        this.durationDays = durationDays;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public int getTimesPerDay() { return timesPerDay; }
    public void setTimesPerDay(int timesPerDay) { this.timesPerDay = timesPerDay; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public List<String> getTimes() { return times; }
    public void setTimes(List<String> times) { this.times = times; }

    public Map<String, Boolean> getIntakeHistory() { return intakeHistory; }
    public void setIntakeHistory(Map<String, Boolean> intakeHistory) { this.intakeHistory = intakeHistory; }
}
