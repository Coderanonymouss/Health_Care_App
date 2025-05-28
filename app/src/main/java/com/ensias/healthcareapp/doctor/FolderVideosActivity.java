package com.ensias.healthcareapp.doctor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.adapter.FirestoreVideoAdapter;
import com.ensias.healthcareapp.model.VideoLesson;
import com.ensias.healthcareapp.patient.videolesson.PatientVideoListActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FolderVideosActivity extends AppCompatActivity {

    private FirestoreVideoAdapter adapter;
    private FirebaseFirestore db;

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

        // !!! Важно: используем коллекцию "videos", не "Videos" !!!
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
                    if (video.getVideoUrl() != null &&
                            (video.getVideoUrl().contains("youtube.com") || video.getVideoUrl().contains("youtu.be"))) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(video.getVideoUrl()));
                        startActivity(i);
                    } else if (video.getVideoUrl() != null) {
                        Intent i = new Intent(FolderVideosActivity.this, PatientVideoListActivity.class);
                        i.putExtra("videoTitle", video.getTitle());
                        i.putExtra("videoUrl",   video.getVideoUrl());
                        startActivity(i);
                    }
                }
        );

        rv.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}
