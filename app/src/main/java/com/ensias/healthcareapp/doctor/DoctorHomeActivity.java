package com.ensias.healthcareapp.doctor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.gridlayout.widget.GridLayout;
import android.widget.TextView;

import com.ensias.healthcareapp.BaseActivity;
import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.SettingsActivity;
import com.ensias.healthcareapp.doctor.patients.AllUsersActivity;
import com.ensias.healthcareapp.doctor.videolesson.DoctorVideoLessonsActivity;
import com.ensias.healthcareapp.doctor.chat.MyPatientsActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DoctorHomeActivity extends  BaseActivity  {


    MaterialCardView cardPatients, cardAppointments, cardCalendar, cardProfile,cardSettings,cardVideos;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_home);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView welcomeText = findViewById(R.id.welcomeTitle);

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
                                welcomeText.setText("Сәлем, доктор!");
                            }
                        } else {
                            welcomeText.setText("Сәлем, доктор!");
                        }
                    })
                    .addOnFailureListener(e -> welcomeText.setText("Сәлем, доктор!"));
        }

        // 🔐 Авторизация
        Common.CurrentUserid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Common.CurrentUserType = "doctor";

        cardVideos = findViewById(R.id.card_videos);
        cardVideos.setOnClickListener(v ->
                startActivity(new Intent(this, DoctorVideoLessonsActivity.class)));
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View card = gridLayout.getChildAt(i);
            card.setAlpha(0f);
            card.setTranslationY(50);
            card.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setDuration(400)
                    .setStartDelay(i * 100)
                    .start();
        }




        // 👨‍⚕️ Пациенттер
        cardPatients = findViewById(R.id.card_patients);
        cardPatients.setOnClickListener(v ->
                startActivity(new Intent(this, MyPatientsActivity.class)));

        // 📅 Қабылдаулар
        cardAppointments = findViewById(R.id.card_appointments);
        cardAppointments.setOnClickListener(v ->
                startActivity(new Intent(this, AddPatientActivity.class)));

        // 🗓 Күнтізбе
        cardCalendar = findViewById(R.id.card_users);
        cardCalendar.setOnClickListener(v ->
                startActivity(new Intent(this, AllUsersActivity.class)));

        // 👤 Профиль
        cardProfile = findViewById(R.id.card_profile);
        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileDoctorActivity.class)));

        cardSettings = findViewById(R.id.card_settings);
        cardSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }
}
