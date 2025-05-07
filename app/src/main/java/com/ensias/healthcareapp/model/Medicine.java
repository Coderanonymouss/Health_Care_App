package com.ensias.healthcareapp.model;

public class Medicine {
    private final String name;
    private final String dosage;
    private final String time; // например, "9:00", "14:00"

    public Medicine(String name, String dosage, String time) {
        this.name = name;
        this.dosage = dosage;
        this.time = time;
    }

    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getTime() { return time; }
}

