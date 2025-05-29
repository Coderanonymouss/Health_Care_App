package com.ensias.healthcareapp.patient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.DossierMedical;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.MainActivity;
import com.ensias.healthcareapp.activity.SearchPatActivity;
import com.ensias.healthcareapp.activity.patient.ProfilePatientActivity;
import com.ensias.healthcareapp.patient.medicine.MedicinesActivity;
import com.ensias.healthcareapp.patient.chat.MyDoctorsActivity;
import com.ensias.healthcareapp.patient.progress.PatientAnalyticsActivity;
import com.ensias.healthcareapp.patient.search.AIChatActivity;
import com.ensias.healthcareapp.patient.videolesson.PatientVideoListActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Бұл класс - Пациенттің басты (үй) экраны.
 * Мұнда қолданушы өзінің дәрігерлеріне, бейнесабақтарға, профильге, т.б. өтуге мүмкіндік алады.
 */
public class HomeActivity extends AppCompatActivity {

    // Шығу батырмасы
    MaterialButton signOutBtn;

    // Карточкалар арқылы түрлі функцияларға өту
    MaterialCardView cardMyDoctors, cardMedicines, cardVideoLesson,
            cardProfile, cardSearch, cardDossier;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Қош келдіңіз мәтінін көрсету
        TextView welcomeText = findViewById(R.id.welcomeTitle);

        // 🔐 Қолданушыны тексеру және атымен қарсы алу
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore.getInstance()
                    .collection("Patient")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("firstName");
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

        // 🔄 Ағымдағы қолданушы туралы мәліметтерді жүктеу
        Common.CurrentUserid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore.getInstance().collection("User")
                .document(Common.CurrentUserid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Common.CurrentUserName = documentSnapshot.getString("fullName");
                });

        // 🔘 Шығу батырмасы (сессияны жабу)
        signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Барлық алдыңғы экрандарды жояды
            startActivity(intent);
            finish();
        });

        // 🩺 Менің дәрігерлерім (MyDoctorsActivity)
        cardMyDoctors = findViewById(R.id.card_myDoctors);
        cardMyDoctors.setOnClickListener(v ->
                startActivity(new Intent(this, MyDoctorsActivity.class)));

        // 💊 Дәрі-дәрмектер тізімі
        cardMedicines = findViewById(R.id.card_medicines);
        cardMedicines.setOnClickListener(v ->
                startActivity(new Intent(this, MedicinesActivity.class)));

        // 🎥 Бейнесабақтарға өту
        cardVideoLesson = findViewById(R.id.card_videoLesson);
        cardVideoLesson.setOnClickListener(v ->
                startActivity(new Intent(this, PatientVideoListActivity.class)));

        // 👤 Пайдаланушы профилі
        cardProfile = findViewById(R.id.card_profile);
        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfilePatientActivity.class)));

        // 🔍 Дәрігерлерді немесе басқа пациенттерді іздеу
        cardSearch = findViewById(R.id.card_search);
        cardSearch.setOnClickListener(v ->
                startActivity(new Intent(this, AIChatActivity.class)));

        // 📁 Медициналық жазбалар (DossierMedical)
        cardDossier = findViewById(R.id.card_dossier);
        cardDossier.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientAnalyticsActivity.class);
            intent.putExtra("patient_email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
            startActivity(intent);
        });
    }
}
