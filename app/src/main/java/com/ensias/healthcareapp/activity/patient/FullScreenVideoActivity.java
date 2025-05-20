package com.ensias.healthcareapp.activity.patient;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;

public class FullScreenVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_video);

        VideoView videoView = findViewById(R.id.fullscreenVideoView);
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // Закрыть активити

        // Получаем видео из интента
        String videoPath = getIntent().getStringExtra("videoPath");
        Uri videoUri = Uri.parse(videoPath);
        videoView.setVideoURI(videoUri);
        videoView.start();
    }
}
