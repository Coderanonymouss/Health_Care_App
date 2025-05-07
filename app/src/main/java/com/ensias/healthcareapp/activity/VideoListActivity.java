package com.ensias.healthcareapp.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.adapter.VideoAdapter;
import com.ensias.healthcareapp.model.VideoLesson;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<VideoLesson> videoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        recyclerView = findViewById(R.id.recyclerViewVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        videoList = new ArrayList<>();
        videoList.add(new VideoLesson("Урок 1: Введение", false));
        videoList.add(new VideoLesson("Урок 2: Дыхательные упражнения", true));
        videoList.add(new VideoLesson("Урок 3: Движения рук", false));

        VideoAdapter adapter = new VideoAdapter(this, videoList, position -> {
            // Здесь можно открывать видео по индексу
            Intent intent = new Intent(VideoListActivity.this, PatientVideoLessonActivity.class);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}