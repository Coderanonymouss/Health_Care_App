//package com.ensias.healthcareapp.testinfo;
//
//
//import android.os.Bundle;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.ensias.healthcareapp.model.Patient;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.List;
//
//public class AddTestPatientsActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Получаем email текущего врача
//        String doctorEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//        if (doctorEmail == null) {
//            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Получаем список тестовых пациентов
//        List<Patient> patients = TestPatientGenerator.getTestPatients(doctorEmail);
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Добавляем пациентов в базу
//        for (Patient patient : patients) {
//            String patientDocId = patient.getEmail();
//
//            db.collection("Patient").document(patientDocId)
//                    .get()
//                    .addOnSuccessListener(snapshot -> {
//                        if (!snapshot.exists()) {
//                            // Новый пациент: создаём запись
//                            db.collection("Patient").document(patientDocId).set(patient);
//
//                            // Привязываем к врачу
//                            db.collection("Doctor")
//                                    .document(doctorEmail)
//                                    .collection("MyPatients")
//                                    .document(patientDocId)
//                                    .set(patient);
//
//                            // Обновляем ссылку на врача у пациента
//                            db.collection("Patient")
//                                    .document(patientDocId)
//                                    .update("doctorId", doctorEmail);
//                        }
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(this, "Ошибка добавления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    });
//        }
//
//        Toast.makeText(this, "Тестовые пациенты добавлены!", Toast.LENGTH_LONG).show();
//        // finish(); // Закрыть activity, если не нужна UI
//    }
//}