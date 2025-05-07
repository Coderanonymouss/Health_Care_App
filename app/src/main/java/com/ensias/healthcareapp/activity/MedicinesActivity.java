package com.ensias.healthcareapp.activity;

import static kotlin.text.Typography.times;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.adapter.MedicineAdapter;
import com.ensias.healthcareapp.model.Medicine;
import com.ensias.healthcareapp.receiver.MedicineReminderReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.widget.EditText;
import android.widget.TimePicker;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MedicinesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private List<Medicine> medicineList = new ArrayList<>();
    private MedicineAdapter adapter;
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_medicine);

        recyclerView = findViewById(R.id.recyclerViewMedicines);
        fab = findViewById(R.id.fabAddMedicine);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        prefs = getSharedPreferences("MedPrefs", MODE_PRIVATE);
        loadMedicines();

        adapter = new MedicineAdapter(this, medicineList);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> showAddMedicineDialog());
    }


    public void saveMedicines() {
        String json = new Gson().toJson(medicineList);
        prefs.edit().putString("medicines", json).apply();
    }

    private void loadMedicines() {
        String json = prefs.getString("medicines", null);
        if (json != null) {
            Type type = new TypeToken<List<Medicine>>() {}.getType();
            List<Medicine> saved = new Gson().fromJson(json, type);
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

        etTimesPerDay.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                timeContainer.removeAllViews();

                int count;
                try {
                    count = Integer.parseInt(etTimesPerDay.getText().toString());
                } catch (NumberFormatException e) {
                    count = 0;
                }

                for (int i = 0; i < count; i++) {
                    TimePicker tp = new TimePicker(this);
                    tp.setIs24HourView(true);
                    timeContainer.addView(tp);
                }
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Добавить лекарство")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String dosage = etDosage.getText().toString().trim();
                    String timesStr = etTimesPerDay.getText().toString().trim();
                    String daysStr = etDurationDays.getText().toString().trim();

                    if (name.isEmpty() || dosage.isEmpty() || timesStr.isEmpty() || daysStr.isEmpty()) {
                        Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int timesPerDay = Integer.parseInt(timesStr);
                    int durationDays = Integer.parseInt(daysStr);

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

                            timesForAlarm.add(cal); // для будильника
                            stringTimes.add(String.format("%02d:%02d", hour, minute)); // для сохранения
                        }
                    }

                    Medicine med = new Medicine(name, dosage, stringTimes); // ✅ теперь правильно
 // где times — List<String>
                    medicineList.add(med);
                    adapter.notifyItemInserted(medicineList.size() - 1);
                    saveMedicines();

                    scheduleRemindersCustomTimes(name, timesForAlarm, durationDays);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleRemindersCustomTimes(String medName, List<Calendar> timesPerDay, int durationDays) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Проверка разрешения на точные напоминания
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Разрешите точные напоминания в настройках", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        for (int day = 0; day < durationDays; day++) {
            for (Calendar baseTime : timesPerDay) {
                Calendar cal = (Calendar) baseTime.clone();
                cal.add(Calendar.DAY_OF_MONTH, day);

                Intent intent = new Intent(this, MedicineReminderReceiver.class);
                intent.putExtra("medName", medName);
                int reqCode = (medName + cal.getTimeInMillis()).hashCode();
                PendingIntent pi = PendingIntent.getBroadcast(this, reqCode, intent, PendingIntent.FLAG_IMMUTABLE);

                // Здесь безопасно использовать setExact
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            }
        }

        Toast.makeText(this, "Напоминания установлены", Toast.LENGTH_LONG).show();
    }
}