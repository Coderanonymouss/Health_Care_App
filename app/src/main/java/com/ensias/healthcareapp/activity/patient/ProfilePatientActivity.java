package com.ensias.healthcareapp.activity.patient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.ensias.healthcareapp.R;
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

public class ProfilePatientActivity extends AppCompatActivity {

    private MaterialTextView patientName;
    private MaterialTextView patientPhone;
    private MaterialTextView patientEmail;
    private MaterialTextView patientAddress;
    private MaterialTextView patientAbout;
    private ImageView patientImage;

    private final String patientID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final DocumentReference docRef;

    {
        assert patientID != null;
        docRef = db.collection("Patient").document(patientID);
    }

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info); // или создай activity_profile_patient

        // 🔄 View init
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView backButton = findViewById(R.id.backButton);
        ImageView editIcon = findViewById(R.id.editIcon);

        patientName = findViewById(R.id.doctor_name);
        patientPhone = findViewById(R.id.doctor_phone);
        patientEmail = findViewById(R.id.doctor_email);
        patientAddress = findViewById(R.id.doctor_address);
        patientAbout = findViewById(R.id.doctor_about);
        patientImage = findViewById(R.id.imageView3);
        @SuppressLint("UseCompatLoadingForDrawables") Drawable defaultImage = getResources().getDrawable(R.drawable.ic_user_placeholder);

        AlertDialog dialog = new SpotsDialog.Builder().setContext(this).setCancelable(true).build();
        dialog.show();

        // 🔙 Назад
        backButton.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(ProfilePatientActivity.this, HomeActivity.class));
        });

        // ✏️ Редактировать
        editIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePatientActivity.this, EditProfilePatientActivity.class);
            intent.putExtra("CURRENT_NAME", patientName.getText().toString());
            intent.putExtra("CURRENT_PHONE", patientPhone.getText().toString());
            intent.putExtra("CURRENT_ADDRESS", patientAddress.getText().toString());
            startActivity(intent);
        });

        // 🖼️ Загрузка фото из Firebase
        String imageId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        StorageReference pathReference = FirebaseStorage.getInstance().getReference().child("PatientProfile/" + imageId + ".jpg");

        pathReference.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Picasso.get()
                            .load(uri)
                            .placeholder(R.drawable.ic_user_placeholder)
                            .fit()
                            .centerCrop()
                            .into(patientImage);
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    patientImage.setImageDrawable(defaultImage);
                    dialog.dismiss();
                });

        // 🧾 Загрузка данных пациента
        docRef.addSnapshotListener(this, (documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                patientName.setText(documentSnapshot.getString("fullName"));
                patientPhone.setText(documentSnapshot.getString("tel"));
                patientEmail.setText(documentSnapshot.getString("email"));
                patientAddress.setText(documentSnapshot.getString("adresse"));
                patientAbout.setText(documentSnapshot.getString("about"));
            }
        });

        // 🧭 Toolbar кастомный
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void startEditActivity() {
        Intent intent = new Intent(this, EditProfilePatientActivity.class);
        intent.putExtra("CURRENT_NAME", patientName.getText().toString());
        intent.putExtra("CURRENT_PHONE", patientPhone.getText().toString());
        intent.putExtra("CURRENT_ADDRESS", patientAddress.getText().toString());
        startActivity(intent);
    }
}
