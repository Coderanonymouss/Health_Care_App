package com.ensias.healthcareapp.model;

public class Intake {
    private String medicine;   // название лекарства (опционально)
    private String datetime;   // ключ вида "2024-05-27 09:00"
    private boolean status;    // принято или нет
    private long timestamp;    // время в миллисекундах (опционально)

    public Intake() {}

    public Intake(String medicine, String datetime, boolean status, long timestamp) {
        this.medicine = medicine;
        this.datetime = datetime;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getMedicine() { return medicine; }
    public void setMedicine(String medicine) { this.medicine = medicine; }

    public String getDatetime() { return datetime; }
    public void setDatetime(String datetime) { this.datetime = datetime; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
