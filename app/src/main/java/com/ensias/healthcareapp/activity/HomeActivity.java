package com.ensias.healthcareapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.DossierMedical;
import com.ensias.healthcareapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;

public class HomeActivity extends AppCompatActivity {

    MaterialButton signOutBtn;
    MaterialCardView cardMyDoctors, cardMedicines, cardVideoLesson,
            cardProfile, cardSearch, cardDossier;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        TextView welcomeText = findViewById(R.id.welcomeTitle);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null) {
                                welcomeText.setText("Сәлем, " + fullName + "!");
                            } else {
                                welcomeText.setText("Сәлем, қолданушы!");
                            }
                        } else {
                            welcomeText.setText("Сәлем, қолданушы!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        welcomeText.setText("Сәлем, қолданушы!");
                    });
        }


        // 🔐 Авторизация
        Common.CurrentUserid = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        FirebaseFirestore.getInstance().collection("User")
                .document(Common.CurrentUserid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Common.CurrentUserName = documentSnapshot.getString("name");
                });

        // 🟢 Кнопка "Шығу"
        signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // 🩺 "Дәрігерлерім"
        cardMyDoctors = findViewById(R.id.card_myDoctors);
        cardMyDoctors.setOnClickListener(v ->
                startActivity(new Intent(this, MyDoctorsAvtivity.class)));

        // 💊 "Дәрілерім"
        cardMedicines = findViewById(R.id.card_medicines);
        cardMedicines.setOnClickListener(v ->
                startActivity(new Intent(this, MedicinesActivity.class)));

        // 🎥 "Бейнесабақ"
        cardVideoLesson = findViewById(R.id.card_videoLesson);
        cardVideoLesson.setOnClickListener(v ->
                startActivity(new Intent(this, PatientVideoLessonActivity.class)));

        // 👤 "Профиль"
        cardProfile = findViewById(R.id.card_profile);
        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfilePatientActivity.class)));

        // 🔍 "Іздеу"
        cardSearch = findViewById(R.id.card_search);
        cardSearch.setOnClickListener(v ->
                startActivity(new Intent(this, SearchPatActivity.class)));

        // 📁 "Жазбалар"
        cardDossier = findViewById(R.id.card_dossier);
        cardDossier.setOnClickListener(v -> {
            Intent intent = new Intent(this, DossierMedical.class);
            intent.putExtra("patient_email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
            startActivity(intent);
        });
    }
}
