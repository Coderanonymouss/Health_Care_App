package com.ensias.healthcareapp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.ensias.healthcareapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button resetButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.email_input);
        resetButton = findViewById(R.id.reset_btn);
        progressBar = findViewById(R.id.progress_bar);

        resetButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Email енгізіңіз", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Қалпына келтіру сілтемесі жіберілді", Toast.LENGTH_LONG).show();
                            finish(); // Вернуться назад после успеха
                        } else {
                            Toast.makeText(this, "Қате орын алды. Email дұрыс па?", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
