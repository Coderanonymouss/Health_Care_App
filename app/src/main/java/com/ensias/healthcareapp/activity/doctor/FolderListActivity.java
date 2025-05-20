package com.ensias.healthcareapp.activity.doctor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.VideoListActivity;
import com.ensias.healthcareapp.adapter.FolderAdapter;
import com.ensias.healthcareapp.adapter.FolderAdapter.OnFolderClickListener;
import com.ensias.healthcareapp.model.VideoFolder;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FolderListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FolderAdapter adapter;
    private FirebaseFirestore firestore;
    private CollectionReference folderRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_list);

        recyclerView = findViewById(R.id.recyclerViewFolders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firestore = FirebaseFirestore.getInstance();
        folderRef = firestore.collection("VideoFolders");
        Query query = folderRef.orderBy("name");

        FirestoreRecyclerOptions<VideoFolder> options =
                new FirestoreRecyclerOptions.Builder<VideoFolder>()
                        .setQuery(query, VideoFolder.class)
                        .setLifecycleOwner(this)
                        .build();

        adapter = new FolderAdapter(options, new OnFolderClickListener() {
            @Override
            public void onFolderClick(VideoFolder folder) {
                Intent intent = new Intent(FolderListActivity.this, VideoListActivity.class);
                intent.putExtra("folderName", folder.getName());
                startActivity(intent);
            }

            @Override
            public void onEditFolder(VideoFolder folder) {
                showFolderDialog(folder);
            }

            @Override
            public void onDeleteFolder(VideoFolder folder) {
                folderRef.document(folder.getId()).delete()
                        .addOnSuccessListener(unused -> Toast.makeText(FolderListActivity.this, "Папка удалена", Toast.LENGTH_SHORT).show());
            }
        });

        recyclerView.setAdapter(adapter);

        @SuppressLint({"WrongViewCast", "MissingInflatedId", "LocalSuppress"}) MaterialButton btnAdd = findViewById(R.id.btnAddFolder);
        btnAdd.setOnClickListener(v -> showFolderDialog(null));
    }

    private void showFolderDialog(@Nullable VideoFolder folderToEdit) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_folder, null);
        EditText etName = dialogView.findViewById(R.id.editFolderName);

        if (folderToEdit != null) {
            etName.setText(folderToEdit.getName());
        }

        new AlertDialog.Builder(this)
                .setTitle(folderToEdit == null ? "Жаңа папка" : "Папканы өңдеу")
                .setView(dialogView)
                .setPositiveButton("Сақтау", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Атауы бос болмауы керек", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (folderToEdit == null) {
                        folderRef.add(new VideoFolder(name));
                    } else {
                        folderRef.document(folderToEdit.getId()).update("name", name);
                    }
                })
                .setNegativeButton("Бас тарту", null)
                .show();
    }
}
