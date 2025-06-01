    package com.ensias.healthcareapp.patient.videolesson;

    import android.annotation.SuppressLint;
    import android.content.Intent;
    import android.os.Build;
    import android.os.Bundle;
    import android.util.Log;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.ensias.healthcareapp.R;
    import com.ensias.healthcareapp.adapter.VideoAdapter;
    import com.ensias.healthcareapp.model.VideoLesson;
    import com.google.android.material.appbar.MaterialToolbar;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.Collections;
    import java.util.List;
    import java.util.Map;

    public class PatientVideoListActivity extends AppCompatActivity {

        private List<VideoLesson> videos = new ArrayList<>();
        private VideoAdapter adapter;
        private ProgressBar progressBar;
        private TextView tvProgress, tvStreak, tvCongrats;
        private String patientUid;
        private String folderId;

        @SuppressLint("MissingInflatedId")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_patient_video_list);

            RecyclerView rv = findViewById(R.id.recyclerViewVideos);
            rv.setLayoutManager(new LinearLayoutManager(this));
            progressBar = findViewById(R.id.progressBar);
            tvProgress = findViewById(R.id.tvProgress);
            tvStreak = findViewById(R.id.tvStreak);
            tvCongrats = findViewById(R.id.tvCongrats);
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());

            adapter = new VideoAdapter(videos, video -> {
                if (video.isAvailable()) {
                    Intent i = new Intent(this, PatientVideoPlayerActivity.class);
                    i.putExtra("videoUrl", video.getVideoUrl());
                    i.putExtra("videoTitle", video.getTitle());
                    i.putExtra("videoId", video.getId());
                    i.putExtra("folderId", folderId);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Дождитесь следующего дня для нового видео!", Toast.LENGTH_SHORT).show();
                }
            });
            rv.setAdapter(adapter);

            patientUid = FirebaseAuth.getInstance().getUid();
            if (patientUid == null) {
                showError("Ошибка: не удалось определить пользователя");
                return;
            }
            Log.d("VideoListActivity", "patientUid = " + patientUid);

            // 🟢 Найти документ по полю uid, а не по documentId
            FirebaseFirestore.getInstance()
                    .collection("Patient")
                    .whereEqualTo("uid", patientUid)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            folderId = doc.getString("folderId");
                            if (folderId == null || folderId.isEmpty()) {
                                showError("Вам ещё не назначена папка с видеоуроками");
                            } else {
                                loadVideosAndProgress();
                            }
                        } else {
                            showError("Пользователь не найден");
                        }
                    })
                    .addOnFailureListener(e -> showError("Ошибка при загрузке данных"));
        }

        private void loadVideosAndProgress() {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("video_folders")
                    .document(folderId)
                    .collection("Videos")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        videos.clear();
                        List<VideoLesson> allVideos = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot) {
                            VideoLesson v = doc.toObject(VideoLesson.class);
                            if (v != null) {
                                v.setId(doc.getId());
                                allVideos.add(v);
                            }
                        }
                        if (allVideos.isEmpty()) {
                            showError("Видеоуроки не найдены. Обратитесь к врачу.");
                            return;
                        }
                        // Если хочешь, сортируй по другой логике (по дате, по порядковому номеру и т.д.)
                        Collections.sort(allVideos, (a, b) -> {
                            int aNum = extractLeadingNumber(a.getTitle());
                            int bNum = extractLeadingNumber(b.getTitle());
                            return Integer.compare(aNum, bNum);
                        });

                        loadUserProgress(allVideos);
                    })
                    .addOnFailureListener(e -> showError("Ошибка загрузки видеоуроков"));
        }

        private int extractLeadingNumber(String text) {
            if (text == null || text.isEmpty()) return Integer.MAX_VALUE;
            try {
                String[] parts = text.trim().split("\\s+");
                return Integer.parseInt(parts[0]);
            } catch (Exception e) {
                return Integer.MAX_VALUE;
            }
        }

        private void loadUserProgress(List<VideoLesson> allVideos) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("video_progress")
                    .document(patientUid)
                    .get()
                    .addOnSuccessListener(progressDoc -> {
                        Map<String, Long> viewed = null;
                        long lastDate = 0;
                        int streak = 0;

                        if (progressDoc.exists()) {
                            viewed = (Map<String, Long>) progressDoc.get("viewed");
                            Long d = progressDoc.getLong("lastVideoDate");
                            lastDate = d == null ? 0 : d;
                            Integer s = progressDoc.getLong("streak") != null ? progressDoc.getLong("streak").intValue() : 0;
                            streak = s;
                        }

                        // streak logic
                        if (lastDate != 0 && !isTodayNew(lastDate) && isYesterday(lastDate)) {
                            streak++;
                        } else if (lastDate != 0 && !isTodayNew(lastDate)) {
                            streak = 1;
                        }

                        int watchedCount = 0;
                        int firstUnwatched = -1;
                        videos.clear();

                        // 1. Помечаем просмотренные и находим первое НЕ просмотренное
                        for (int i = 0; i < allVideos.size(); i++) {
                            VideoLesson v = allVideos.get(i);
                            boolean watched = viewed != null && viewed.containsKey(v.getId());
                            v.setWatched(watched);
                            v.setAvailable(watched); // Доступны только просмотренные и... смотри ниже!
                            if (watched) watchedCount++;
                            else if (firstUnwatched == -1) firstUnwatched = i;
                            videos.add(v);
                        }

                        // 2. Проверяем: можно ли открыть СЛЕДУЮЩЕЕ (только если новый день)
                        boolean openNext = false;
                        if (firstUnwatched != -1) {
                            // lastDate == 0 — первый просмотр; открываем только 1 урок
                            if (lastDate == 0 || isTodayNew(lastDate)) openNext = true;
                        }
                        if (openNext && firstUnwatched != -1) {
                            videos.get(firstUnwatched).setAvailable(true);
                        }

                        // Все остальные НЕ просмотренные — недоступны!
                        for (int i = 0; i < videos.size(); i++) {
                            if (!videos.get(i).isWatched() && i != firstUnwatched) {
                                videos.get(i).setAvailable(false);
                            }
                        }

                        // UI/Progress
                        if (progressBar != null) {
                            progressBar.setMax(allVideos.size());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                progressBar.setProgress(watchedCount, true);
                            } else {
                                progressBar.setProgress(watchedCount);
                            }
                        }
                        if (tvProgress != null) {
                            tvProgress.setText("Прогресс: " + watchedCount + "/" + allVideos.size());
                        }
                        if (tvStreak != null) {
                            if (streak > 1) {
                                tvStreak.setText("Дней подряд: " + streak + " 🔥");
                            } else {
                                tvStreak.setText("");
                            }
                        }
                        if (tvCongrats != null) {
                            tvCongrats.setText(watchedCount == allVideos.size() ? "Поздравляем! Все видеоуроки просмотрены!" : "");
                        }
                        db.collection("video_progress").document(patientUid)
                                .update("streak", streak);

                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> showError("Ошибка загрузки прогресса"));
        }

        private void showError(String message) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            finish();
        }

        // Новый день?
        private boolean isTodayNew(long lastTimestamp) {
            Calendar today = Calendar.getInstance();
            Calendar last = Calendar.getInstance();
            last.setTimeInMillis(lastTimestamp);
            return today.get(Calendar.YEAR) > last.get(Calendar.YEAR)
                    || today.get(Calendar.DAY_OF_YEAR) > last.get(Calendar.DAY_OF_YEAR);
        }

        // Вчерашний день?
        private boolean isYesterday(long lastTimestamp) {
            Calendar today = Calendar.getInstance();
            Calendar last = Calendar.getInstance();
            last.setTimeInMillis(lastTimestamp);
            today.add(Calendar.DAY_OF_YEAR, -1);
            return today.get(Calendar.YEAR) == last.get(Calendar.YEAR)
                    && today.get(Calendar.DAY_OF_YEAR) == last.get(Calendar.DAY_OF_YEAR);
        }
    }
