package com.ensias.healthcareapp.activity.doctor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.VideoListActivity;
import com.ensias.healthcareapp.adapter.FolderAdapter;
import com.ensias.healthcareapp.model.VideoFolder;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class DoctorVideoLessonsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference folderRef;
    private FolderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_video_lessons);

        db = FirebaseFirestore.getInstance();
        folderRef = db.collection("video_folders");

        RecyclerView folderRecycler = findViewById(R.id.folderRecyclerView);
        folderRecycler.setLayoutManager(new LinearLayoutManager(this));

        Query query = folderRef.orderBy("name");

        FirestoreRecyclerOptions<VideoFolder> options = new FirestoreRecyclerOptions.Builder<VideoFolder>()
                .setQuery(query, snapshot -> {
                    VideoFolder folder = snapshot.toObject(VideoFolder.class);
                    if (folder != null) folder.setId(snapshot.getId());
                    return folder;
                })
                .setLifecycleOwner(this)
                .build();

        adapter = new FolderAdapter(options, new FolderAdapter.OnFolderClickListener() {
            @Override
            public void onFolderClick(VideoFolder folder) {
                Intent intent = new Intent(DoctorVideoLessonsActivity.this, VideoListActivity.class);
                intent.putExtra("folderName", folder.getName());
                startActivity(intent);
            }

            @Override
            public void onEditFolder(VideoFolder folder) {
                showCreateFolderDialog(folder);
            }

            @Override
            public void onDeleteFolder(VideoFolder folder) {
                deleteFolder(folder);
            }
        });

        folderRecycler.setAdapter(adapter);

        @SuppressLint("WrongViewCast") MaterialButton btnAddFolder = findViewById(R.id.btnAddFolder);
        btnAddFolder.setOnClickListener(v -> showCreateFolderDialog(null));
    }

    private void showCreateFolderDialog(@Nullable VideoFolder folderToEdit) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_folder, null);
        EditText editText = dialogView.findViewById(R.id.editFolderName);

        if (folderToEdit != null) {
            editText.setText(folderToEdit.getName()); // заполни если редактируем
        }

        new AlertDialog.Builder(this)
                .setTitle(folderToEdit == null ? "Жаңа папка қосу" : "Папканы өңдеу")
                .setView(dialogView)
                .setPositiveButton(folderToEdit == null ? "Қосу" : "Сақтау", (dialog, which) -> {
                    String name = editText.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Атауы бос болмауы керек", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (folderToEdit == null) {
                        // ✅ Жаңа папка қосу
                        folderRef.add(new VideoFolder(name))
                                .addOnSuccessListener(doc -> Toast.makeText(this, "Папка қосылды", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                });
                    } else {
                        // ✏️ Бар папканы өңдеу
                        folderRef.document(folderToEdit.getId())
                                .update("name", name)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Өңделді", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                });
                    }
                })
                .setNegativeButton("Бас тарту", null)
                .show();
    }

    private void deleteFolder(VideoFolder folder) {
        folderRef.document(folder.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Папка жойылды", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
