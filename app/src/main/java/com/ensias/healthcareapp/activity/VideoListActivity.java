package com.ensias.healthcareapp.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.patient.PatientVideoLessonActivity;
import com.ensias.healthcareapp.adapter.VideoAdapter;
import com.ensias.healthcareapp.model.VideoLesson;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<VideoLesson> videoList;
    private FirebaseFirestore firestore;
    private String folderName;
    private Uri selectedVideoUri = null;
    private EditText etUrl;

    @SuppressLint("SetTextI18n")
    private final ActivityResultLauncher<Intent> videoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    selectedVideoUri = result.getData().getData();
                    etUrl.setText("Видео выбрано: " + selectedVideoUri.getLastPathSegment());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        folderName = getIntent().getStringExtra("folderName");
        recyclerView = findViewById(R.id.recyclerViewVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        firestore = FirebaseFirestore.getInstance();
        videoList = new ArrayList<>();

        MaterialButton btnAddVideo = findViewById(R.id.btnAddVideo);
        btnAddVideo.setOnClickListener(v -> showAddVideoDialog());

        loadVideos();
    }

    private void loadVideos() {
        firestore.collection("VideoFolders")
                .document(folderName)
                .collection("Videos")
                .get()
                .addOnSuccessListener(query -> {
                    videoList.clear();
                    for (DocumentSnapshot doc : query) {
                        VideoLesson video = doc.toObject(VideoLesson.class);
                        videoList.add(video);
                    }

                    VideoAdapter adapter = new VideoAdapter(this, videoList, position -> {
                        VideoLesson selected = videoList.get(position);
                        Intent intent = new Intent(this, PatientVideoLessonActivity.class);
                        intent.putExtra("videoTitle", selected.getTitle());
                        intent.putExtra("videoUrl", selected.getUrl());
                        startActivity(intent);
                    });
                    recyclerView.setAdapter(adapter);
                });
    }

    private void showAddVideoDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_video, null);
        EditText etTitle = dialogView.findViewById(R.id.etVideoTitle);
        etUrl = dialogView.findViewById(R.id.etVideoUrl);
        Button btnPickVideo = dialogView.findViewById(R.id.btnPickVideo);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnPlayYouTube = dialogView.findViewById(R.id.btnPlayYouTube);
        btnPlayYouTube.setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            if (!url.isEmpty() && (url.contains("youtube.com") || url.contains("youtu.be"))) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage("com.google.android.youtube"); // откроется в YouTube, если установлен
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    // если YouTube не установлен — откроем в браузере
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            } else {
                Toast.makeText(this, "Введите корректную ссылку на YouTube", Toast.LENGTH_SHORT).show();
            }
        });

        btnPickVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            videoPickerLauncher.launch(Intent.createChooser(intent, "Выберите видео"));
        });

        new AlertDialog.Builder(this)
                .setTitle("Добавить видео")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String url = etUrl.getText().toString().trim();

                    if (selectedVideoUri != null) {
                        uploadVideoToFirebase(title, selectedVideoUri);
                    } else if (!url.isEmpty() && title.length() > 2) {
                        // YouTube ссылка
                        saveVideoToFirestore(new VideoLesson(title, url, false));
                    } else {
                        Toast.makeText(this, "Введите название и ссылку или выберите видео", Toast.LENGTH_SHORT).show();
                    }

                    selectedVideoUri = null;
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void uploadVideoToFirebase(String title, Uri videoUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("videos/" + System.currentTimeMillis() + ".mp4");

        storageRef.putFile(videoUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    VideoLesson video = new VideoLesson(title, uri.toString(), false);
                    saveVideoToFirestore(video);
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки видео: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void saveVideoToFirestore(VideoLesson video) {
        firestore.collection("VideoFolders")
                .document(folderName)
                .collection("Videos")
                .add(video)
                .addOnSuccessListener(doc -> Toast.makeText(this, "Видео добавлено", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
