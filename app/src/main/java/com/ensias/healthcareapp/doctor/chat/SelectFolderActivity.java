package com.ensias.healthcareapp.doctor.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.doctor.adapter.VideoLessonsFolderAdapter;
import com.ensias.healthcareapp.model.FolderItems;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class SelectFolderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoLessonsFolderAdapter adapter;
    private String doctorEmail;
    private String patientUid, patientEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_folder);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Выберите папку");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerFolders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        doctorEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        patientUid = getIntent().getStringExtra("patientUid");
        patientEmail = getIntent().getStringExtra("patientEmail");

        loadFolders();
    }

    private void loadFolders() {
        FirebaseFirestore.getInstance()
                .collection("video_folders")
                .whereEqualTo("createdBy", doctorEmail)
                .get()
                .addOnSuccessListener(query -> {
                    ArrayList<FolderItems> folders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        folders.add(new FolderItems(doc.getId(), doc.getString("name")));
                    }
                    if (folders.isEmpty()) {
                        Toast.makeText(this, "Нет папок для выбора. Создайте папку!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // Теперь при клике — просто возвращаем id выбранной папки!
                        adapter = new VideoLessonsFolderAdapter(folders, folderId -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("folderId", folderId);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        });
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки папок", Toast.LENGTH_SHORT).show();
                    Log.e("FOLDER", "Ошибка загрузки папок", e);
                    finish();
                });
    }

    // Обрабатываем кнопку "назад" в toolbar (AppBar)
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
