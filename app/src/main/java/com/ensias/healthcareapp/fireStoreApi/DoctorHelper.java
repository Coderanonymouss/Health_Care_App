package com.ensias.healthcareapp.fireStoreApi;

import com.ensias.healthcareapp.model.Doctor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class DoctorHelper {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static CollectionReference DoctorRef = db.collection("Doctor");

    public static void addDoctor(String name, String adresse, String tel, String specialite, String birthday, boolean firstSigninCompleted, String type) {
        // Создаем объект doctor с дополнительными полями
        Doctor doctor = new Doctor(name, adresse, tel,
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),
                specialite, birthday, firstSigninCompleted, type);

        // Сохраняем документ с email как идентификатор
        DoctorRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail())).set(doctor);
    }
}
