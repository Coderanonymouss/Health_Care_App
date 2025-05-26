package com.ensias.healthcareapp.doctor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.patient.PatientVideoLessonActivity;
import com.ensias.healthcareapp.adapter.VideoAdapter;
import com.ensias.healthcareapp.model.VideoLesson;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DoctorVideoListActivity extends AppCompatActivity {

    private String folderId;
    private List<VideoLesson> videos = new ArrayList<>();
    private VideoAdapter adapter;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        folderId = getIntent().getStringExtra("folderId");
        if (folderId == null) {
            Toast.makeText(this, "Ошибка: папка не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RecyclerView rv = findViewById(R.id.recyclerViewVideos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(videos, video -> {
            Intent i = new Intent(DoctorVideoListActivity.this.peekAvailableContext(), DoctorVideoPlayerActivity.class);
            i.putExtra("videoUrl", video.getVideoUrl());
            // Можно добавить title, description и т.д.
            startActivity(i);
        });
        rv.setAdapter(adapter);

        // Загрузка видео из Firestore
        FirebaseFirestore.getInstance()
                .collection("video_folders")
                .document(folderId)
                .collection("Videos")
                .addSnapshotListener((snap, err) -> {
                    if (snap != null) {
                        videos.clear();
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            VideoLesson v = doc.toObject(VideoLesson.class);
                            if (v != null) {
                                v.setId(doc.getId());
                                videos.add(v);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        // Кнопка "Добавить видео"
        MaterialButton btnAddVideo = findViewById(R.id.btnAddVideo);
        btnAddVideo.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorUploadVideoActivity.class);
            intent.putExtra("folderId", folderId); // обязательно передавай folderId!
            startActivity(intent);
        });
    }
}
