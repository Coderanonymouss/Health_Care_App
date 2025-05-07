package com.ensias.healthcareapp.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

    public void saveMedicines() {
        String json = gson.toJson(medicineList);
        prefs.edit().putString("medicines", json).apply();
    }


    private void showAddMedicineDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medicine, null);
        EditText etName = dialogView.findViewById(R.id.etMedName);
        EditText etDosage = dialogView.findViewById(R.id.etMedDosage);
        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        new AlertDialog.Builder(this)
                .setTitle("Добавить лекарство")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String dosage = etDosage.getText().toString().trim();
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();

                    if (name.isEmpty() || dosage.isEmpty()) {
                        Toast.makeText(this, "Поля не должны быть пустыми", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String timeStr = String.format("%02d:%02d", hour, minute);
                    Medicine med = new Medicine(name, dosage, timeStr);
                    medicineList.add(med);
                    adapter.notifyItemInserted(medicineList.size() - 1);
                    saveMedicines();
                    scheduleMedicineReminder(name, hour, minute);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void scheduleMedicineReminder(String medName, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1); // На завтра
        }

        Intent intent = new Intent(this, MedicineReminderReceiver.class);
        intent.putExtra("medName", medName);

        PendingIntent pi = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);

        Toast.makeText(this, "Напоминание установлено на " + String.format("%02d:%02d", hour, minute), Toast.LENGTH_SHORT).show();
    }
}