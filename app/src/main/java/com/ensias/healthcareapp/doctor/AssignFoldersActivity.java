package com.ensias.healthcareapp.doctor;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignFoldersActivity extends AppCompatActivity {

    Spinner spinnerPatient;
    Button btnSelectFolders, btnAssign;
    TextView tvSelectedFolders;

    // Списки пациентов
    List<String> patientNames = new ArrayList<>();
    List<String> patientIds = new ArrayList<>();

    // Списки папок
    List<String> folderNames = new ArrayList<>();
    List<String> folderIds = new ArrayList<>();
    List<String> selectedFolderNames = new ArrayList<>();
    List<String> selectedFolderIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_folders);

        spinnerPatient = findViewById(R.id.spinner_patient);
        btnSelectFolders = findViewById(R.id.btn_select_folders);
        btnAssign = findViewById(R.id.btn_assign);
        tvSelectedFolders = findViewById(R.id.tv_selected_folders);

        loadPatients();

        btnSelectFolders.setOnClickListener(v -> loadAndShowFolders());

        btnAssign.setOnClickListener(v -> {
            int selectedPatientIndex = spinnerPatient.getSelectedItemPosition();
            if (selectedPatientIndex < 0 || selectedPatientIndex >= patientIds.size() || selectedFolderIds.isEmpty()) {
                Toast.makeText(this, "Пациент пен папканы таңдаңыз!", Toast.LENGTH_SHORT).show();
                return;
            }
            String patientId = patientIds.get(selectedPatientIndex);
            assignFoldersToPatient(patientId);
        });
    }

    // 1. Загружаем пациентов этого доктора (по doctorEmail)
    private void loadPatients() {
        String doctorEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore.getInstance()
                .collection("patients")
                .whereEqualTo("doctorEmail", doctorEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    patientNames.clear();
                    patientIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("fullName");
                        String email = doc.getString("email");
                        patientNames.add(name != null ? name : email);
                        patientIds.add(email);
                    }
                    ArrayAdapter<String> patientAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, patientNames);
                    patientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPatient.setAdapter(patientAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Пациенттер жүктелмеді", Toast.LENGTH_SHORT).show());
    }

    // 2. Загружаем папки (глобальные и личные)
    private void loadAndShowFolders() {
        String doctorEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore.getInstance()
                .collection("video_folders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    folderNames.clear();
                    folderIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Boolean isGlobal = doc.getBoolean("isGlobal");
                        String creator = doc.getString("doctorEmail");
                        if ((isGlobal != null && isGlobal) ||
                                (creator != null && creator.equals(doctorEmail))) {
                            folderNames.add(doc.getString("name"));
                            folderIds.add(doc.getId());
                        }
                    }
                    showFoldersMultiSelectDialog();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Папкалар жүктелмеді", Toast.LENGTH_SHORT).show());
    }

    // 3. Мультиселект-диалог для выбора папок
    private void showFoldersMultiSelectDialog() {
        boolean[] checkedItems = new boolean[folderNames.size()];
        selectedFolderNames.clear();
        selectedFolderIds.clear();

        new AlertDialog.Builder(this)
                .setTitle("Папкаларды таңдаңыз")
                .setMultiChoiceItems(folderNames.toArray(new String[0]), checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedFolderNames.add(folderNames.get(which));
                        selectedFolderIds.add(folderIds.get(which));
                    } else {
                        selectedFolderNames.remove(folderNames.get(which));
                        selectedFolderIds.remove(folderIds.get(which));
                    }
                })
                .setPositiveButton("Сақтау", (dialog, which) -> {
                    tvSelectedFolders.setText("Таңдалған папкалар: " + selectedFolderNames);
                })
                .setNegativeButton("Болдырмау", null)
                .show();
    }

    // 4. Назначаем папки пациенту (сохраняем в patient_video_folders)
    private void assignFoldersToPatient(String patientId) {
        Map<String, Object> data = new HashMap<>();
        data.put("folders", selectedFolderIds);

        FirebaseFirestore.getInstance()
                .collection("patient_video_folders")
                .document(patientId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Папкалар сәтті тағайындалды!", Toast.LENGTH_SHORT).show();
                    selectedFolderIds.clear();
                    selectedFolderNames.clear();
                    tvSelectedFolders.setText("Таңдалған папкалар: ");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
