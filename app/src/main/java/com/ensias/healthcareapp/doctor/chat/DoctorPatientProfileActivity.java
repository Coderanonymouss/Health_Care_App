package com.ensias.healthcareapp.doctor.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.doctor.videolesson.DoctorUploadVideoActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DoctorPatientProfileActivity extends AppCompatActivity {

    private ImageView ivPatientPhoto;
    private TextView tvIIN, tvName, tvEmail, tvPhone, tvVideoStats, tvMedStats, tvOverdueVideos, tvOverdueMeds, tvOverallProgress;
    private ProgressBar progressOverall;
    private MaterialButton btnAddVideo;
    private String patientUid;
    private String patientEmail;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_patient_profile);

        ivPatientPhoto = findViewById(R.id.ivPatientPhoto);
        tvName = findViewById(R.id.tvPatientName);
        tvEmail = findViewById(R.id.tvPatientEmail); // ✅ используем email вместо iin
        tvPhone = findViewById(R.id.tvPatientPhone);
        tvVideoStats = findViewById(R.id.tvVideoStats);
        tvMedStats = findViewById(R.id.tvMedStats);
        tvOverdueVideos = findViewById(R.id.tvOverdueVideos);
        tvOverdueMeds = findViewById(R.id.tvOverdueMeds);
        tvOverallProgress = findViewById(R.id.tvOverallProgress);
        progressOverall = findViewById(R.id.progressOverall);
        btnAddVideo = findViewById(R.id.btnAddVideo);

        patientUid = getIntent().getStringExtra("patient_uid");
        patientEmail = getIntent().getStringExtra("patient_email");

        if (patientUid != null) {
            loadPatientData(patientUid, patientEmail);
        } else {
            Log.e("PROFILE", "UID is null!");
        }

        btnAddVideo.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectFolderActivity.class);
            intent.putExtra("patientUid", patientUid);
            intent.putExtra("patientEmail", patientEmail);
            startActivityForResult(intent, 101);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            String folderId = data.getStringExtra("folderId");
            if (folderId != null && patientUid != null) {
                assignFolderToPatient(patientUid, folderId);
                saveFolderIdToPatient(patientUid, folderId);
            }
        }
    }

    private void saveFolderIdToPatient(String patientUid, String folderId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("folderId", folderId);

        db.collection("Patient").document(patientUid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Папка успешно назначена в профиль пациента!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка при сохранении папки пациенту: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void assignFolderToPatient(String patientUid, String folderId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String documentId = patientUid + "_" + folderId;

        Map<String, Object> data = new HashMap<>();
        data.put("patientUid", patientUid);
        data.put("folderId", folderId);
        data.put("assignedAt", System.currentTimeMillis());

        db.collection("patient_folders").document(documentId)
                .set(data)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Папка успешно назначена пациенту!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка при назначении папки", Toast.LENGTH_SHORT).show());
    }

    private void loadPatientData(String uid, String emailFromIntent) {
        DocumentReference ref = FirebaseFirestore.getInstance().collection("Patient").document(uid);
        ref.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String firstName = doc.getString("firstName");
                String lastName = doc.getString("lastName");
                String name = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                String phone = doc.getString("tel");
                String email = doc.getString("email");

                tvName.setText(name.trim().isEmpty() ? "—" : name.trim());
                tvEmail.setText(email != null ? email : "—");
                tvPhone.setText(phone != null ? phone : "—");

                String imageId;
                try {
                    imageId = URLEncoder.encode(patientUid, "UTF-8") + ".jpg";
                } catch (UnsupportedEncodingException e) {
                    imageId = patientUid + ".jpg";
                }

                String url = "https://firebasestorage.googleapis.com/v0/b/rehabcare-fc585.appspot.com/o/PatientProfile%2F" + imageId + "?alt=media";
                Picasso.get().load(url).placeholder(R.drawable.ic_account).into(ivPatientPhoto);

                loadVideoProgress(uid);
                loadMedProgress(uid);
                loadOverdueStats(uid);
                loadOverallProgress(uid);
            } else {
                Log.d("PROFILE", "No patient found for uid: " + uid);
            }
        }).addOnFailureListener(e -> Log.e("PROFILE", "Error loading patient", e));
    }

    private void loadOverallProgress(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("video_progress").document(uid).get().addOnSuccessListener(videoDoc -> {
            long watched = videoDoc.getLong("watchedCount") != null ? videoDoc.getLong("watchedCount") : 0;
            long totalVideos = videoDoc.getLong("totalVideos") != null ? videoDoc.getLong("totalVideos") : 0;
            db.collection("med_progress").document(uid).get().addOnSuccessListener(medDoc -> {
                long taken = medDoc.getLong("taken") != null ? medDoc.getLong("taken") : 0;
                long totalMeds = medDoc.getLong("total") != null ? medDoc.getLong("total") : 0;

                float percentVideos = totalVideos > 0 ? (watched * 1f / totalVideos) : 1f;
                float percentMeds = totalMeds > 0 ? (taken * 1f / totalMeds) : 1f;
                int percent = Math.round(((percentVideos + percentMeds) / 2f) * 100);

                progressOverall.setProgress(percent);
                tvOverallProgress.setText(percent + "%");
            });
        });
    }

    private void loadOverdueStats(String uid) {
        FirebaseFirestore.getInstance().collection("video_overdue").document(uid).get()
                .addOnSuccessListener(doc -> {
                    Long overdue = doc.getLong("count");
                    tvOverdueVideos.setText("Просроченных видео: " + (overdue != null ? overdue : 0));
                }).addOnFailureListener(e -> {
                    tvOverdueVideos.setText("Просроченных видео: —");
                    Log.e("PROFILE", "Ошибка чтения просроченных видео", e);
                });

        FirebaseFirestore.getInstance().collection("med_overdue").document(uid).get()
                .addOnSuccessListener(doc -> {
                    Long overdue = doc.getLong("count");
                    tvOverdueMeds.setText("Пропущенных лекарств: " + (overdue != null ? overdue : 0));
                }).addOnFailureListener(e -> {
                    tvOverdueMeds.setText("Пропущенных лекарств: —");
                    Log.e("PROFILE", "Ошибка чтения просроченных лекарств", e);
                });
    }

    private void loadMedProgress(String uid) {
        FirebaseFirestore.getInstance().collection("med_progress").document(uid).get()
                .addOnSuccessListener(doc -> {
                    Long taken = doc.getLong("taken");
                    Long total = doc.getLong("total");
                    long takenCount = taken != null ? taken : 0;
                    long totalCount = total != null ? total : 0;
                    tvMedStats.setText(takenCount + "/" + totalCount);
                }).addOnFailureListener(e -> {
                    tvMedStats.setText("—");
                    Log.e("PROFILE", "Ошибка чтения прогресса лекарств", e);
                });
    }

    private void loadVideoProgress(String uid) {
        FirebaseFirestore.getInstance().collection("video_progress").document(uid).get()
                .addOnSuccessListener(doc -> {
                    Long watched = doc.getLong("watchedCount");
                    Long total = doc.getLong("totalVideos");
                    long watchedCount = watched != null ? watched : 0;
                    long totalCount = total != null ? total : 0;
                    tvVideoStats.setText(watchedCount + "/" + totalCount);
                }).addOnFailureListener(e -> {
                    tvVideoStats.setText("—");
                    Log.e("PROFILE", "Ошибка чтения прогресса видео", e);
                });
    }
}
