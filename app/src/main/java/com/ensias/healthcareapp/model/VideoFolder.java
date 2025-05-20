package com.ensias.healthcareapp.model;

public class VideoFolder {

    private String id; // если ты работаешь с Firestore document ID
    private String name;

    public VideoFolder() {
        // Пустой конструктор для Firestore
    }

    public VideoFolder(String name) {
        this.name = name;
    }

    public VideoFolder(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() { // <<< ОБЯЗАТЕЛЬНО нужен этот метод
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
