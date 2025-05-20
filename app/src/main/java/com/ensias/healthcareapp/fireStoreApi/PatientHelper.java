package com.ensias.healthcareapp.fireStoreApi;

import com.ensias.healthcareapp.model.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class PatientHelper {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static CollectionReference PatientRef = db.collection("Patient");

    public static void addPatient(String doctorId, String firstName, String lastName, String email, String address, String tel) {
        Patient patient = new Patient();
        patient.setDoctorId(doctorId);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setEmail(email);
        patient.setAddress(address);
        patient.setTel(tel);
        patient.setFirstSigninCompleted(true); // если нужно
        patient.setUid(email); // или другой уникальный идентификатор

        System.out.println("Создаём объект patient");

        // Документ можно создать по email пациента или с помощью .document() с уникальным id
        PatientRef.document(email).set(patient);
    }
}
