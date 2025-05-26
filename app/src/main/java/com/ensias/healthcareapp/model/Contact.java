package com.ensias.healthcareapp.model;

public class Contact {
    private String doctorUid;
    private String email;
    private String name;
    private String role;
    private String photoUrl;

    private String tel;

    public Contact() {} // Пустой конструктор для Firestore

    public Contact(String doctorUid,String email, String name, String role, String photoUrl,String tel) {
        this.doctorUid = doctorUid;
        this.email = email;
        this.name = name;
        this.role = role;
        this.photoUrl = photoUrl;
        this.tel = tel;
    }

    public String getDoctorUid() {
        return doctorUid;
    }

    public void setDoctorUid(String doctorUid) {
        this.doctorUid = doctorUid;
    }

    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getPhotoUrl() { return photoUrl; }

    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
