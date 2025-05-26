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

/**
 * AdminPanelActivity — әкімші интерфейсін басқаратын негізгі экран.
 * Бұл экран арқылы әкімші дәрігерлерді, қолданушыларды, есептерді,
 * баптауларды және өзінің профилін басқара алады.
 */
public class AdminPanelActivity extends AppCompatActivity {

    // Шығу батырмасы
    MaterialButton signOutBtn;

    // Әкімшілік басқару карталары (бөлімдер)
    MaterialCardView cardManageDoctors, cardManageUsers, cardSettings, cardReports, cardProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        // Карталарды layout-тан байланыстыру
        cardManageDoctors = findViewById(R.id.card_manage_doctors);
        cardManageUsers = findViewById(R.id.card_manage_users);
        cardSettings = findViewById(R.id.card_settings);
        cardReports = findViewById(R.id.card_reports);
        cardProfile = findViewById(R.id.card_profile);

        // "Дәрігерлерді басқару" бөліміне өту
        cardManageDoctors.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, ManageDoctorsActivity.class);
            startActivity(intent);
        });

        // "Қолданушыларды басқару" бөліміне өту
        cardManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });

        // "Баптаулар" бөліміне өту
        cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, AdminSettingsActivity.class);
            startActivity(intent);
        });

        // "Есептер" бөліміне өту
        cardReports.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, AdminReportsActivity.class);
            startActivity(intent);
        });

        // "Профиль" бөліміне өту
        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, AdminProfileActivity.class);
            startActivity(intent);
        });

        // Шығу батырмасын layout-тан байланыстыру
        signOutBtn = findViewById(R.id.signOutBtn);

        // Firebase арқылы жүйеден шығу және басты экранға қайта бағыттау
        signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminPanelActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // алдыңғы экрандарды тазалау
            startActivity(intent);
            finish(); // ағымдағы экранды жабу
        });

        // GridLayout-тағы карталарға анимация қосу (DoctorHomeActivity үлгісі бойынша)
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View card = gridLayout.getChildAt(i);
            card.setAlpha(0f); // бастапқы мөлдірлік
            card.setTranslationY(50); // бастапқы орын ауыстыру
            card.animate()
                    .alpha(1f) // соңғы мөлдірлік
                    .translationY(0) // соңғы орын
                    .setDuration(400) // анимация уақыты
                    .setStartDelay(i * 100) // әр карточкаға кідіріс
                    .start();
        }
    }
}
