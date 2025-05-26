package com.ensias.healthcareapp.doctor;

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
import com.ensias.healthcareapp.adapter.FolderAdapter;
import com.ensias.healthcareapp.model.VideoFolder;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * Бұл класс дәрігерлерге арналған бейнесабақтар папкаларын көруге,
 * қосуға, өңдеуге және жоюға арналған негізгі экранды басқарады.
 */
public class DoctorVideoLessonsActivity extends AppCompatActivity {

    // Firestore дерекқорына сілтемелер
    private FirebaseFirestore db;
    private CollectionReference folderRef;

    // RecyclerView адаптері
    private FolderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_video_lessons);

        // Firestore және папка коллекциясына сілтеме инициализациясы
        db = FirebaseFirestore.getInstance();
        folderRef = db.collection("video_folders");

        // RecyclerView орнату (папкалар тізімі)
        RecyclerView folderRecycler = findViewById(R.id.folderRecyclerView);
        folderRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Папкаларды атауы бойынша сұрыптап сұрау
        Query query = folderRef.orderBy("name");

        // FirestoreRecyclerOptions арқылы адаптер конфигурациясы
        FirestoreRecyclerOptions<VideoFolder> options = new FirestoreRecyclerOptions.Builder<VideoFolder>()
                .setQuery(query, snapshot -> {
                    VideoFolder folder = snapshot.toObject(VideoFolder.class);
                    if (folder != null) folder.setId(snapshot.getId()); // Firestore құжатының ID-сын модельге орнату
                    return folder;
                })
                .setLifecycleOwner(this)
                .build();

        // Адаптерді орнату және папкаға байланысты әрекеттерді өңдеу
        adapter = new FolderAdapter(options, new FolderAdapter.OnFolderClickListener() {
            @Override
            public void onFolderClick(VideoFolder folder) {
                // Папканы басқанда: бейнелер тізіміне өту
                Intent intent = new Intent(DoctorVideoLessonsActivity.this, DoctorVideoListActivity.class);
                intent.putExtra("folderId", folder.getId());
                startActivity(intent);
            }

            @Override
            public void onEditFolder(VideoFolder folder) {
                // Папканы өңдеу терезесін көрсету
                showCreateFolderDialog(folder);
            }

            @Override
            public void onDeleteFolder(VideoFolder folder) {
                // Папканы жою
                deleteFolder(folder);
            }
        });

        folderRecycler.setAdapter(adapter);

        // "Папка қосу" батырмасы
        @SuppressLint("WrongViewCast")
        MaterialButton btnAddFolder = findViewById(R.id.btnAddFolder);
        btnAddFolder.setOnClickListener(v -> showCreateFolderDialog(null));
    }

    /**
     * Папка жасау немесе өңдеу диалогы
     *
     * @param folderToEdit Егер null болмаса, өңдеу режимі
     */
    private void showCreateFolderDialog(@Nullable VideoFolder folderToEdit) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_folder, null);
        EditText editText = dialogView.findViewById(R.id.editFolderName);

        if (folderToEdit != null) {
            // Егер өңделіп жатса, бұрынғы атауды енгізу өрісіне орнатамыз
            editText.setText(folderToEdit.getName());
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
                        // ✅ Жаңа папка қосу логикасы
                        Map<String, Object> folderData = new HashMap<>();
                        folderData.put("name", name);
                        folderData.put("createdBy", FirebaseAuth.getInstance().getCurrentUser() != null
                                ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : "");
                        folderData.put("createdAt", FieldValue.serverTimestamp());

                        folderRef.add(folderData)
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

    /**
     * Папканы Firestore-дан жою функциясы
     *
     * @param folder Жойылатын папка
     */
    private void deleteFolder(VideoFolder folder) {
        folderRef.document(folder.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Папка жойылды", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
