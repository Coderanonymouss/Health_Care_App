package com.ensias.healthcareapp.patient.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ensias.healthcareapp.R;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import dmax.dialog.SpotsDialog;

public class ProfileDoctorInPatientActivity extends AppCompatActivity {

    private MaterialTextView doctorName, doctorSpe, doctorPhone, doctorEmail, doctorAddress, doctorAbout;
    private ImageView doctorImage;
    private String doctorUid,doctorRole;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_doctor);

        doctorUid = getIntent().getStringExtra("userUid");
        doctorRole = getIntent().getStringExtra("companionRole");
        if (doctorUid == null || doctorUid.isEmpty()) {
            finish();
            return;
        }
        // Лог для проверки
        android.util.Log.d("ProfileDoctor", "Открываю профиль доктора с UID: " + doctorUid);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        // UI Elements
        doctorImage = findViewById(R.id.imageView3);
        doctorName = findViewById(R.id.doctor_name);
        doctorSpe = findViewById(R.id.doctor_specialite);
        doctorPhone = findViewById(R.id.doctor_phone);
        doctorEmail = findViewById(R.id.doctor_email);
        doctorAddress = findViewById(R.id.doctor_address);
        doctorAbout = findViewById(R.id.doctor_about);
        ImageView backButton = findViewById(R.id.backButton);
        ImageView editIcon = findViewById(R.id.editIcon);

        // Прячем кнопку редактирования!
        editIcon.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> finish());

        AlertDialog dialog = new SpotsDialog.Builder().setContext(this).setCancelable(true).build();
        dialog.show();

        StorageReference pathReference = FirebaseStorage.getInstance().getReference()
                .child("DoctorProfile/" + doctorUid + ".jpg");

        pathReference.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Picasso.get()
                            .load(uri)
                            .placeholder(R.drawable.ic_user_placeholder)
                            .fit()
                            .centerCrop()
                            .into(doctorImage);
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    doctorImage.setImageResource(R.drawable.ic_user_placeholder);
                    dialog.dismiss();
                });

        DocumentReference docRef = db.collection("Doctor").document(doctorUid);
        docRef.addSnapshotListener(this, (documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                doctorName.setText(getSafeValue(documentSnapshot.getString("fullName")));
                doctorSpe.setText(getSafeValue(documentSnapshot.getString("specialite")));
                doctorPhone.setText(getSafeValue(documentSnapshot.getString("tel")));
                doctorEmail.setText(getSafeValue(documentSnapshot.getString("email")));
                doctorAddress.setText(getSafeValue(documentSnapshot.getString("adresse")));
                doctorAbout.setText(getSafeValue(documentSnapshot.getString("about")));
            }
        });
    }

    private String getSafeValue(String value) {
        return value != null ? value : "";
    }
}

