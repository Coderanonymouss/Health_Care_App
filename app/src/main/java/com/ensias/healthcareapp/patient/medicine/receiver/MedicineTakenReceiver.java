package com.ensias.healthcareapp.patient.medicine.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.ensias.healthcareapp.model.Medicine;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class MedicineTakenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String medName = intent.getStringExtra("medName");
        String dateTimeKey = intent.getStringExtra("dateTimeKey");

        SharedPreferences prefs = context.getSharedPreferences("MedPrefs_" + medName, Context.MODE_PRIVATE);
        String json = prefs.getString("medicines", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Medicine>>() {}.getType();
            List<Medicine> list = gson.fromJson(json, type);
            for (Medicine m : list) {
                if (m.getName().equals(medName)) {
                    m.getIntakeHistory().put(dateTimeKey, true);
                    break;
                }
            }
            prefs.edit().putString("medicines", gson.toJson(list)).apply();
            Toast.makeText(context, medName + " қабылданды!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Ошибка: не найдено лекарство", Toast.LENGTH_SHORT).show();
        }
    }
}