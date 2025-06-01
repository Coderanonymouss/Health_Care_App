package com.ensias.healthcareapp.model;

public class Patient {
    private String doctorId;
    private String doctorUid;
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private String address;
    private String tel;
    private Boolean firstSigninCompleted;
    private String uid;
    private String photoUrl;

    private String diagnosis;

    private String iin;

    private String rehabStage;

    // Пустой конструктор нужен для Firebase
    public Patient() {}

    // Геттеры и сеттеры для всех полей


    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getIin() {
        return iin;
    }

    public void setIin(String iin) {
        this.iin = iin;
    }

    public String getRehabStage() {
        return rehabStage;
    }

    public void setRehabStage(String rehabStage) {
        this.rehabStage = rehabStage;
    }

    public String getDoctorUid() {
        return doctorUid;
    }

    public void setDoctorUid(String doctorUid) {
        this.doctorUid = doctorUid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Boolean getFirstSigninCompleted() {
        return firstSigninCompleted;
    }

    public void setFirstSigninCompleted(Boolean firstSigninCompleted) {
        this.firstSigninCompleted = firstSigninCompleted;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    // При необходимости можно добавить метод getName() для удобства
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
