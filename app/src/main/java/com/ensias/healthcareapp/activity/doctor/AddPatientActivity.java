package com.ensias.healthcareapp.activity.doctor;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddPatientActivity extends AppCompatActivity {

    private EditText firstNameField, lastNameField, middleNameField, emailField;
    private Button addBtn;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        firstNameField = findViewById(R.id.input_patient_first_name);
        lastNameField = findViewById(R.id.input_patient_last_name);
        middleNameField = findViewById(R.id.input_patient_middle_name);
        emailField = findViewById(R.id.input_patient_email);
        addBtn = findViewById(R.id.btn_add_patient);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        addBtn.setOnClickListener(v -> addPatient());
    }

    private void addPatient() {
        String firstName = firstNameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();
        String middleName = middleNameField.getText().toString().trim();
        String email = emailField.getText().toString().trim().toLowerCase();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Имя, фамилия и email обязательны", Toast.LENGTH_SHORT).show();
            return;
        }

        // Здесь лучше проверять по email в коллекции User (поиск по полю email), а не по ID документа,
        // так как теперь ID - это uid, а не email
        db.collection("User").whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Пациент с этим email уже существует!", Toast.LENGTH_LONG).show();
                    } else {
                        registerPatient(firstName, lastName, middleName, email);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка проверки существующего пациента: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        db.collection("Patient").whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Пациент с этим email уже существует!", Toast.LENGTH_LONG).show();
                    } else {
                        registerPatient(firstName, lastName, middleName, email);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка проверки существующего пациента: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }


    private void registerPatient(String firstName, String lastName, String middleName, String email) {
        String tempPassword = generateTempPassword();
        FirebaseUser doctor = FirebaseAuth.getInstance().getCurrentUser();
        String doctorEmail = doctor != null ? doctor.getEmail() : "";

        auth.createUserWithEmailAndPassword(email, tempPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser newUser = task.getResult().getUser();

                        if (newUser != null) {
                            // Отправляем письмо сброса пароля
                            auth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(resetTask -> {
                                        if (resetTask.isSuccessful()) {
                                            Toast.makeText(this, "Письмо для установки пароля отправлено пациенту.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(this, "Ошибка отправки письма сброса пароля: " + resetTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });

                            String patientUid = newUser.getUid();

                            // Создаём профиль пациента в Firestore с ID = uid
                            Map<String, Object> patient = new HashMap<>();
                            patient.put("firstName", firstName);
                            patient.put("lastName", lastName);
                            patient.put("middleName", middleName.isEmpty() ? null : middleName);
                            patient.put("email", email);
                            patient.put("doctorId", doctorEmail);
                            patient.put("uid", patientUid);

                            db.collection("Patient").document(patientUid)
                                    .set(patient)
                                    .addOnSuccessListener(aVoid -> {
                                        // Создаём запись пользователя с ролью в коллекции User с ID = uid
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("email", email);
                                        userData.put("type", "Patient");
                                        userData.put("firstSigninCompleted", false);
                                        userData.put("doctorId", doctorEmail);
                                        userData.put("uid", patientUid);

                                        db.collection("User").document(patientUid)
                                                .set(userData)
                                                .addOnSuccessListener(aVoid2 -> {
                                                    Toast.makeText(this, "Пациент добавлен и роль установлена!", Toast.LENGTH_LONG).show();
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Ошибка записи роли: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Ошибка Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Ошибка создания: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
