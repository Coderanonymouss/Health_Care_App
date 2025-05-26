package com.ensias.healthcareapp.patient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// Активити для просмотра назначенных папок
public class PatientFoldersActivity extends AppCompatActivity {

    ListView listFolders;
    List<String> folderNames = new ArrayList<>();
    List<String> folderIds = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_folders);

        listFolders = findViewById(R.id.list_folders);

        loadAssignedFoldersForPatient();
    }

    // Получаем назначенные folderId
    private void loadAssignedFoldersForPatient() {
        String patientId = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // или uid
        FirebaseFirestore.getInstance()
                .collection("patient_video_folders")
                .document(patientId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> assignedFolderIds = (List<String>) documentSnapshot.get("folders");
                    if (assignedFolderIds != null && !assignedFolderIds.isEmpty()) {
                        loadFolderNames(assignedFolderIds);
                    } else {
                        Toast.makeText(this, "Сізге ешқандай папка тағайындалмады", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Получаем имена папок
    private void loadFolderNames(List<String> assignedFolderIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        folderNames.clear();
        folderIds.clear();

        // Firestore позволяет whereIn только до 10 id за раз, делай батчами если нужно
        db.collection("video_folders")
                .whereIn("id", assignedFolderIds) // если хранишь поле id в документе!
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        folderNames.add(doc.getString("name"));
                        folderIds.add(doc.getId());
                    }
                    updateFoldersList();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Папкалар жүктелмеді", Toast.LENGTH_SHORT).show());
    }

    // Показываем список папок
    private void updateFoldersList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, folderNames);
        listFolders.setAdapter(adapter);

        listFolders.setOnItemClickListener((parent, view, position, id) -> {
            String folderId = folderIds.get(position);
            Intent intent = new Intent(PatientFoldersActivity.this, PatientVideosActivity.class);
            intent.putExtra("folderId", folderId);
            startActivity(intent);
        });
    }
}
