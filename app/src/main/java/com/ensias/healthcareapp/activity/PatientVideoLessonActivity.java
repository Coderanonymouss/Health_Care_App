
package com.ensias.healthcareapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.adapter.VideoAdapter;
import com.ensias.healthcareapp.model.VideoLesson;
import com.ensias.healthcareapp.utils.AlarmScheduler;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PatientVideoLessonActivity extends AppCompatActivity {

    private TextView tvVideoReport, tvLevel;
    private ProgressBar progressXp;
    private VideoView videoView;
    private RecyclerView recyclerView;
    private Button btnWatch;

    private SharedPreferences prefs;
    private int videosWatched;
    private LocalDate startDate;
    private Set<String> watchedDates;
    private List<VideoLesson> videoList;
    private int currentIndex = -1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_lesson);

        tvVideoReport = findViewById(R.id.tvVideoReport);
        tvLevel = findViewById(R.id.tvLevel);
        progressXp = findViewById(R.id.progressXp);
        videoView = findViewById(R.id.videoView);
        recyclerView = findViewById(R.id.recyclerViewVideos);
        btnWatch = findViewById(R.id.btnWatchVideo);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        videosWatched = prefs.getInt("videosWatched", 0);
        watchedDates = prefs.getStringSet("watchedDates", new HashSet<>());

        String savedDate = prefs.getString("startDate", null);
        if (savedDate == null) {
            startDate = LocalDate.now();
            prefs.edit().putString("startDate", startDate.toString()).apply();
        } else {
            startDate = LocalDate.parse(savedDate);
        }

        // Запуск уведомлений каждый день
        AlarmScheduler.scheduleDailyReminder(this);

        initVideoList();
        updateUI();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        VideoAdapter adapter = new VideoAdapter(this, videoList, position -> {
            currentIndex = position;
            btnWatch.setVisibility(View.VISIBLE);
        });
        recyclerView.setAdapter(adapter);

        btnWatch.setOnClickListener(v -> {
            if (currentIndex < 0) return;

            VideoLesson selected = videoList.get(currentIndex);
            btnWatch.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);

                Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video1);
            videoView.setVideoURI(videoUri);
            videoView.start();

            Intent intent = new Intent(this, FullScreenVideoActivity.class);
            intent.putExtra("videoPath", "android.resource://" + getPackageName() + "/" + R.raw.video1);
            startActivity(intent);

            videoView.setOnCompletionListener(mp -> {
                if (!selected.isWatched()) {
                    selected.setWatched(true);
                    videosWatched++;
                    watchedDates.add(LocalDate.now().toString());

                    prefs.edit().putInt("videosWatched", videosWatched).apply();
                    prefs.edit().putStringSet("watchedDates", watchedDates).apply();
                }
                updateUI();
                videoView.setVisibility(View.GONE);
            });
        });
    }

    private void updateUI() {
        long daysPassed = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        int xp = videosWatched * 10;
        int level = (xp / 100) + 1;
        int progress = xp % 100;

        tvVideoReport.setText("Прошло дней: " + daysPassed + "\nПросмотрено видеоуроков: " + videosWatched);
        tvLevel.setText("Уровень: " + level);
        progressXp.setProgress(progress);
    }

    private void initVideoList() {
        videoList = new ArrayList<>();
        videoList.add(new VideoLesson("Урок 1: Введение", wasWatched("2025-05-01")));
        videoList.add(new VideoLesson("Урок 2: Упражнения", wasWatched("2025-05-02")));
        videoList.add(new VideoLesson("Урок 3: Питание", wasWatched("2025-05-03")));
    }

    private boolean wasWatched(String date) {
        return watchedDates.contains(date);
    }
}

