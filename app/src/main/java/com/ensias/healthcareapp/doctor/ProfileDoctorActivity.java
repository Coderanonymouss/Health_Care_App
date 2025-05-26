package com.ensias.healthcareapp.doctor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.doctor.EditProfileDoctorActivity;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class ProfileDoctorActivity extends AppCompatActivity {

    private MaterialTextView doctorName, doctorSpe, doctorPhone, doctorEmail, doctorAddress, doctorAbout;
    private ImageView doctorImage, backButton, editIcon;

    private final String doctorID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final DocumentReference docRef = db.collection("Doctor").document(doctorID);
    private StorageReference pathReference;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_doctor);

        // 🔧 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 🔄 UI Elements
        doctorImage = findViewById(R.id.imageView3);
        doctorName = findViewById(R.id.doctor_name);
        doctorSpe = findViewById(R.id.doctor_specialite);
        doctorPhone = findViewById(R.id.doctor_phone);
        doctorEmail = findViewById(R.id.doctor_email);
        doctorAddress = findViewById(R.id.doctor_address);
        doctorAbout = findViewById(R.id.doctor_about);
        backButton = findViewById(R.id.backButton);
        editIcon = findViewById(R.id.editIcon);

        // 🔙 Назад
        backButton.setOnClickListener(v -> {
            startHomeActivity();
        });

        // ✏️ Редактировать
        editIcon.setOnClickListener(v -> {
            startEditActivity();
        });

        // 🔄 Загрузка с Firebase
        AlertDialog dialog = new SpotsDialog.Builder().setContext(this).setCancelable(true).build();
        dialog.show();

        // Фото
        pathReference = FirebaseStorage.getInstance().getReference()
                .child("DoctorProfile/" + doctorID + ".jpg");

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

        // Данные
        docRef.addSnapshotListener(this, (documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                String photoUrl = getSafeValue(documentSnapshot.getString("photoUrl"));
                if (!photoUrl.isEmpty()) {
                    Picasso.get()
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_user_placeholder)
                            .fit()
                            .centerCrop()
                            .into(doctorImage);
                } else {
                    doctorImage.setImageResource(R.drawable.ic_user_placeholder);
                }
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

    private void startHomeActivity() {
        Intent intent = new Intent(this, DoctorHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void startEditActivity() {
        Intent intent = new Intent(this, EditProfileDoctorActivity.class);
        intent.putExtra("CURRENT_NAME", doctorName.getText().toString());
        intent.putExtra("CURRENT_PHONE", doctorPhone.getText().toString());
        intent.putExtra("CURRENT_ADDRESS", doctorAddress.getText().toString());
        startActivity(intent);
        // можно добавить: overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
