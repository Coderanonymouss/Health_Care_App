package com.ensias.healthcareapp.model;

import com.google.firebase.firestore.Exclude;

/**
 * Бұл класс — қолданушының мәліметтерін сипаттайтын модель.
 * Firestore дерекқорына сәйкес келеді және пайдаланушы туралы
 * ақпаратты сақтауға арналған (аты, email, типі және т.б.).
 */
public class User {

    // Қолданушының бірегей идентификаторы (UID)
    private String uid;

    // Қолданушының толық аты
    private String fullName;

    // Қолданушының электрондық поштасы
    private String email;

    // Қолданушы рөлі (мысалы: doctor, patient, admin т.б.)
    private String type;

    /**
     * Firestore үшін қажет бос конструктор
     */
    public User() { }

    /**
     * Қолданушы объектісін параметрлермен құруға арналған конструктор
     *
     * @param uid Firebase UID
     * @param fullName Қолданушының аты-жөні
     * @param email Қолданушының email-і
     * @param type Қолданушының типі (роль)
     */
    public User(String uid, String fullName, String email, String type) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.type = type;
    }

    // ---------------- Getters және Setters ---------------- //

    /**
     * @return Қолданушының UID-і
     */
    public String getUid() {
        return uid;
    }

    /**
     * @param uid Қолданушының UID-ін орнатады
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * @return Қолданушының толық аты
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName Қолданушының атын орнатады
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return Қолданушының email мекенжайы
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email Қолданушының email мекенжайын орнатады
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return Қолданушының рөлі (типі)
     */
    public String getType() {
        return type;
    }

    /**
     * @param type Қолданушының типін орнатады
     */
    public void setType(String type) {
        this.type = type;
    }
}
