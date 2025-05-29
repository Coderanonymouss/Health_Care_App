package com.ensias.healthcareapp.patient.progress;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.MedicineIntakeRow;
import com.ensias.healthcareapp.model.VideoProgressRow;
import com.ensias.healthcareapp.patient.progress.adapter.MedicineIntakeAdapter;
import com.ensias.healthcareapp.patient.progress.adapter.VideoProgressAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PatientAnalyticsActivity extends AppCompatActivity {

    private ProgressBar progressBarVideo;
    private TextView tvVideoProgress, tvStreak;
    private RecyclerView rvVideo, rvMedicineIntake;
    private LinearLayout llBadges;
    private LineChart lineChartMedicine;
    private Toolbar toolbar;

    private final List<VideoProgressRow> videoRows = new ArrayList<>();
    private final List<MedicineIntakeRow> medicineRows = new ArrayList<>();
    private String patientUid;

    private VideoProgressAdapter videoAdapter;
    private MedicineIntakeAdapter medicineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_analytics);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBarVideo = findViewById(R.id.progressBarVideo);
        tvVideoProgress = findViewById(R.id.tvVideoProgress);
        tvStreak = findViewById(R.id.tvStreak);
        rvVideo = findViewById(R.id.rvVideoProgress);
        rvMedicineIntake = findViewById(R.id.rvMedicineIntake);
        lineChartMedicine = findViewById(R.id.lineChartMedicine);
        llBadges = findViewById(R.id.llBadges);

        rvVideo.setLayoutManager(new LinearLayoutManager(this));
        rvMedicineIntake.setLayoutManager(new LinearLayoutManager(this));

        videoAdapter = new VideoProgressAdapter(videoRows);
        medicineAdapter = new MedicineIntakeAdapter(medicineRows);

        rvVideo.setAdapter(videoAdapter);
        rvMedicineIntake.setAdapter(medicineAdapter);

        patientUid = FirebaseAuth.getInstance().getUid();
        if (patientUid == null) return;

        loadVideoProgress();
        loadMedicineIntake();
        loadMedicineChart();
    }

    // --- Badges ---
    private void showBadges(int streak, int watchedVideos, int fullDays) {
        llBadges.removeAllViews();
        if (streak >= 3) addBadge("3 күн қатарынан", R.drawable.ic_fire);
        if (streak >= 7) addBadge("7 күн қатарынан", R.drawable.ic_fire);
        if (watchedVideos >= 10) addBadge("10 видео сабақ", R.drawable.ic_award);
        if (fullDays >= 5) addBadge("5 күн толық курс", R.drawable.ic_medal);
    }

    private void addBadge(String label, int iconRes) {
        LinearLayout badge = new LinearLayout(this);
        badge.setOrientation(LinearLayout.HORIZONTAL);
        badge.setPadding(18,8,18,8);
        ImageView iv = new ImageView(this);
        iv.setImageResource(iconRes);
        iv.setLayoutParams(new LinearLayout.LayoutParams(48,48));
        badge.addView(iv);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(15);
        tv.setPadding(10,0,0,0);
        badge.addView(tv);

        llBadges.addView(badge);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void loadVideoProgress() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("video_progress").document(patientUid).get().addOnSuccessListener(progressDoc -> {
            if (!progressDoc.exists()) return;
            Map<String, Long> viewed = (Map<String, Long>) progressDoc.get("viewed");
            if (viewed == null) viewed = new HashMap<>();
            int streak = progressDoc.getLong("streak") != null ? Objects.requireNonNull(progressDoc.getLong("streak")).intValue() : 0;
            AtomicReference<String> folderId = new AtomicReference<>();

            Map<String, Long> finalViewed = viewed;
            db.collection("Patient").document(patientUid).get().addOnSuccessListener(patientDoc -> {
                folderId.set(patientDoc.getString("folderId"));
                if (folderId.get() == null) return;

                db.collection("video_folders").document(folderId.get()).collection("Videos").get().addOnSuccessListener(querySnapshot -> {
                    videoRows.clear();
                    int watchedCount = 0;
                    for (DocumentSnapshot doc : querySnapshot) {
                        String title = doc.getString("title");
                        String id = doc.getId();
                        boolean watched = finalViewed.containsKey(id);
                        long ts = finalViewed.get(id) != null ? finalViewed.get(id) : 0L;
                        if (watched) watchedCount++;
                        videoRows.add(new VideoProgressRow(title, watched, ts));
                    }
                    progressBarVideo.setMax(videoRows.size());
                    progressBarVideo.setProgress(watchedCount);
                    tvVideoProgress.setText("Прогресс: " + watchedCount + "/" + videoRows.size());
                    tvStreak.setText(streak > 1 ? ("Дней подряд: " + streak + " 🔥") : "");
                    videoAdapter.notifyDataSetChanged();
                    showBadges(streak, watchedCount, 0);
                });
            });
        });
    }

    /** Показывает на сегодня, сколько раз и сколько осталось по каждому лекарству */
    @SuppressLint("NotifyDataSetChanged")
    private void loadMedicineIntake() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        medicineRows.clear();

        db.collection("users").document(patientUid).collection("medicines")
                .get().addOnSuccessListener(medDocs -> {
                    for (DocumentSnapshot medDoc : medDocs) {
                        String name = medDoc.getString("name");
                        int durationDays = medDoc.getLong("durationDays") != null ? medDoc.getLong("durationDays").intValue() : 0;
                        int timesPerDay = medDoc.getLong("timesPerDay") != null ? medDoc.getLong("timesPerDay").intValue() : 0;

                        // Days left: вычисляем на основе даты создания документа и durationDays
                        Date createdAt = medDoc.getDate("createdAt"); // если есть поле createdAt
                        int daysLeft = durationDays;
                        // Если нет createdAt — просто покажи durationDays
                        // (или добавь вычисление — дай знать, подскажу)

                        medDoc.getReference().collection("intakes").get().addOnSuccessListener(intakeDocs -> {
                            int takenToday = 0;
                            for (DocumentSnapshot intake : intakeDocs) {
                                String docId = intake.getId(); // формат: name_yyyy-MM-dd_HH:mm
                                Boolean status = intake.getBoolean("status");
                                if (docId.contains(today) && status != null && status) {
                                    takenToday++;
                                }
                            }
                            int remainingToday = Math.max(0, timesPerDay - takenToday);

                            medicineRows.add(new MedicineIntakeRow(name, timesPerDay, takenToday, remainingToday, daysLeft));
                            medicineAdapter.notifyDataSetChanged();
                        });
                    }
                });
    }

    private void loadMedicineChart() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(patientUid).collection("medicines")
                .get().addOnSuccessListener(medDocs -> {
                    // собираем приёмы по датам
                    Map<String, Integer> takenByDate = new TreeMap<>();
                    for (DocumentSnapshot medDoc : medDocs) {
                        medDoc.getReference().collection("intakes").get().addOnSuccessListener(intakeDocs -> {
                            for (DocumentSnapshot intake : intakeDocs) {
                                Boolean taken = intake.getBoolean("status");
                                String[] parts = intake.getId().split("_");
                                String date = parts.length > 1 ? parts[1] : ""; // формат yyyy-MM-dd
                                if (taken != null && taken && !date.isEmpty()) {
                                    int prev = 0;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        prev = takenByDate.getOrDefault(date, 0);
                                    }
                                    takenByDate.put(date, prev + 1);
                                }
                            }

                            // Строим точки графика только за последние дни (или все если мало)
                            List<Entry> entries = new ArrayList<>();
                            List<String> days = new ArrayList<>(takenByDate.keySet());
                            int totalDays = days.size();

                            // даже если один день - покажет
                            for (int i = 0; i < totalDays; i++) {
                                String day = days.get(i);
                                entries.add(new Entry(i, takenByDate.get(day)));
                            }

                            LineDataSet dataSet = new LineDataSet(entries, "Приём лекарств");
                            dataSet.setColor(getResources().getColor(R.color.dlya_shapka));
                            dataSet.setCircleColor(getResources().getColor(R.color.dlya_shapka));
                            dataSet.setValueTextColor(getResources().getColor(R.color.primary_dark));
                            dataSet.setLineWidth(2f);
                            dataSet.setCircleRadius(4f);

                            LineData lineData = new LineData(dataSet);
                            lineChartMedicine.setData(lineData);

                            // Оформим ось X под даты (чтобы были подписи дат)
                            lineChartMedicine.getXAxis().setGranularity(1f);
                            lineChartMedicine.getXAxis().setValueFormatter(new ValueFormatter() {
                                @Override
                                public String getFormattedValue(float value) {
                                    int idx = (int) value;
                                    if (idx >= 0 && idx < days.size()) {
                                        String raw = days.get(idx); // yyyy-MM-dd
                                        // Можно красиво через формат, если хочется (см. предыдущий ответ)
                                        return raw.substring(5); // MM-dd
                                    }
                                    return "";
                                }
                            });


                            lineChartMedicine.getDescription().setText("Приём по дням");
                            lineChartMedicine.getLegend().setEnabled(false); // убрать легенду если мешает
                            lineChartMedicine.invalidate();
                        });
                    }
                });
    }

}
