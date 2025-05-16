package com.ensias.healthcareapp.activity.doctor;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DoctorUploadVideoActivity extends AppCompatActivity {

    EditText inputEmail, inputTitle, inputUrl;
    Spinner spinnerFunction, spinnerPriority;
    Button btnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        inputEmail = findViewById(R.id.input_patient_email);
        inputTitle = findViewById(R.id.input_title);
        inputUrl = findViewById(R.id.input_url);
        spinnerFunction = findViewById(R.id.spinner_function);
        spinnerPriority = findViewById(R.id.spinner_priority);
        btnUpload = findViewById(R.id.btn_upload);

        setupSpinners();

        btnUpload.setOnClickListener(v -> uploadVideo());
    }

    private void setupSpinners() {
        String[] functions = {"Физио", "Қозғалу", "Тыныс алу", "Психология"};
        String[] priorities = {"жоғары", "орта", "төмен"};

        ArrayAdapter<String> functionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, functions);
        functionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFunction.setAdapter(functionAdapter);

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
    }

    private void uploadVideo() {
        String patientEmail = inputEmail.getText().toString().trim();
        String title = inputTitle.getText().toString().trim();
        String videoUrl = inputUrl.getText().toString().trim();
        String function = spinnerFunction.getSelectedItem().toString();
        String priority = spinnerPriority.getSelectedItem().toString();
        String doctorEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        if (patientEmail.isEmpty() || title.isEmpty() || videoUrl.isEmpty()) {
            Toast.makeText(this, "Барлық өрістерді толтырыңыз", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> videoData = new HashMap<>();
        videoData.put("patientEmail", patientEmail);
        videoData.put("doctorEmail", doctorEmail);
        videoData.put("title", title);
        videoData.put("videoUrl", videoUrl);
        videoData.put("function", function);
        videoData.put("priority", priority);
        videoData.put("createdAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("Videos")
                .add(videoData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Видео сәтті сақталды", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
