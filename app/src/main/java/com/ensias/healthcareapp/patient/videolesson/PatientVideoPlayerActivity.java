package com.ensias.healthcareapp.patient.videolesson;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
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
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatientVideoPlayerActivity extends AppCompatActivity {

    private ExoPlayer exoPlayer;
    private YouTubePlayerView youtubePlayerView;
    private String patientUid, videoId, folderId;

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
        videoId = getIntent().getStringExtra("videoId");
        folderId = getIntent().getStringExtra("folderId");
        patientUid = FirebaseAuth.getInstance().getUid();

        if (!TextUtils.isEmpty(videoTitle)) {
            tvVideoTitle.setText(videoTitle);
        }

        btnBack.setOnClickListener(v -> onBackPressed());

        if (TextUtils.isEmpty(videoUrl) || TextUtils.isEmpty(videoId) || TextUtils.isEmpty(patientUid) || TextUtils.isEmpty(folderId)) {
            Toast.makeText(this, "Ошибка: не найдены необходимые данные", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (isYoutubeUrl(videoUrl)) {
            youtubePlayerView.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
            getLifecycle().addObserver(youtubePlayerView);

            String ytId = extractYouTubeId(videoUrl);
            if (!ytId.isEmpty()) {
                youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                    private boolean watched = false;
                    private float lastPos = 0f;
                    private float duration = 0f;

                    @Override
                    public void onReady(YouTubePlayer youTubePlayer) {
                        youTubePlayer.loadVideo(ytId, 0);
                    }

                    @Override
                    public void onVideoDuration(YouTubePlayer youTubePlayer, float videoDuration) {
                        duration = videoDuration;
                    }

                    @Override
                    public void onCurrentSecond(YouTubePlayer youTubePlayer, float second) {
                        lastPos = second;
                        if (!watched && duration > 0 && (duration - lastPos) <= 1f) {
                            watched = true;
                            saveVideoAsWatched();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Некорректная ссылка YouTube", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            youtubePlayerView.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);

            exoPlayer = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(exoPlayer);
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);

            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_ENDED) {
                        saveVideoAsWatched();
                    }
                }
            });
        }
    }

    private void getVideoIndex(String folderId, String videoId, OnIndexFoundListener listener) {
        FirebaseFirestore.getInstance()
                .collection("video_folders")
                .document(folderId)
                .collection("Videos")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int index = 0;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        if (videoId.equals(doc.getId())) {
                            listener.onFound(index);
                            return;
                        }
                        index++;
                    }
                    listener.onFound(-1); // не найдено
                })
                .addOnFailureListener(e -> listener.onFound(-1));
    }

    public interface OnIndexFoundListener {
        void onFound(int index);
    }

    private void saveVideoAsWatched() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("video_progress").document(patientUid);

        ref.get().addOnSuccessListener(doc -> {
            Map<String, Long> viewed = new HashMap<>();
            int currentIndex = 0;
            long lastVideoDate = System.currentTimeMillis();
            int streak = 1;

            if (doc.exists()) {
                Map<String, Object> data = doc.getData();
                if (data != null && data.get("viewed") instanceof Map) {
                    viewed = (Map<String, Long>) data.get("viewed");
                }
                Number i = (Number) data.get("currentIndex");
                if (i != null) currentIndex = i.intValue();

                Long lastDate = (Long) data.get("lastVideoDate");
                if (lastDate != null) {
                    Calendar today = Calendar.getInstance();
                    Calendar last = Calendar.getInstance();
                    last.setTimeInMillis(lastDate);

                    // streak logic
                    if (today.get(Calendar.YEAR) > last.get(Calendar.YEAR)
                            || today.get(Calendar.DAY_OF_YEAR) > last.get(Calendar.DAY_OF_YEAR)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            streak = ((Number) data.getOrDefault("streak", 1)).intValue() + 1;
                        }
                    } else if (today.get(Calendar.YEAR) == last.get(Calendar.YEAR)
                            && today.get(Calendar.DAY_OF_YEAR) == last.get(Calendar.DAY_OF_YEAR)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            streak = ((Number) data.getOrDefault("streak", 1)).intValue();
                        }
                    } else {
                        streak = 1;
                    }
                }
            }

            // Проверяем, просмотрено ли уже видео
            if (viewed.containsKey(videoId)) {
                // Уже просмотрено — просто выходим
                runOnUiThread(() ->
                        Toast.makeText(this, "Это видео уже было просмотрено!", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // Помечаем видео просмотренным
            viewed.put(videoId, System.currentTimeMillis());

            // Теперь ищем индекс этого видео
            int prevIndex = currentIndex;
            Map<String, Long> finalViewed = viewed;
            int finalStreak = streak;
            getVideoIndex(folderId, videoId, index -> {
                int newIndex = Math.max(prevIndex, index);

                // Только если это новое видео и это "следующее" по очереди (currentIndex + 1), обновляем lastVideoDate и streak
                boolean isNext = index == prevIndex + 1;
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("viewed", finalViewed);

                if (isNext) {
                    updateData.put("lastVideoDate", System.currentTimeMillis());
                    updateData.put("streak", finalStreak);
                    updateData.put("currentIndex", newIndex);
                } else {
                    // Не увеличиваем streak/дату для старых видео
                    updateData.put("currentIndex", prevIndex); // не меняем lastVideoDate/streak
                }

                ref.set(updateData, SetOptions.merge());

                runOnUiThread(() ->
                        Toast.makeText(this, "Поздравляем! Видео просмотрено!", Toast.LENGTH_SHORT).show()
                );
            });
        });
    }

    private boolean isYoutubeUrl(String url) {
        return !TextUtils.isEmpty(url) &&
                (url.contains("youtube.com") || url.contains("youtu.be"));
    }

    private String extractYouTubeId(String url) {
        String pattern = "(?:youtube\\.com\\/(?:[^\\/]+\\/.+\\/|(?:v|e(?:mbed)?)\\/|.*[?&]v=)|youtu\\.be\\/)([^\"&?\\/#\\s]{11})";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        if (url.matches("^[\\w-]{11}$")) return url;
        return "";
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
}
