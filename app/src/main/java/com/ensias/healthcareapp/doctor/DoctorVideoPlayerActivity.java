package com.ensias.healthcareapp.doctor;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoctorVideoPlayerActivity extends AppCompatActivity {

    private ExoPlayer exoPlayer;
    private YouTubePlayerView youtubePlayerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setContentView(R.layout.activity_video_player);

        PlayerView playerView = findViewById(R.id.playerView);
        youtubePlayerView = findViewById(R.id.youtubePlayerView);
        TextView tvVideoTitle = findViewById(R.id.tvVideoTitle);
        ImageButton btnBack = findViewById(R.id.btnBack);

        String videoUrl = getIntent().getStringExtra("videoUrl");
        String videoTitle = getIntent().getStringExtra("videoTitle");

        if (!TextUtils.isEmpty(videoTitle)) {
            tvVideoTitle.setText(videoTitle);
        }

        btnBack.setOnClickListener(v -> onBackPressed());

        if (TextUtils.isEmpty(videoUrl)) {
            Toast.makeText(this, "URL видео не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (isYoutubeUrl(videoUrl)) {
            // Показываем YouTube
            youtubePlayerView.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);

            getLifecycle().addObserver(youtubePlayerView);

            String videoId = extractYouTubeId(videoUrl);
            if (!videoId.isEmpty()) {
                youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(YouTubePlayer youTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0);
                    }
                });
            } else {
                Toast.makeText(this, "Некорректная ссылка YouTube", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Показываем обычное видео (mp4 и т.д.)
            youtubePlayerView.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);

            exoPlayer = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(exoPlayer);
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (youtubePlayerView != null) {
            youtubePlayerView.release();
        }
        super.onDestroy();
    }

    private boolean isYoutubeUrl(String url) {
        return !TextUtils.isEmpty(url) &&
                (url.contains("youtube.com") || url.contains("youtu.be"));
    }

    private String extractYouTubeId(String url) {
        // Для коротких и длинных ссылок
        String pattern = "(?:youtube\\.com\\/(?:[^\\/]+\\/.+\\/|(?:v|e(?:mbed)?)\\/|.*[?&]v=)|youtu\\.be\\/)([^\"&?\\/#\\s]{11})";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Иногда id — это весь путь
        if (url.matches("^[\\w-]{11}$")) return url;
        return "";
    }
}
