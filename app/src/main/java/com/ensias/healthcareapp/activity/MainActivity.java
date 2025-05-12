package com.ensias.healthcareapp.activity;

import static com.ensias.healthcareapp.fireStoreApi.UserHelper.UsersRef;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.DoctorHomeActivity;
import com.ensias.healthcareapp.activity.FirstSigninActivity;
import com.ensias.healthcareapp.activity.HomeActivity;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("User");

    private EditText emailText, passwordText, confirmPassword;
    private Button loginBtn, createAccountBtn, signUpBtn, resendVerificationBtn;
    private SignInButton googleSignInBtn;
    private GoogleSignInClient googleSignInClient;

    private ProgressBar progressBar;
    private ImageView logoImage;

    TextView resetPasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        // UI elements
        logoImage = findViewById(R.id.logoImage);
        emailText = findViewById(R.id.editText2);
        passwordText = findViewById(R.id.editText);
        confirmPassword = findViewById(R.id.editText3);
        confirmPassword.setVisibility(View.GONE);
        resetPasswordBtn = findViewById(R.id.ResetPasswordBtn);
        progressBar = findViewById(R.id.progressBar);


        loginBtn = findViewById(R.id.LoginBtn);
        signUpBtn = findViewById(R.id.SignUpBtn);
        createAccountBtn = findViewById(R.id.CreateAccount);
        resendVerificationBtn = findViewById(R.id.ResendVerificationBtn);
        resendVerificationBtn.setVisibility(View.GONE);
        resendVerificationBtn.setPaintFlags(resendVerificationBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        googleSignInBtn = findViewById(R.id.sign_in_button);
        TextView googleText = (TextView) googleSignInBtn.getChildAt(0);
        googleText.setText("Sign in with Google");

        // Animation
        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_animation);
        logoImage.startAnimation(logoAnim);

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // сброс пороль
        resetPasswordBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
        });

        // Login
        loginBtn.setOnClickListener(v -> {
            String email = emailText.getText().toString();
            String password = passwordText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Почта мен құпиясөзді енгізіңіз");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                updateUI(user);
                            } else {
                                showToast("Email-ді растаңыз");
                                resendVerificationBtn.setVisibility(View.VISIBLE);
                            }
                        } else {
                            showToast("Кіру сәтсіз");
                        }
                    });
        });

        // Switch to Sign Up
        createAccountBtn.setOnClickListener(v -> {
            boolean creating = createAccountBtn.getText().toString().equals("Create Account");
            confirmPassword.setVisibility(creating ? View.VISIBLE : View.GONE);
            signUpBtn.setVisibility(creating ? View.VISIBLE : View.GONE);
            loginBtn.setVisibility(creating ? View.GONE : View.VISIBLE);
            resendVerificationBtn.setVisibility(View.GONE);
            googleSignInBtn.setVisibility(creating ? View.GONE : View.VISIBLE);
            createAccountBtn.setText(creating ? "Back to login" : "Create Account");
        });

        // Register new user
        createAccountBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        // Resend verification
        resendVerificationBtn.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && !user.isEmailVerified()) {
                user.sendEmailVerification().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Хат қайта жіберілді");
                    } else {
                        showToast("Қате орын алды");
                    }
                });
            }
        });

        // Google Sign-in
        googleSignInBtn.setOnClickListener(v -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) updateUI(user);
            });
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            } else {
                Snackbar.make(findViewById(R.id.main_layout), "Google арқылы кіру сәтсіз", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                firebaseAuthWithGoogle(task.getResult(ApiException.class));
            } catch (ApiException e) {
                Log.w("TAG", "Google кіру қатесі", e);
            }
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser == null) return;

        progressBar.setVisibility(View.VISIBLE);

        usersRef.document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            progressBar.setVisibility(View.GONE);

            if (documentSnapshot.exists()) {
                Boolean firstCompleted = documentSnapshot.getBoolean("firstSigninCompleted");
                if (firstCompleted != null && firstCompleted) {
                    String role = documentSnapshot.getString("type");
                    if ("Patient".equals(role)) {
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    } else if ("Doctor".equals(role)) {
                        startActivity(new Intent(MainActivity.this, DoctorHomeActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, FirstSigninActivity.class));
                    }
                } else {
                    startActivity(new Intent(MainActivity.this, FirstSigninActivity.class));
                }
            } else {
                startActivity(new Intent(MainActivity.this, FirstSigninActivity.class));
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
            showToast("Профиль оқу қатесі");
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
