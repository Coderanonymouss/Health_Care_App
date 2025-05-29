package com.ensias.healthcareapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.doctor.DoctorHomeActivity;
import com.ensias.healthcareapp.patient.HomeActivity;
import com.ensias.healthcareapp.admin.AdminPanelActivity;
import com.ensias.healthcareapp.patient.FirstSigninPatientActivity;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

/**
 * Негізгі экран (логин және тіркелу) класы.
 * Пайдаланушыны аутентификациялап, рөліне сәйкес экранға бағыттайды.
 */
public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100; // Google Sign-in үшін сұрау коды

    private FirebaseAuth mAuth; // Firebase аутентификация объектісі
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore дерекқоры
    private CollectionReference usersRef = db.collection("User"); // "User" коллекциясы

    private EditText emailText, passwordText, confirmPassword; // Email, құпия сөз және оның қайталануы
    private Button loginBtn,greateAccountBtn; // Батырмалар

    private GoogleSignInClient googleSignInClient; // Google Sign-in клиенті

    private ProgressBar progressBar; // Жүктелу индикаторы
    private ImageView logoImage; // Логотип суреті

    TextView resetPasswordBtn; // Құпия сөзді қалпына келтіру батырмасы

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        logoImage = findViewById(R.id.logoImage);
        emailText = findViewById(R.id.editText2);
        passwordText = findViewById(R.id.editText);
        resetPasswordBtn = findViewById(R.id.ResetPasswordBtn);
        progressBar = findViewById(R.id.progressBar);

        loginBtn = findViewById(R.id.LoginBtn);
        greateAccountBtn = findViewById(R.id.CreateAccount);

        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_animation);
        logoImage.startAnimation(logoAnim);

        resetPasswordBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
        });

        loginBtn.setOnClickListener(v -> {
            String email = emailText.getText().toString();
            String password = passwordText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Почта мен құпиясөзді енгізіңіз");
                return;
            }

            showLoading();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        hideLoading();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                updateUI(user);
                            } else {
                                showToast("Email-ді растаңыз");
                                // больше ничего не делаем
                            }
                        } else {
                            showToast("Кіру сәтсіз");
                        }
                    });
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser != null && fUser.isEmailVerified()) {
            updateUI(fUser); // Если пользователь уже залогинен и email подтвержден, сразу направляем
        }
    }


    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        // Можно дополнительно отключить взаимодействие с UI
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        // Включаем взаимодействие с UI обратно
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    /**
     * Пайдаланушының деректерін Firestore-дан алып, рөлі мен бірінші кіру мәртебесіне қарай бағыттау жасайды
     * @param currentUser ағымдағы FirebaseUser
     */
    private void updateUI(FirebaseUser currentUser) {
        if (currentUser == null) return;
        showLoading();
        progressBar.setVisibility(View.VISIBLE);

        usersRef.document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            hideLoading();

            if (documentSnapshot.exists()) {
                Boolean firstCompleted = documentSnapshot.getBoolean("firstSigninCompleted"); // Бірінші кіру аяқталды ма
                String role = documentSnapshot.getString("type"); // Пайдаланушы рөлі

                Log.d("MainActivity", "Loaded user role: " + role);
                Log.d("MainActivity", "firstSigninCompleted: " + firstCompleted);

                Intent intent = getRedirectIntent(firstCompleted, role);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // Егер профиль жоқ болса, бірінші кіру экранына бағыттау
                Intent intent = new Intent(MainActivity.this, FirstSigninActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(e -> {
            hideLoading();
            e.printStackTrace();
            showToast("Профиль оқу қатесі");
        });
    }

    /**
     * Рөл мен бірінші кіру мәртебесіне қарай тиісті экранға бағыттау үшін Intent дайындау
     * @param firstCompleted бірінші кіру аяқталған ба
     * @param role пайдаланушы рөлі ("Patient", "Doctor", "Admin")
     * @return бағыттайтын Intent
     */
    private Intent getRedirectIntent(Boolean firstCompleted, String role) {
        Intent intent;

        if ("Patient".equals(role)) {
            if (Boolean.TRUE.equals(firstCompleted)) {
                intent = new Intent(MainActivity.this, HomeActivity.class); // Пациенттің басты экраны
            } else {
                intent = new Intent(MainActivity.this, FirstSigninPatientActivity.class); // Бірінші кіру экраны (пациент)
            }
        } else if ("Doctor".equals(role)) {
            if (Boolean.TRUE.equals(firstCompleted)) {
                intent = new Intent(MainActivity.this, DoctorHomeActivity.class); // Врач басты экраны
            } else {
                intent = new Intent(MainActivity.this, FirstSigninActivity.class); // Бірінші кіру (врач)
            }
        } else if ("Admin".equals(role)) {
            if (Boolean.TRUE.equals(firstCompleted)) {
                intent = new Intent(MainActivity.this, AdminPanelActivity.class); // Админ панелі
            } else {
                intent = new Intent(MainActivity.this, FirstSigninActivity.class); // Бірінші кіру (админ)
            }
        } else {
            intent = new Intent(MainActivity.this, FirstSigninActivity.class); // Белгісіз рөл, бірінші кіру
        }

        return intent;
    }

    /**
     * Қарапайым Toast хабарламасын көрсету әдісі
     * @param msg Хабарлама мәтіні
     */
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
