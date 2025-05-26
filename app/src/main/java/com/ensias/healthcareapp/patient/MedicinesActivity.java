package com.ensias.healthcareapp.patient;

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
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.patient.adapter.MedicineAdapter;
import com.ensias.healthcareapp.model.Medicine;
import com.ensias.healthcareapp.receiver.MedicineReminderReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MedicinesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private List<Medicine> medicineList = new ArrayList<>();
    private MedicineAdapter adapter;
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    private static final int REQ_NOTIF = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_medicine);

        recyclerView = findViewById(R.id.recyclerViewMedicines);
        fab = findViewById(R.id.fabAddMedicine);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Уникальный SharedPreferences на каждого пользователя
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences("MedPrefs_" + userId, MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission();
        }
        LinearLayout rootLayout = findViewById(R.id.rootLayout);
        Button btnTest = new Button(this);
        btnTest.setText("ТЕСТ УВЕДОМЛЕНИЯ");
        btnTest.setAllCaps(false);

        rootLayout.addView(btnTest);

        btnTest.setOnClickListener(v -> {
            android.util.Log.d("REMINDER_DEBUG", "Тестовая кнопка нажата");
            Intent i = new Intent(this, MedicineReminderReceiver.class);
            i.putExtra("medName", "Тестовое лекарство");
            sendBroadcast(i);
        });
        loadMedicines();

        adapter = new MedicineAdapter(this, medicineList);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> showAddMedicineDialog());
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
        }
    }

    public void saveMedicines() {
        String json = gson.toJson(medicineList);
        prefs.edit().putString("medicines", json).apply();
    }

    private void loadMedicines() {
        String json = prefs.getString("medicines", null);
        if (json != null) {
            Type type = new TypeToken<List<Medicine>>() {}.getType();
            List<Medicine> saved = gson.fromJson(json, type);
            if (saved != null) {
                medicineList.clear();
                medicineList.addAll(saved);
            }
        }
    }

    private void showAddMedicineDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medicine, null);
        EditText etName = dialogView.findViewById(R.id.etMedName);
        EditText etDosage = dialogView.findViewById(R.id.etMedDosage);
        EditText etTimesPerDay = dialogView.findViewById(R.id.etTimesPerDay);
        EditText etDurationDays = dialogView.findViewById(R.id.etDurationDays);
        LinearLayout timeContainer = dialogView.findViewById(R.id.timePickerContainer);

        // Вставка таймпикеров при изменении количества
        etTimesPerDay.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                timeContainer.removeAllViews();
                int countVal = 0;
                try { countVal = Integer.parseInt(s.toString()); } catch (Exception ignore) {}
                for (int i = 0; i < countVal; i++) {
                    TimePicker tp = new TimePicker(MedicinesActivity.this);
                    tp.setIs24HourView(true);
                    timeContainer.addView(tp);
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Дәрі қосу")
                .setView(dialogView)
                .setPositiveButton("Сақтау", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String dosage = etDosage.getText().toString().trim();
                    String timesStr = etTimesPerDay.getText().toString().trim();
                    String daysStr = etDurationDays.getText().toString().trim();

                    if (name.isEmpty() || dosage.isEmpty() || timesStr.isEmpty() || daysStr.isEmpty()) {
                        Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int timesPerDay;
                    int durationDays;
                    try {
                        timesPerDay = Integer.parseInt(timesStr);
                        durationDays = Integer.parseInt(daysStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Введите корректные числа", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Если таймпикеры не были созданы — создаём их
                    if (timeContainer.getChildCount() != timesPerDay) {
                        timeContainer.removeAllViews();
                        for (int i = 0; i < timesPerDay; i++) {
                            TimePicker tp = new TimePicker(this);
                            tp.setIs24HourView(true);
                            timeContainer.addView(tp);
                        }
                    }

                    List<Calendar> timesForAlarm = new ArrayList<>();
                    List<String> stringTimes = new ArrayList<>();
                    for (int i = 0; i < timeContainer.getChildCount(); i++) {
                        View view = timeContainer.getChildAt(i);
                        if (view instanceof TimePicker) {
                            TimePicker tp = (TimePicker) view;
                            int hour = tp.getHour();
                            int minute = tp.getMinute();

                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.HOUR_OF_DAY, hour);
                            cal.set(Calendar.MINUTE, minute);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);

                            if (cal.before(Calendar.getInstance())) {
                                cal.add(Calendar.DAY_OF_MONTH, 1);
                            }

                            timesForAlarm.add(cal);
                            stringTimes.add(String.format("%02d:%02d", hour, minute));
                        }
                    }

                    if (stringTimes.isEmpty()) {
                        Toast.makeText(this, "Выберите хотя бы одно время приёма!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Medicine med = new Medicine(name, dosage, stringTimes);
                    medicineList.add(med);
                    adapter.notifyItemInserted(medicineList.size() - 1);
                    saveMedicines();

                    scheduleReminders(name, timesForAlarm, durationDays);
                })
                .setNegativeButton("Артқа", null)
                .show();
    }

    private void scheduleReminders(String medName, List<Calendar> times, int durationDays) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                android.util.Log.d("REMINDER_DEBUG", "NO EXact alarm permission!");
                Toast.makeText(this, "Включите точные напоминания в настройках", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        for (int day = 0; day < durationDays; day++) {
            for (Calendar baseTime : times) {
                Calendar cal = (Calendar) baseTime.clone();
                cal.add(Calendar.DAY_OF_MONTH, day);

                Intent intent = new Intent(this, MedicineReminderReceiver.class);
                intent.putExtra("medName", medName);
                int reqCode = (medName + cal.getTimeInMillis()).hashCode();

                PendingIntent pi = PendingIntent.getBroadcast(this, reqCode, intent, PendingIntent.FLAG_IMMUTABLE);

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);

                android.util.Log.d("REMINDER_DEBUG", "Alarm set: " + medName + " for " + cal.getTime() + " reqCode=" + reqCode + " now=" + System.currentTimeMillis() + " calMillis=" + cal.getTimeInMillis());
            }
        }

        Toast.makeText(this, "Напоминания установлены", Toast.LENGTH_SHORT).show();
    }

}
