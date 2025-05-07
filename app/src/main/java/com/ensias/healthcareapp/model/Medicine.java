package com.ensias.healthcareapp.model;

import java.util.ArrayList;
import java.util.List;

public class Medicine {
    private String name;
    private String dosage;
    private List<String> times;

    public Medicine(String name, String dosage, List<String> times) {
        this.name = name;
        this.dosage = dosage;
        this.times = times;
    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }

    public List<String> getTimes() {
        return times != null ? times : new ArrayList<>(); // добавлена защита
    }
}
