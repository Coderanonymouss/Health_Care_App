package com.ensias.healthcareapp.activity.patient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PatientVideoLessonActivity extends AppCompatActivity {

    private TextView    tvVideoReport, tvLevel;
    private ProgressBar progressXp;
    private VideoView   videoView;
    private RecyclerView recyclerView;

    private SharedPreferences prefs;
    private int videosWatched;
    private LocalDate startDate;
    private Set<String> watchedDates;

    private List<VideoLesson> videoList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_lesson);

        // UI
        tvVideoReport  = findViewById(R.id.tvVideoReport);
        tvLevel        = findViewById(R.id.tvLevel);
        progressXp     = findViewById(R.id.progressXp);
        videoView      = findViewById(R.id.videoView);
        recyclerView   = findViewById(R.id.recyclerViewVideos);

        // prefs и статистика
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        videosWatched = prefs.getInt("videosWatched", 0);
        watchedDates  = prefs.getStringSet("watchedDates", new HashSet<>());
        String saved = prefs.getString("startDate", null);
        if (saved==null) {
            startDate = LocalDate.now();
            prefs.edit().putString("startDate", startDate.toString()).apply();
        } else {
            startDate = LocalDate.parse(saved);
        }
        AlarmScheduler.scheduleDailyReminder(this);
        updateUI();

        // если пришёл URL — сразу воспроизводим
        String videoUrl = getIntent().getStringExtra("videoUrl");
        if (videoUrl != null) {
            playVideo(videoUrl);
        } else {
            // иначе показываем список
            initVideoListFromFirestore();
        }
    }

    private void updateUI() {
        long days = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        int xp    = videosWatched * 10;
        int lvl   = xp / 100 + 1;
        int prog  = xp % 100;

        tvVideoReport.setText("Прошло дней: " + days + "\nВидео просмотрено: " + videosWatched);
        tvLevel.setText("Уровень: " + lvl);
        progressXp.setProgress(prog);
    }

    private void playVideo(String url) {
        recyclerView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoURI(Uri.parse(url));
        videoView.start();

        videoView.setOnCompletionListener(mp -> {
            String today = LocalDate.now().toString();
            if (!watchedDates.contains(today)) {
                watchedDates.add(today);
                videosWatched++;
                prefs.edit().putInt("videosWatched", videosWatched)
                        .putStringSet("watchedDates", watchedDates)
                        .apply();
                updateUI();
            }
            videoView.setVisibility(View.GONE);
        });
    }

    private void initVideoListFromFirestore() {
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // у вас, видимо, передаётся folderName
        String folderName = getIntent().getStringExtra("folderName");

        FirebaseFirestore.getInstance()
                .collection("VideoFolders")
                .document(folderName)
                .collection("Videos")
                .get()
                .addOnSuccessListener(query -> {
                    videoList.clear();
                    for (DocumentSnapshot d : query.getDocuments()) {
                        VideoLesson v = d.toObject(VideoLesson.class);
                        v.setId(d.getId());
                        videoList.add(v);
                    }
                    VideoAdapter adapter = new VideoAdapter(videoList, video -> {
                        // при клике — перезапустить этот activity с url
                        Intent it = new Intent(PatientVideoLessonActivity.this, PatientVideoLessonActivity.class);
                        it.putExtra("videoUrl", video.getVideoUrl());
                        startActivity(it);
                        finish();
                    });
                    recyclerView.setAdapter(adapter);
                });
    }
}
