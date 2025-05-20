package com.ensias.healthcareapp.activity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;

public class LocalVideoPlayerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_video_player);

        VideoView vv = findViewById(R.id.videoViewPlayer);
        String url = getIntent().getStringExtra("videoUrl");
        vv.setVideoURI(Uri.parse(url));
        vv.setMediaController(new MediaController(this));
        vv.start();
    }
}
