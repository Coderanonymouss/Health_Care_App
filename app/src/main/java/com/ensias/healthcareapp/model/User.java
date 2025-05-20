package com.ensias.healthcareapp.model;

import com.google.firebase.firestore.Exclude;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String type; // doctor, patient, admin и т.п.

    public User() { }

    public User( String uid,String fullName, String email, String type) {

        this.fullName = fullName;
        this.email = email;
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
