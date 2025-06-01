package com.ensias.healthcareapp.patient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.ensias.healthcareapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.Objects;
import dmax.dialog.SpotsDialog;

public class ProfilePatientActivity extends AppCompatActivity {

    private TextView patientName, patientPhone, patientEmail, patientAddress, patientAbout;
    private ImageView patientImage;

    private String patientID;

    @SuppressLint({"UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        // Получаем email профиля пациента
        patientID = getIntent().getStringExtra("patient_uid");
        if (patientID == null) {
            patientID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        }
        String currentUserEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // View init
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        patientName = findViewById(R.id.patient_full_name);
        patientPhone = findViewById(R.id.patient_phone_value);
        patientEmail = findViewById(R.id.patient_email_value);
        patientAddress = findViewById(R.id.patient_address_value);
        patientImage = findViewById(R.id.imageView3);
        ImageView editIcon = findViewById(R.id.editIcon); // Теперь editIcon есть!

        Drawable defaultImage = getResources().getDrawable(R.drawable.ic_user_placeholder);

        AlertDialog dialog = new SpotsDialog.Builder().setContext(this).setCancelable(true).build();
        dialog.show();

        // Фото профиля
        StorageReference pathReference = FirebaseStorage.getInstance()
                .getReference().child("PatientProfile/" + patientID + ".jpg");
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

        // Получаем поля профиля из Firestore

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("Patient").document(patientID);
        docRef.addSnapshotListener(this, (documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                String firstName = documentSnapshot.getString("firstName");
                String lastName = documentSnapshot.getString("lastName");
                String middleName = documentSnapshot.getString("middleName");
                StringBuilder nameBuilder = new StringBuilder();
                if (firstName != null) nameBuilder.append(firstName).append(" ");
                if (middleName != null && !middleName.isEmpty() && !"null".equals(middleName)) nameBuilder.append(middleName).append(" ");
                if (lastName != null) nameBuilder.append(lastName);
                String fullName = nameBuilder.toString().trim();
                patientName.setText(fullName);

                patientPhone.setText(documentSnapshot.getString("tel"));
                patientEmail.setText(documentSnapshot.getString("email"));
                patientAddress.setText(documentSnapshot.getString("address"));
                String about = documentSnapshot.getString("about");
            }
        });

        // Кнопка "редактировать" — видна только если профиль свой
        if (patientID.equals(currentUserEmail)) {
            editIcon.setVisibility(ImageView.VISIBLE);
            editIcon.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditProfilePatientActivity.class);
                intent.putExtra("patient_uid", patientID);
                startActivity(intent);
            });
        } else {
            editIcon.setVisibility(ImageView.GONE);
        }
    }
}
