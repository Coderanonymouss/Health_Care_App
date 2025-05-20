package com.ensias.healthcareapp.activity.doctor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.ApointementInformation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DoctorDetailActivity extends AppCompatActivity {
    private TextView tvName, tvSpec, tvPhone;
    private Button btnReq;

    private FirebaseFirestore db;
    private String patientEmail, patientName,patientPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_detail);

        tvName  = findViewById(R.id.tvDocName);
        tvSpec  = findViewById(R.id.tvDocSpeciality);
        tvPhone = findViewById(R.id.tvDocPhone);
        btnReq  = findViewById(R.id.btnRequestControl);

        db = FirebaseFirestore.getInstance();

        // Проверка на null (иногда бывает баг при FirebaseAuth)
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            patientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            patientName  = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            patientPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        }

        // Получаем данные врача из Intent
        String docEmail = getIntent().getStringExtra("doctorEmail");
        String docName  = getIntent().getStringExtra("doctorName");
        String docSpec  = getIntent().getStringExtra("doctorSpeciality");
        String docPhone = getIntent().getStringExtra("doctorPhone");

        // Отображаем
        tvName.setText(docName);
        tvSpec.setText(docSpec);
        tvPhone.setText(docPhone);

        btnReq.setOnClickListener(v -> sendControlRequest(docEmail, docName));
    }

    private void sendControlRequest(String docEmail, String docName) {
        String patientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("Patient")
                .whereEqualTo("email", patientEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                        String patientName = doc.getString("fullName");
                        String patientPhone = doc.getString("tel");

                        // --- Обработка null и пустых значений ---
                        if (patientName == null || patientName.trim().isEmpty()) {
                            patientName = "Не указано";
                        }
                        if (patientPhone == null || patientPhone.trim().isEmpty()) {
                            patientPhone = "Не указано";
                        }

                        ApointementInformation ap = new ApointementInformation();
                        ap.setDoctorId(docEmail);
                        ap.setDoctorName(docName);
                        ap.setPatientId(patientEmail);
                        ap.setPatientName(patientName);
                        ap.setPatientPhone(patientPhone);

                        String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                .format(new Date());
                        ap.setTime(formatted);
                        ap.setType("Requested");
                        ap.setApointementType("Control");

                        WriteBatch batch = db.batch();

                        DocumentReference ctrlRef = db.collection("ControlRequests").document();
                        ap.setChemin("ControlRequests/" + ctrlRef.getId());
                        batch.set(ctrlRef, ap);

                        DocumentReference docReqRef = db
                                .collection("Doctor")
                                .document(docEmail)
                                .collection("apointementrequest")
                                .document();
                        batch.set(docReqRef, ap);

                        DocumentReference patReqRef = db
                                .collection("Patient")
                                .document(patientEmail)
                                .collection("apointementrequest")
                                .document();
                        batch.set(patReqRef, ap);

                        batch.commit()
                                .addOnSuccessListener(__ -> {
                                    Toast.makeText(this, "Запрос на контроль отправлен", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    } else {
                        Toast.makeText(this, "Не найден профиль пациента!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка получения профиля: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}
