package com.ensias.healthcareapp.activity.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.patient.PatientVideoLessonActivity;
import com.ensias.healthcareapp.adapter.FirestoreVideoAdapter;
import com.ensias.healthcareapp.model.VideoLesson;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FolderVideosActivity extends AppCompatActivity {

    private FirestoreVideoAdapter adapter;
    private FirebaseFirestore       db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_videos);

        String folderId = getIntent().getStringExtra("folderId");
        if (folderId == null) {
            Toast.makeText(this, "Папка не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        RecyclerView rv = findViewById(R.id.videosRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Запрос на все видео внутри этой папки
        Query query = db.collection("video_folders")
                .document(folderId)
                .collection("Videos")
                .orderBy("title");

        FirestoreRecyclerOptions<VideoLesson> options =
                new FirestoreRecyclerOptions.Builder<VideoLesson>()
                        .setQuery(query, VideoLesson.class)
                        .setLifecycleOwner(this)
                        .build();

        adapter = new FirestoreVideoAdapter(
                this,
                options,
                video -> {
                    Intent i = new Intent(FolderVideosActivity.this,
                            PatientVideoLessonActivity.class);
                    i.putExtra("videoTitle", video.getTitle());
                    i.putExtra("videoUrl",   video.getUrl());
                    startActivity(i);
                }
        );

        rv.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
