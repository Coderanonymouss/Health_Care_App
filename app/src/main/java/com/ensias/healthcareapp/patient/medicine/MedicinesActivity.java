package com.ensias.healthcareapp.patient.medicine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Medicine;
import com.ensias.healthcareapp.patient.medicine.adapter.MedicineAdapter;
import com.ensias.healthcareapp.patient.medicine.receiver.MissedIntakeReceiver;
import com.ensias.healthcareapp.patient.medicine.receiver.MedicineReminderReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Бұл экран – пациенттің дәрі-дәрмектер тізімін, қабылдау уақыттарын,
 * қабылданғанын/қабылданбағанын және ескертулерді басқарады.
 */
public class MedicinesActivity extends AppCompatActivity {

    // UI компоненттер
    private ProgressBar progressBar;
    private final List<Medicine> medicineList = new ArrayList<>();
    private MedicineAdapter adapter;

    // Жергілікті және қашықтағы сақтау
    private SharedPreferences prefs;
    private FirebaseFirestore db;
    public String userId;

    // Android 13+ үшін уведомление рұқсаты
    private static final int REQ_NOTIF = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_medicine);

        // Тіркелген пайдаланушының uid
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("MedPrefs_" + userId, MODE_PRIVATE);

        // UI-ды байланыстыру
        RecyclerView recyclerView = findViewById(R.id.recyclerViewMedicines);
        progressBar = findViewById(R.id.progressBar);
        FloatingActionButton fab = findViewById(R.id.fabAddMedicine);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineAdapter(this, medicineList, this::deleteMedicine, this, userId);
        recyclerView.setAdapter(adapter);

        // Android 13+ үшін уведомление рұқсат сұрау
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission();
        }

        // Деректерді жүктеу
        fetchMedicinesFromFirestore();

        // Дәрі қосу батырмасы
        fab.setOnClickListener(v -> showAddMedicineDialog());

        // Toolbar және "артқа" батырмасы
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Android 13 (TIRAMISU) және жоғары үшін уведомление рұқсатын сұрайды
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
        }
    }

    /**
     * Қабылдаудың статусын (қабылданды/қабылданбады) жергілікті SharedPreferences-қа сақтайды
     */
    public void saveIntakeLocally(String medId, String dateTimeKey, boolean status) {
        String key = medId + "_" + dateTimeKey;
        prefs.edit().putBoolean(key, status).apply();
    }

    /**
     * Қабылдау статусын жергілікті сақтау орнынан оқиды
     */
    public Boolean getIntakeStatusLocally(String medId, String dateTimeKey) {
        String key = medId + "_" + dateTimeKey;
        return prefs.contains(key) ? prefs.getBoolean(key, false) : null;
    }

    /**
     * Firestore-дан барлық дәрілерді алады
     */
    @SuppressLint("NotifyDataSetChanged")
    private void fetchMedicinesFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users")
                .document(userId)
                .collection("medicines")
                .get()
                .addOnSuccessListener(snapshot -> {
                    medicineList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Medicine med = doc.toObject(Medicine.class);
                        if (med != null) medicineList.add(med);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    loadMedicines();
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });
    }

    /**
     * Барлық дәрілерді жергілікті SharedPreferences-қа сериализациялайды
     */
    public void saveMedicines() {
        String json = new com.google.gson.Gson().toJson(medicineList);
        prefs.edit().putString("medicines", json).apply();
    }

    /**
     * Дәрілерді SharedPreferences-тен жүктеу
     */
    private void loadMedicines() {
        String json = prefs.getString("medicines", null);
        if (json != null) {
            try {
                Type type = new TypeToken<List<Medicine>>() {}.getType();
                List<Medicine> saved = new com.google.gson.Gson().fromJson(json, type);
                if (saved != null) {
                    medicineList.clear();
                    medicineList.addAll(saved);
                }
            } catch (Exception e) {
                Log.e("GSON", "Ошибка загрузки", e);
            }
        }
    }

    /**
     * Дәріні Firestore-ға сақтайды
     */
    private void saveMedicineToFirestore(Medicine med) {
        db.collection("users")
                .document(userId)
                .collection("medicines")
                .document(med.getId())
                .set(med)
                .addOnSuccessListener(a -> Log.d("FIREBASE", "Medicine saved: " + med.getName()))
                .addOnFailureListener(e -> Log.e("FIREBASE", "Save failed", e));
    }

    /**
     * Дәрі қосу үшін диалог көрсетеді
     */
    @SuppressLint("DefaultLocale")
    private void showAddMedicineDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medicine, null);
        EditText etName = dialogView.findViewById(R.id.etMedName);
        EditText etDosage = dialogView.findViewById(R.id.etMedDosage);
        EditText etTimesPerDay = dialogView.findViewById(R.id.etTimesPerDay);
        EditText etDuration = dialogView.findViewById(R.id.etDurationDays);
        LinearLayout timeContainer = dialogView.findViewById(R.id.timePickerContainer);

        // Күніне қанша рет қабылдау керектігін енгізген кезде автоматты түрде TimePicker қосу
        etTimesPerDay.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                timeContainer.removeAllViews();
                int count = 0;
                try { count = Integer.parseInt(s.toString()); }
                catch (Exception ignore) {}
                count = Math.min(count, 5);

                for (int i = 0; i < count; i++) {
                    TimePicker tp = new TimePicker(MedicinesActivity.this);
                    tp.setIs24HourView(true);
                    int defaultHour = 8 + i * 4;
                    if (defaultHour > 22) defaultHour = 22;
                    tp.setHour(defaultHour);
                    tp.setMinute(0);
                    timeContainer.addView(tp);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Диалог құрылған соң "Сақтау" батырмасымен дәріні қосу
        new AlertDialog.Builder(this)
                .setTitle("Дәрі қосу")
                .setView(dialogView)
                .setPositiveButton("Сақтау", (dlg, which) -> {
                    String name = etName.getText().toString().trim();
                    String dosage = etDosage.getText().toString().trim();
                    String timesStr = etTimesPerDay.getText().toString().trim();
                    String daysStr = etDuration.getText().toString().trim();

                    if (name.isEmpty() || dosage.isEmpty()
                            || timesStr.isEmpty() || daysStr.isEmpty()) {
                        Toast.makeText(this, "Барлық өрістерді толтырыңыз", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int timesPerDay, durationDays;
                    try {
                        timesPerDay = Integer.parseInt(timesStr);
                        durationDays = Integer.parseInt(daysStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Дұрыс сан енгізіңіз", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    timesPerDay = Math.min(timesPerDay, 5);

                    if (timeContainer.getChildCount() != timesPerDay) {
                        timeContainer.removeAllViews();
                        for (int i = 0; i < timesPerDay; i++) {
                            TimePicker tp = new TimePicker(this);
                            tp.setIs24HourView(true);
                            int defaultHour = 8 + i * 4;
                            if (defaultHour > 22) defaultHour = 22;
                            tp.setHour(defaultHour);
                            tp.setMinute(0);
                            timeContainer.addView(tp);
                        }
                    }

                    List<Calendar> timesForAlarm = new ArrayList<>();
                    List<String> stringTimes = new ArrayList<>();
                    for (int i = 0; i < timeContainer.getChildCount(); i++) {
                        View v = timeContainer.getChildAt(i);
                        if (v instanceof TimePicker tp) {
                            int h = tp.getHour(), m = tp.getMinute();
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.HOUR_OF_DAY, h);
                            cal.set(Calendar.MINUTE, m);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            if (cal.before(Calendar.getInstance())) {
                                cal.add(Calendar.DAY_OF_MONTH, 1);
                            }
                            timesForAlarm.add(cal);
                            stringTimes.add(String.format("%02d:%02d", h, m));
                        }
                    }

                    String medId = UUID.randomUUID().toString();
                    Medicine med = new Medicine(
                            medId, name, dosage,
                            timesPerDay, durationDays,
                            stringTimes
                    );
                    medicineList.add(med);
                    adapter.notifyItemInserted(medicineList.size() - 1);

                    saveMedicines();
                    saveMedicineToFirestore(med);

                    scheduleReminders(medId, name, timesForAlarm, durationDays);
                })
                .setNegativeButton("Артқа", null)
                .show();
    }

    /**
     * Қабылдаудың фактін Firestore-ға және жергілікті базаға сақтайды
     */
    public void saveIntakeToFirestore(String medId, String medName, String dateTimeKey, boolean status) {
        Map<String,Object> data = new HashMap<>();
        data.put("medicine", medName);
        data.put("datetime", dateTimeKey);
        data.put("status", status);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .collection("medicines")
                .document(medId)
                .collection("intakes")
                .document(medName + "_" + dateTimeKey)
                .set(data)
                .addOnSuccessListener(a -> {
                    saveIntakeLocally(medId, dateTimeKey, status);
                    Log.d("FIREBASE","✅ Intake saved");
                })
                .addOnFailureListener(e -> Log.e("FIREBASE","❌ Intake save failed",e));
    }

    /**
     * Дәрі қабылдауы туралы ескерту жасау (AlarmManager арқылы)
     */
    @SuppressLint("ScheduleExactAlarm")
    public void scheduleReminders(String medId, String medName, List<Calendar> intakeTimes, int durationDays) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        for (Calendar intakeTime : intakeTimes) {
            for (int day = 0; day < durationDays; day++) {
                Calendar alarmCal = (Calendar) intakeTime.clone();
                alarmCal.add(Calendar.DAY_OF_MONTH, day);

                // Негізгі ескерту (лекарство қабылдау уақыты)
                Intent reminderIntent = new Intent(this, MedicineReminderReceiver.class);
                reminderIntent.putExtra("medName", medName);
                String dateTimeKey = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(alarmCal.getTime());
                reminderIntent.putExtra("dateTimeKey", dateTimeKey);

                int reqCode = (medId + "_" + dateTimeKey).hashCode();
                PendingIntent pi = PendingIntent.getBroadcast(
                        this, reqCode, reminderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarmCal.getTimeInMillis(),
                            pi
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            alarmCal.getTimeInMillis(),
                            pi
                    );
                }

                // Қабылдау уақыты өтіп кеткен жағдайда ескерту
                Calendar missedCal = (Calendar) alarmCal.clone();
                missedCal.add(Calendar.MINUTE, 60);

                Intent missedIntent = new Intent(this, MissedIntakeReceiver.class);
                missedIntent.putExtra("medId", medId);
                missedIntent.putExtra("medName", medName);
                missedIntent.putExtra("dateTimeKey", dateTimeKey);

                int missedReqCode = (medId + "_" + dateTimeKey + "_missed").hashCode();
                PendingIntent missedPi = PendingIntent.getBroadcast(
                        this, missedReqCode, missedIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        missedCal.getTimeInMillis(),
                        missedPi
                );
            }
        }
    }

    /**
     * Дәріні және барлық байланысты ескертулер мен тарихты өшіреді
     */
    private void deleteMedicine(Medicine med, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить лекарство?")
                .setMessage("Все напоминания и история будут удалены!")
                .setPositiveButton("Удалить", (d, w) -> {
                    cancelAllReminders(med);
                    db.collection("users")
                            .document(userId)
                            .collection("medicines")
                            .document(med.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                medicineList.remove(position);
                                saveMedicines();
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(this, "Лекарство удалено", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    /**
     * Дәріге байланысты барлық ескертулерді (AlarmManager) өшіреді
     */
    private void cancelAllReminders(Medicine med) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        for (String time : med.getTimes()) {
            for (int day = 0; day < med.getDurationDays(); day++) {
                Calendar cal = Calendar.getInstance();
                String[] arr = time.split(":");
                int h = Integer.parseInt(arr[0]);
                int m = Integer.parseInt(arr[1]);
                cal.set(Calendar.HOUR_OF_DAY, h);
                cal.set(Calendar.MINUTE, m);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.add(Calendar.DAY_OF_MONTH, day);
                int req = (med.getId() + "_" + cal.getTimeInMillis()).hashCode();
                Intent i = new Intent(this, MedicineReminderReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(
                        this, req, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
                );
                if (pi != null) alarmManager.cancel(pi);

                int mreq = (med.getId() + "_" + cal.getTimeInMillis() + "_missed").hashCode();
                Intent mi = new Intent(this, MissedIntakeReceiver.class);
                PendingIntent mpi = PendingIntent.getBroadcast(
                        this, mreq, mi, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
                );
                if (mpi != null) alarmManager.cancel(mpi);
            }
        }
    }
}
