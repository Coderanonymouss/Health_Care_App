package com.ensias.healthcareapp.activity.doctor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.gridlayout.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DoctorHomeActivity extends AppCompatActivity {

    MaterialButton signOutBtn;
    MaterialCardView cardPatients, cardAppointments, cardCalendar, cardProfile, cardVideos;

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


        // 🟢 Кнопка "Шығу"
        signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // 👨‍⚕️ Пациенттер
        cardPatients = findViewById(R.id.card_patients);
        cardPatients.setOnClickListener(v ->
                startActivity(new Intent(this, MyPatientsActivity.class)));

        // 📅 Қабылдаулар
        cardAppointments = findViewById(R.id.card_appointments);
        cardAppointments.setOnClickListener(v ->
                startActivity(new Intent(this, DoctorAppointementActivity.class)));

        // 🗓 Күнтізбе
        cardCalendar = findViewById(R.id.card_calendar);
        cardCalendar.setOnClickListener(v ->
                startActivity(new Intent(this, MyCalendarDoctorActivity.class)));

        // 👤 Профиль
        cardProfile = findViewById(R.id.card_profile);
        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileDoctorActivity.class)));
    }
}
