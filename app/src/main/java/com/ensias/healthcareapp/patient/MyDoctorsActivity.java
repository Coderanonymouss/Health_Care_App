package com.ensias.healthcareapp.patient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.ChatActivity;
import com.ensias.healthcareapp.model.Doctor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MyDoctorsActivity extends AppCompatActivity {

    private ImageView doctorImage;
    private TextView doctorName, doctorSpeciality;
    private Button chatBtn, callBtn;
    private String doctorEmail, doctorPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_doctors);

        doctorImage = findViewById(R.id.doctor_image);
        doctorName = findViewById(R.id.doctor_name);
        doctorSpeciality = findViewById(R.id.doctor_speciality);
        chatBtn = findViewById(R.id.doctor_chat);
        callBtn = findViewById(R.id.doctor_call);

        loadDoctor();
    }

    private void loadDoctor() {
        String currentPatientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (currentPatientEmail == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("Patient")
                .whereEqualTo("email", currentPatientEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Берём первый найденный документ
                        DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
                        if (snapshot.contains("doctorId")) {
                            doctorEmail = snapshot.getString("doctorId");
                            if (doctorEmail != null && !doctorEmail.isEmpty()) {
                                loadDoctorData(doctorEmail);
                            } else {
                                doctorName.setText("Доктор не назначен");
                                doctorSpeciality.setText("");
                            }
                        }
                    } else {
                        doctorName.setText("Доктор не назначен");
                        doctorSpeciality.setText("");
                    }
                })
                .addOnFailureListener(e -> {
                    doctorName.setText("Ошибка загрузки доктора");
                });
    }

    private void loadDoctorData(String doctorId) {
        FirebaseFirestore.getInstance()
                .collection("Doctor")
                .document(doctorId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Doctor doctor = snapshot.toObject(Doctor.class);
                        if (doctor != null) {
                            doctorName.setText(doctor.getFullName());
                            doctorSpeciality.setText(doctor.getSpecialite());
                            doctorPhone = doctor.getTel();

                            // Загрузка фото доктора
                            StorageReference imgRef = FirebaseStorage.getInstance()
                                    .getReference("DoctorProfile/" + doctor.getEmail() + ".jpg");
                            imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Picasso.get().load(uri).into(doctorImage);
                            });

                            chatBtn.setOnClickListener(v -> openChat(doctor.getEmail()));
                            callBtn.setOnClickListener(v -> callDoctor(doctorPhone));
                        }
                    } else {
                        doctorName.setText("Доктор не найден");
                        doctorSpeciality.setText("");
                    }
                });
    }

    private void openChat(String doctorEmail) {
        String patientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("key1", doctorEmail + "_" + patientEmail);
        i.putExtra("key2", patientEmail + "_" + doctorEmail);
        startActivity(i);
    }

    private void callDoctor(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }
}
