package com.ensias.healthcareapp.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FirstSigninActivity extends AppCompatActivity {

    private TextInputEditText fullName, birthday, tel;
    private Spinner spinner, specialiteSpinner;
    private Button confirmBtn;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_signin);

        fullName = findViewById(R.id.firstSignFullName);
        birthday = findViewById(R.id.firstSignBirthDay);
        tel = findViewById(R.id.firstSignTel);
        spinner = findViewById(R.id.spinner);
        specialiteSpinner = findViewById(R.id.specialite_spinner);
        confirmBtn = findViewById(R.id.confirmeBtn);

        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Установка адаптера для ролей
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Адаптер для специальностей
        ArrayAdapter<CharSequence> adapterSpecialite = ArrayAdapter.createFromResource(
                this, R.array.specialite_spinner, android.R.layout.simple_spinner_item);
        adapterSpecialite.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        specialiteSpinner.setAdapter(adapterSpecialite);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String role = spinner.getSelectedItem().toString();
                specialiteSpinner.setVisibility(role.equals("Doctor") ? android.view.View.VISIBLE : android.view.View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        birthday.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    FirstSigninActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                        birthday.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        confirmBtn.setOnClickListener(v -> {
            String name = fullName.getText().toString().trim();
            String birth = birthday.getText().toString().trim();
            String phone = tel.getText().toString().trim();
            String role = spinner.getSelectedItem().toString();
            String specialite = specialiteSpinner.getSelectedItem().toString();

            if (name.isEmpty() || birth.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Барлық өрісті толтырыңыз", Toast.LENGTH_SHORT).show();
                return;
            }

            if (firebaseUser != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("fullName", name);
                userData.put("birthday", birth);
                userData.put("tel", phone);
                userData.put("type", role);
                userData.put("firstSigninCompleted", true);
                if (role.equals("Doctor")) {
                    userData.put("specialite", specialite);
                }

                db.collection("User").document(firebaseUser.getUid())
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Анкета сақталды", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Сақтау кезінде қате болды", Toast.LENGTH_SHORT).show();
                            Log.e("Firestore", "Error saving user data", e);
                        });
            } else {
                Toast.makeText(this, "Қолданушы табылмады", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
