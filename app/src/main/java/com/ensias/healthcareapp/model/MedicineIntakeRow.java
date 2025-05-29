package com.ensias.healthcareapp.model;

// Для лекарства
public class MedicineIntakeRow {
    public String name;
    public int timesPerDay;
    public int takenToday;
    public int remainingToday;
    public int daysLeft;

    public MedicineIntakeRow() {
    }

    public MedicineIntakeRow(String name, int timesPerDay, int takenToday, int remainingToday, int daysLeft) {
        this.name = name;
        this.timesPerDay = timesPerDay;
        this.takenToday = takenToday;
        this.remainingToday = remainingToday;
        this.daysLeft = daysLeft;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTimesPerDay() {
        return timesPerDay;
    }

    public void setTimesPerDay(int timesPerDay) {
        this.timesPerDay = timesPerDay;
    }

    public int getTakenToday() {
        return takenToday;
    }

    public void setTakenToday(int takenToday) {
        this.takenToday = takenToday;
    }

    public int getRemainingToday() {
        return remainingToday;
    }

    public void setRemainingToday(int remainingToday) {
        this.remainingToday = remainingToday;
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(int daysLeft) {
        this.daysLeft = daysLeft;
    }
}
