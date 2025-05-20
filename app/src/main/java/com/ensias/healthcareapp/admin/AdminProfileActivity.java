package com.ensias.healthcareapp.admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminProfileActivity extends AppCompatActivity {

    private TextView textFullName, textEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);
        setTitle("Профиль");

        textFullName = findViewById(R.id.textFullName);
        textEmail = findViewById(R.id.textEmail);

        loadProfile();
    }

    private void loadProfile() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("User")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        textFullName.setText(fullName != null ? fullName : "Белгісіз");
                        textEmail.setText(email != null ? email : "Белгісіз");
                    } else {
                        Toast.makeText(this, "Профиль табылмады", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
