package com.ensias.healthcareapp.activity.doctor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ensias.healthcareapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddPatientActivity extends AppCompatActivity {

    private EditText iinField, emailField;
    private Button addBtn;
    private TextView fioText;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Данные найденного пациента
    private String foundIIN = "";
    private String foundFirstName = "";
    private String foundLastName = "";
    private String foundMiddleName = "";

    // Новые поля
    private String foundBirthDate = "";
    private String foundGender = "";
    private String foundDiagnosis = "";
    private String foundRehabStage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Пациентті қосу"); // если хочешь динамически

// Если нужен back:
        toolbar.setNavigationOnClickListener(v -> finish());

        iinField = findViewById(R.id.input_patient_iin);
        emailField = findViewById(R.id.input_patient_email);
        addBtn = findViewById(R.id.btn_add_patient);
        fioText = findViewById(R.id.text_patient_fio);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Слушатель для поля ИИН
        iinField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String iin = s.toString().trim();
                if (iin.length() == 12) {
                    checkIIN(iin);
                } else {
                    fioText.setText("");
                    foundIIN = "";
                    foundFirstName = "";
                    foundLastName = "";
                    foundMiddleName = "";
                    foundBirthDate = "";
                    foundGender = "";
                    foundDiagnosis = "";
                    foundRehabStage = "";
                }
            }
        });

        addBtn.setOnClickListener(v -> addPatient());
    }

    // Поиск пациента по ИИН (с твоего backend-сервера, например, Render)
    private void checkIIN(String iin) {
        fioText.setText("Іздеу...");
        String url = "https://health-backend-d1ug.onrender.com/api/patient/" + iin;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> fioText.setText("Серверден қате!"));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    try {
                        JSONObject json = new JSONObject(res);
                        foundIIN = json.getString("iin");
                        foundFirstName = json.optString("firstName", "");
                        foundLastName = json.optString("lastName", "");
                        foundMiddleName = json.has("middleName") && !json.isNull("middleName")
                                ? json.optString("middleName", "") : "";
                        foundBirthDate = json.optString("birthDate", "");
                        foundGender = json.optString("gender", "");
                        foundDiagnosis = json.optString("diagnosis", "");
                        foundRehabStage = json.optString("rehabStage", "");

                        runOnUiThread(() -> {
                            String fio = foundLastName + " " + foundFirstName;
                            if (!TextUtils.isEmpty(foundMiddleName)) fio += " " + foundMiddleName;
                            fioText.setText(fio);
                        });
                    } catch (Exception ex) {
                        runOnUiThread(() -> fioText.setText("Пациент табылмады!"));
                        foundIIN = "";
                        foundFirstName = "";
                        foundLastName = "";
                        foundMiddleName = "";
                        foundBirthDate = "";
                        foundGender = "";
                        foundDiagnosis = "";
                        foundRehabStage = "";
                    }
                } else {
                    runOnUiThread(() -> fioText.setText("Пациент табылмады!"));
                    foundIIN = "";
                    foundFirstName = "";
                    foundLastName = "";
                    foundMiddleName = "";
                    foundBirthDate = "";
                    foundGender = "";
                    foundDiagnosis = "";
                    foundRehabStage = "";
                }
            }
        });
    }

    // Добавление пациента в Firestore (перед этим проверка, что такого пациента нет)
    private void addPatient() {
        String iin = iinField.getText().toString().trim();
        String email = emailField.getText().toString().trim().toLowerCase();

        if (TextUtils.isEmpty(foundIIN) || !iin.equals(foundIIN)) {
            Toast.makeText(this, "Бірінші ЖСН бойынша іздеңіз!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email міндетті", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Patient").whereEqualTo("iin", iin)
                .get()
                .addOnSuccessListener(qs1 -> {
                    if (!qs1.isEmpty()) {
                        Toast.makeText(this, "Пациент осы ЖСН мен бұрын тіркелген!", Toast.LENGTH_LONG).show();
                    } else {
                        db.collection("Patient").whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener(qs2 -> {
                                    if (!qs2.isEmpty()) {
                                        Toast.makeText(this, "Пациент осы email-ен тіркеліп қойған!", Toast.LENGTH_LONG).show();
                                    } else {
                                        registerPatient(iin, email);
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Email іздеу кезінде қате кетті: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "ЖСН іздеу кезінде қате кетті: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void registerPatient(String iin, String email) {
        FirebaseUser doctor = auth.getCurrentUser();
        String doctorUid = doctor != null ? doctor.getUid() : "";
        String doctorId = doctor != null ? doctor.getEmail() : "";
        String tempPassword = generateTempPassword();

        auth.createUserWithEmailAndPassword(email, tempPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser newUser = task.getResult().getUser();
                        if (newUser != null) {
                            // Отправляем письмо для сброса пароля пациенту
                            auth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(resetTask -> {
                                        if (resetTask.isSuccessful()) {
                                            // Теперь сохраняем пациента в Firestore
                                            Map<String, Object> patient = new HashMap<>();
                                            patient.put("iin", iin);
                                            patient.put("firstName", foundFirstName);
                                            patient.put("lastName", foundLastName);
                                            if (!TextUtils.isEmpty(foundMiddleName)) {
                                                patient.put("middleName", foundMiddleName);
                                            }
                                            patient.put("email", email);
                                            patient.put("doctorUid", doctorUid);
                                            patient.put("doctorId", doctorId);
                                            if (!TextUtils.isEmpty(foundBirthDate)) patient.put("birthDate", foundBirthDate);
                                            if (!TextUtils.isEmpty(foundGender)) patient.put("gender", foundGender);
                                            if (!TextUtils.isEmpty(foundDiagnosis)) patient.put("diagnosis", foundDiagnosis);
                                            if (!TextUtils.isEmpty(foundRehabStage)) patient.put("rehabStage", foundRehabStage);

                                            db.collection("Patient").add(patient)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(this, "Пациентіңіз сәтті қосылды! Сілтеме жібердім.", Toast.LENGTH_LONG).show();
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        } else {
                                            Toast.makeText(this, "Хат жіберу мүмкін болмады: " + Objects.requireNonNull(resetTask.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Пациентті құру кезінде қате кетті: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
