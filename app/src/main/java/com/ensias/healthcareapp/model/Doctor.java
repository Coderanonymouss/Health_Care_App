package com.ensias.healthcareapp.model;

public class Doctor {
    private String uid;
    private String fullName;
    private String adresse;
    private String tel;
    private String email;
    private String specialite;
    private String birthday;  // Добавлено поле для дня рождения
    private boolean firstSigninCompleted;  // Добавлено поле для первого входа
    private String type;  // Добавлено поле для типа (например, "Doctor")

    private String photoUrl;

    public Doctor() {
        // Пустой конструктор для Firebase
    }

    public Doctor(String fullName, String adresse, String tel, String email, String specialite, String birthday, boolean firstSigninCompleted, String type) {
        this.fullName = fullName;
        this.adresse = adresse;
        this.tel = tel;
        this.email = email;
        this.specialite = specialite;
        this.birthday = birthday;
        this.firstSigninCompleted = firstSigninCompleted;
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    // Геттеры и сеттеры для всех полей

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public boolean isFirstSigninCompleted() {
        return firstSigninCompleted;
    }

    public void setFirstSigninCompleted(boolean firstSigninCompleted) {
        this.firstSigninCompleted = firstSigninCompleted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
