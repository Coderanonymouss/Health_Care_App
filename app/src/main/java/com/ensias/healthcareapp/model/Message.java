package com.ensias.healthcareapp.model;

import com.google.firebase.Timestamp;

public class Message {
    private String text;         // Текст сообщения
    private String sender;       // Email отправителя
    private String senderName;
    private String type;         // "text", "image", "audio"
    private String fileUrl;      // Ссылка на файл (если есть)
    private Timestamp dateCreated; // Для сортировки

    private int duration;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    public Message() {} // Нужен для Firestore

    public Message(String text, String fileUrl, String sender, String senderName, String type) {
        this.text = text;
        this.fileUrl = fileUrl;
        this.sender = sender;
        this.senderName = senderName;
        this.type = type;
        this.dateCreated = Timestamp.now();
        this.duration = duration;
    }
    // Универсальный конструктор для любого типа
    public Message(String text, String fileUrl, String sender, String senderName, String type, int duration) {
        this.text = text;
        this.fileUrl = fileUrl;
        this.sender = sender;
        this.senderName = senderName;
        this.type = type;
        this.dateCreated = Timestamp.now();
        this.duration = duration;
    }

    // Getters
    public String getText() { return text; }
    public String getSender() { return sender; }
    public String getSenderName() { return senderName; }
    public String getType() { return type; }
    public String getFileUrl() { return fileUrl; }
    public Timestamp getDateCreated() { return dateCreated; }
}
