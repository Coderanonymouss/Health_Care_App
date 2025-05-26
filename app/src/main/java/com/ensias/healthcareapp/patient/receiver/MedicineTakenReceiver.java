package com.ensias.healthcareapp.patient.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class MedicineTakenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String medName = intent.getStringExtra("medName");
            Log.d("REMINDER_DEBUG", "onReceive: MedicineTakenReceiver вызван!");
            Log.d("REMINDER_DEBUG", "MedicineTakenReceiver: medName=" + medName + ", time=" + System.currentTimeMillis());
            Toast.makeText(context, medName + " қабылданды!", Toast.LENGTH_SHORT).show();
        }
    }

