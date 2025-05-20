package com.ensias.healthcareapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminPanelActivity extends AppCompatActivity {

    MaterialButton signOutBtn;
    MaterialCardView cardManageDoctors, cardManageUsers, cardSettings, cardReports, cardProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        cardManageDoctors = findViewById(R.id.card_manage_doctors);
        cardManageUsers = findViewById(R.id.card_manage_users);
        cardSettings = findViewById(R.id.card_settings);
        cardReports = findViewById(R.id.card_reports);
        cardProfile = findViewById(R.id.card_profile);

        cardManageDoctors.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, ManageDoctorsActivity.class);
            startActivity(intent);
        });

        cardManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });

        cardSettings.setOnClickListener(v -> {
            // Можно открыть настройки админа (пустая активность для примера)
            Intent intent = new Intent(AdminPanelActivity.this, AdminSettingsActivity.class);
            startActivity(intent);
        });

        cardReports.setOnClickListener(v -> {
            // Аналогично для отчетов
            Intent intent = new Intent(AdminPanelActivity.this, AdminReportsActivity.class);
            startActivity(intent);
        });

        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, AdminProfileActivity.class);
            startActivity(intent);
        });

        signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminPanelActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Анимация карточек (как в DoctorHomeActivity)
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
    }
}

