
package com.ensias.healthcareapp.activity.patient;

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

        AlarmScheduler.scheduleDailyReminder(this);
        updateUI();

        // Получаем видео, если пришло
        String videoTitle = getIntent().getStringExtra("videoTitle");
        String videoUrl = getIntent().getStringExtra("videoUrl");

        if (videoUrl != null) {
            videoView.setVisibility(View.VISIBLE);
            Uri videoUri = Uri.parse(videoUrl);
            videoView.setVideoURI(videoUri);
            videoView.start();

            videoView.setOnCompletionListener(mp -> {
                if (!watchedDates.contains(LocalDate.now().toString())) {
                    watchedDates.add(LocalDate.now().toString());
                    videosWatched++;
                    prefs.edit().putInt("videosWatched", videosWatched).apply();
                    prefs.edit().putStringSet("watchedDates", watchedDates).apply();
                    updateUI();
                }
                videoView.setVisibility(View.GONE);
            });
        } else {
            // Если видео не передано — показываем список
            initVideoListFromFirestore();
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            VideoAdapter adapter = new VideoAdapter(this, videoList, position -> {
                currentIndex = position;
                btnWatch.setVisibility(View.VISIBLE);
            });
            recyclerView.setAdapter(adapter);

            btnWatch.setOnClickListener(v -> {
                if (currentIndex < 0) return;
                VideoLesson selected = videoList.get(currentIndex);
                Intent intent = new Intent(this, PatientVideoLessonActivity.class);
                intent.putExtra("videoTitle", selected.getTitle());
                intent.putExtra("videoUrl", selected.getUrl());
                startActivity(intent);
                finish();
            });
        }
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

    private void initVideoListFromFirestore() {
        videoList = new ArrayList<>();
        String folderName = getIntent().getStringExtra("folderName");
        FirebaseFirestore.getInstance()
                .collection("VideoFolders")
                .document(folderName)
                .collection("Videos")
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        VideoLesson video = doc.toObject(VideoLesson.class);
                        videoList.add(video);
                    }

                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    VideoAdapter adapter = new VideoAdapter(this, videoList, position -> {
                        VideoLesson selected = videoList.get(position);
                        Intent intent = new Intent(this, PatientVideoLessonActivity.class);
                        intent.putExtra("videoTitle", selected.getTitle());
                        intent.putExtra("videoUrl", selected.getUrl());
                        intent.putExtra("folderName", folderName);
                        startActivity(intent);
                    });
                    recyclerView.setAdapter(adapter);
                });
    }


    private boolean wasWatched(String date) {
        return watchedDates.contains(date);
    }
}

