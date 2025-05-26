package com.ensias.healthcareapp.patient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Экран только для пациента!
public class PatientVideoListActivity extends AppCompatActivity {

    private String folderId;
    private final List<VideoLesson> videos = new ArrayList<>();
    private VideoAdapter adapter;

    private final Map<String, Boolean> progressMap = new HashMap<>();
    private TextView tvProgress; // для отображения прогресса

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_video_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvProgress = findViewById(R.id.tvProgress);

        folderId = getIntent().getStringExtra("folderId");
        if (folderId == null) {
            Toast.makeText(this, "Ошибка: папка не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // получаем id текущего пользователя (пациента)
        String patientId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        RecyclerView rv = findViewById(R.id.recyclerViewVideos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(videos, video -> {
            Intent i = new Intent(PatientVideoListActivity.this, PatientVideoLessonActivity.class);
            i.putExtra("videoUrl", video.getVideoUrl());
            i.putExtra("videoId", video.getId());
            i.putExtra("folderId", folderId);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        // Сначала загружаем индивидуальный прогресс пациента
        FirebaseFirestore.getInstance()
                .collection("patients")
                .document(patientId)
                .collection("videoProgress")
                .addSnapshotListener((progressSnap, err1) -> {
                    progressMap.clear();
                    if (progressSnap != null) {
                        for (DocumentSnapshot doc : progressSnap.getDocuments()) {
                            progressMap.put(doc.getId(), doc.getBoolean("watched") != null ? doc.getBoolean("watched") : Boolean.FALSE);
                        }
                    }

                    // Затем загружаем список видео в папке
                    FirebaseFirestore.getInstance()
                            .collection("video_folders")
                            .document(folderId)
                            .collection("Videos")
                            .addSnapshotListener((snap, err) -> {
                                videos.clear();
                                int watchedCount = 0;
                                if (snap != null) {
                                    for (DocumentSnapshot doc : snap.getDocuments()) {
                                        VideoLesson v = doc.toObject(VideoLesson.class);
                                        if (v != null) {
                                            v.setId(doc.getId());
                                            // устанавливаем статус "watched" из индивидуального прогресса
                                            boolean watched = false;
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                watched = Boolean.TRUE.equals(progressMap.getOrDefault(v.getId(), false));
                                            }
                                            if (watched) watchedCount++;
                                            videos.add(v);
                                        }
                                    }
                                }
                                // Показываем прогресс
                                tvProgress.setText("Просмотрено: " + watchedCount + " из " + videos.size());
                                adapter.notifyDataSetChanged();
                            });
                });

    }
}
