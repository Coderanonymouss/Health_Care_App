package com.ensias.healthcareapp.patient.progress;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ensias.healthcareapp.R;
import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.prolificinteractive.materialcalendarview.*;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PatientMonitoringActivity extends AppCompatActivity {

    private ProgressBar progressBarMed;
    private PieChart pieChart;
    private LineChart lineChart;
    private MaterialCalendarView calendarView;
    private RecyclerView rvHistory;
    private TextView tvMedProgress, tvHint;
    private LinearLayout badgesContainer;
    private HistoryAdapter historyAdapter;
    private final List<HistoryItem> historyList = new ArrayList<>();

    private String userId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_monitoring);

        progressBarMed = findViewById(R.id.progressBarMed);
        tvMedProgress = findViewById(R.id.tvMedProgress);
        pieChart = findViewById(R.id.pieChart);
        lineChart = findViewById(R.id.lineChart);
        calendarView = findViewById(R.id.calendarView);
        rvHistory = findViewById(R.id.rvHistory);
        tvHint = findViewById(R.id.tvHint);
        badgesContainer = findViewById(R.id.badgesContainer);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(historyList);
        rvHistory.setAdapter(historyAdapter);

        userId = FirebaseAuth.getInstance().getUid();
        db = FirebaseFirestore.getInstance();

        loadAnalytics();
    }

    // Основная логика мониторинга и аналитики
    private void loadAnalytics() {
        db.collection("users").document(userId).collection("medicines")
                .get().addOnSuccessListener(medDocs -> {
                    final int[] total = {0};
                    final int[] taken = {0};
                    final int[] missed = {0};
                    int streak = 0;
                    int maxStreak = 0;
                    Map<String, Integer> daySuccess = new TreeMap<>();
                    Map<String, Boolean> streakDays = new TreeMap<>();
                    historyList.clear();

                    // Сбор истории
                    List<Task<QuerySnapshot>> intakeTasks = new ArrayList<>();
                    for (DocumentSnapshot medDoc : medDocs) {
                        String medName = medDoc.getString("name");
                        intakeTasks.add(medDoc.getReference().collection("intakes").get());
                    }

                    Tasks.whenAllSuccess(intakeTasks).addOnSuccessListener(results -> {
                        for (Object result : results) {
                            QuerySnapshot qs = (QuerySnapshot) result;
                            for (DocumentSnapshot intake : qs.getDocuments()) {
                                String datetime = intake.getString("datetime");
                                Boolean status = intake.getBoolean("status");
                                String medName = intake.getString("medicine");
                                if (datetime == null || status == null) continue;
                                String date = datetime.split(" ")[0];
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    daySuccess.put(date, daySuccess.getOrDefault(date, 0) + (status ? 1 : 0));
                                }
                                streakDays.put(date, status);
                                if (status) taken[0]++; else missed[0]++;
                                total[0]++;

                                // Для истории последних событий
                                historyList.add(new HistoryItem(datetime, medName, status));
                            }
                        }

                        // -- Прогрессбар
                        int percent = total[0] == 0 ? 0 : 100 * taken[0] / total[0];
                        progressBarMed.setMax(100); progressBarMed.setProgress(percent);
                        tvMedProgress.setText("Принято: " + taken[0] + "/" + total[0] + " (" + percent + "%)");

                        // -- PieChart
                        ArrayList<PieEntry> pieEntries = new ArrayList<>();
                        pieEntries.add(new PieEntry(taken[0], "Принято"));
                        pieEntries.add(new PieEntry(missed[0], "Пропущено"));
                        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
                        pieDataSet.setColors(new int[]{Color.parseColor("#22BE87"), Color.RED});
                        pieChart.setData(new PieData(pieDataSet));
                        pieChart.invalidate();

                        // -- LineChart: динамика за 7 дней
                        ArrayList<Entry> entries = new ArrayList<>();
                        List<String> days = new ArrayList<>(daySuccess.keySet());
                        int n = days.size() > 7 ? 7 : days.size();
                        for (int i = days.size() - n; i < days.size(); i++) {
                            entries.add(new Entry(i-(days.size()-n), daySuccess.get(days.get(i))));
                        }
                        LineDataSet lineDataSet = new LineDataSet(entries, "Приёмы за неделю");
                        lineDataSet.setColor(Color.parseColor("#22BE87"));
                        lineDataSet.setCircleColor(Color.parseColor("#22BE87"));
                        lineDataSet.setLineWidth(2f);
                        LineData lineData = new LineData(lineDataSet);
                        lineChart.setData(lineData);
                        lineChart.getDescription().setText("");
                        lineChart.invalidate();

                        // -- Календарь
                        calendarView.removeDecorators();
                        for (String date : streakDays.keySet()) {
                            boolean success = streakDays.get(date);
                            try {
                                Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
                                if (d != null) {
                                    CalendarDay day = CalendarDay.from(d);
                                    calendarView.addDecorator(new DayDecorator(day, success));
                                }
                            } catch (Exception ignore) {}
                        }

                        // -- Стрик и бейджи
                        int currentStreak = 0, bestStreak = 0;
                        for (String date : days) {
                            if (streakDays.get(date) != null && streakDays.get(date)) currentStreak++;
                            else currentStreak = 0;
                            bestStreak = Math.max(bestStreak, currentStreak);
                        }
                        badgesContainer.removeAllViews();
                        if (bestStreak >= 3) addBadge("Streak 3🔥");
                        if (bestStreak >= 7) addBadge("Streak 7🔥");
                        if (bestStreak >= 14) addBadge("Streak 14🔥");
                        if (bestStreak >= 30) addBadge("Streak 30🔥");

                        // -- Рекомендации
                        if (percent < 60)
                            tvHint.setText("Внимание: частые пропуски! Рекомендуется включить дополнительные напоминания.");
                        else if (bestStreak > 0)
                            tvHint.setText("Отлично! Ваш рекорд без пропусков: " + bestStreak + " дней.");
                        else
                            tvHint.setText("");

                        // -- История
                        Collections.sort(historyList, (a, b) -> b.datetime.compareTo(a.datetime));
                        if (historyList.size() > 10) historyList.subList(10, historyList.size()).clear();
                        historyAdapter.notifyDataSetChanged();
                    });
                });
    }

    private void addBadge(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#FFA000"));
        tv.setTextSize(18);
        tv.setPadding(12, 8, 12, 8);
        tv.setBackgroundResource(R.drawable.badge_bg);
        badgesContainer.addView(tv);
    }

    // --- Класс декоратора дня для календаря ---
    private static class DayDecorator implements DayViewDecorator {
        private final CalendarDay date;
        private final boolean isSuccess;
        DayDecorator(CalendarDay date, boolean isSuccess) { this.date = date; this.isSuccess = isSuccess; }
        @Override public boolean shouldDecorate(CalendarDay day) { return day.equals(date); }
        @Override public void decorate(DayViewFacade view) {
            int color = isSuccess ? Color.parseColor("#22BE87") : Color.RED;
            view.addSpan(new DotSpan(8, color));
        }
    }

    // --- История последних событий ---
    static class HistoryItem {
        String datetime, medName; boolean status;
        HistoryItem(String dt, String n, boolean s) { datetime=dt; medName=n; status=s; }
    }
    static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        List<HistoryItem> data;
        HistoryAdapter(List<HistoryItem> d) { data=d; }
        @Override public ViewHolder onCreateViewHolder(ViewGroup p, int v) {
            View v1 = LinearLayout.inflate(p.getContext(), R.layout.item_history, null);
            return new ViewHolder(v1);
        }
        @Override public void onBindViewHolder(ViewHolder h, int pos) {
            HistoryItem i = data.get(pos);
            h.title.setText(i.medName + " — " + (i.status ? "✅" : "❌"));
            h.time.setText(i.datetime);
        }
        @Override public int getItemCount() { return data.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, time;
            ViewHolder(View v) { super(v); title = v.findViewById(R.id.tvEventTitle); time = v.findViewById(R.id.tvEventTime);}
        }
    }
}
