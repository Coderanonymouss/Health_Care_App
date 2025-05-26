package com.ensias.healthcareapp.model;

import com.google.firebase.Timestamp;

import java.util.List;

public class VideoFolder {
    private String id;
    private String name;
    private String createdBy; // добавь эти поля
    private Timestamp createdAt;
    private List<String> patients;

    public VideoFolder() {} // нужен для Firestore

    public VideoFolder(String name) {
        this.name = name;
    }

    public VideoFolder(String name, String createdBy, Timestamp createdAt,List<String> patients) {

        this.name = name;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.patients = patients;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getPatients() {
        return patients;
    }

    public void setPatients(List<String> patients) {
        this.patients = patients;
    }
}
