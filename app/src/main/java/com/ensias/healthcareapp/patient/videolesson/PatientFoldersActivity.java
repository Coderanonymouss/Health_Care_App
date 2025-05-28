package com.ensias.healthcareapp.patient.videolesson;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.VideoFolder;
import com.ensias.healthcareapp.patient.videolesson.adapter.PatientFolderAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatientFoldersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PatientFolderAdapter adapter;
    private List<VideoFolder> folders = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String patientUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_list);

        recyclerView = findViewById(R.id.recyclerViewFolders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PatientFolderAdapter(folders, folder -> {
            Intent intent = new Intent(this, PatientVideoListActivity.class);
            intent.putExtra("folderId", folder.getId());
            intent.putExtra("folderName", folder.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        patientUid = FirebaseAuth.getInstance().getUid();
        loadFoldersForPatient();
    }

    private void loadFoldersForPatient() {
        firestore.collection("patient_folders")
                .whereEqualTo("patientUid", patientUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Set<String> folderIds = new HashSet<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String folderId = doc.getString("folderId");
                        if (folderId != null) {
                            folderIds.add(folderId);
                        }
                    }
                    if (folderIds.isEmpty()) {
                        folders.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }
                    firestore.collection("video_folders")
                            .whereIn(FieldPath.documentId(), new ArrayList<>(folderIds))
                            .get()
                            .addOnSuccessListener(foldersSnapshot -> {
                                folders.clear();
                                for (DocumentSnapshot doc : foldersSnapshot.getDocuments()) {
                                    VideoFolder folder = doc.toObject(VideoFolder.class);
                                    if (folder != null) {
                                        folder.setId(doc.getId());
                                        folders.add(folder);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Ошибка загрузки папок", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки связей", Toast.LENGTH_SHORT).show()
                );
    }
}
