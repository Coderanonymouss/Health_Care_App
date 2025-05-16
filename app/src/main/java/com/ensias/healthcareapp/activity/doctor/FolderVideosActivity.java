package com.ensias.healthcareapp.activity.doctor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.patient.PatientVideoLessonActivity;
import com.ensias.healthcareapp.adapter.FirestoreVideoAdapter;
import com.ensias.healthcareapp.adapter.VideoAdapter;
import com.ensias.healthcareapp.model.Video;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.*;

public class FolderVideosActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirestoreVideoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_videos);

        String folderId = getIntent().getStringExtra("folderId");
        String folderName = getIntent().getStringExtra("folderName");

        if (folderId == null) {
            Toast.makeText(this, "Папка табылмады", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) RecyclerView recyclerView = findViewById(R.id.videosRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Query query = db.collection("video_folders")
                .document(folderName)
                .collection("Videos");

        FirestoreRecyclerOptions<Video> options =
                new FirestoreRecyclerOptions.Builder<Video>()
                        .setQuery(query, Video.class)
                        .build();

        adapter = new FirestoreVideoAdapter(options, video -> {
            Intent intent = new Intent(FolderVideosActivity.this, PatientVideoLessonActivity.class);
            intent.putExtra("videoTitle", video.getTitle());
            intent.putExtra("videoUrl", video.getUrl());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

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
