package com.ensias.healthcareapp.patient.medicine.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.patient.medicine.MedicinesActivity;

public class MedicineReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "med_reminders";
    private static final String CHANNEL_NAME = "Напоминания о лекарствах";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MedicineReminderReceiver", "onReceive called!"); // <-- ДОБАВЬ ЭТО!
        Toast.makeText(context, "onReceive called!", Toast.LENGTH_LONG).show(); // на время проверки!

        String medName = intent.getStringExtra("medName");
        String dateTimeKey = intent.getStringExtra("dateTimeKey");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Создать канал для уведомлений (только для Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Уведомления о приёме лекарств");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        // Интент на открытие MedicinesActivity при нажатии
        Intent openIntent = new Intent(context, MedicinesActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openPending = PendingIntent.getActivity(
                context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "Уақыты келді! 💊";
        String text = (medName != null && !medName.isEmpty())
                ? "Дәріні қабылдаңыз: " + medName
                : "Дәрі қабылдау уақыты келді!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo) // Положи подходящую иконку в drawable
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(openPending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(new long[]{0, 300, 300, 300}) // Вибрация
                .setLights(Color.BLUE, 1000, 2000); // Мерцание LED (если поддерживается)
        Toast.makeText(context, "Напоминание о приёме: " + medName, Toast.LENGTH_LONG).show();
        int notifId = (medName + "_" + dateTimeKey).hashCode();
        manager.notify(notifId, builder.build());
    }
}
