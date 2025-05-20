package com.ensias.healthcareapp.admin.adminLogin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminSetupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Button createAdminBtn;

    private final String ADMIN_EMAIL = "densaulykqamqorshysy@gmail.com"; // обязательно валидный email
    private final String ADMIN_PASSWORD = "qamqor2025";

    private static final String PREFS_NAME = "AdminPrefs";
    private static final String KEY_ADMIN_CREATED = "adminCreated";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_setup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        createAdminBtn = findViewById(R.id.btn_create_admin);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean adminCreated = prefs.getBoolean(KEY_ADMIN_CREATED, false);

        if (adminCreated) {
            // Админ уже создан — скрываем кнопку и показываем сообщение
            createAdminBtn.setVisibility(View.GONE);
            Toast.makeText(this, "Админ уже создан", Toast.LENGTH_LONG).show();
        } else {
            createAdminBtn.setOnClickListener(v -> createAdminUser());
        }
    }

    private void createAdminUser() {
        auth.createUserWithEmailAndPassword(ADMIN_EMAIL, ADMIN_PASSWORD)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            Map<String, Object> adminData = new HashMap<>();
                            adminData.put("email", ADMIN_EMAIL);
                            adminData.put("type", "admin");
                            adminData.put("fullName", "Администратор");

                            db.collection("User").document(uid)
                                    .set(adminData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AdminSetupActivity.this, "Админ создан!", Toast.LENGTH_LONG).show();
                                        Log.d("AdminSetup", "Admin user created in Firestore");

                                        // Сохраняем в SharedPreferences, что админ создан
                                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                                .edit()
                                                .putBoolean(KEY_ADMIN_CREATED, true)
                                                .apply();

                                        createAdminBtn.setVisibility(View.GONE);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AdminSetupActivity.this, "Ошибка Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(AdminSetupActivity.this, "Ошибка создания: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
