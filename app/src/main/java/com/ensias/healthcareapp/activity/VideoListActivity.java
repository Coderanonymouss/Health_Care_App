package com.ensias.healthcareapp.activity;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    private String patientId, folderId;
    private List<VideoLesson> videos = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_video_list);

        patientId = getIntent().getStringExtra("patientId");
        folderId  = getIntent().getStringExtra("folderId");
        if (patientId==null||folderId==null) {
            Toast.makeText(this,"Ошибка",Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        RecyclerView rv = findViewById(R.id.recyclerViewVideos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        FirebaseFirestore.getInstance()
                .collection("patients")
                .document(patientId)
                .collection("videoFolders")
                .document(folderId)
                .collection("Videos")
                .addSnapshotListener((snap,err)->{
                    if (snap!=null) {
                        videos.clear();
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            VideoLesson v = doc.toObject(VideoLesson.class);
                            v.setId(doc.getId());
                            videos.add(v);
                        }
                        rv.setAdapter(new VideoAdapter(videos, video -> {
                            Intent i = new Intent(VideoListActivity.this, PatientVideoLessonActivity.class);
                            i.putExtra("videoUrl", video.getUrl());
                            startActivity(i);
                        }));
                    }
                });
    }
}
