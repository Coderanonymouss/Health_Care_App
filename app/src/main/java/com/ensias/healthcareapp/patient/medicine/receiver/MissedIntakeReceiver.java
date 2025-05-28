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

import androidx.core.app.NotificationCompat;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.patient.medicine.MedicinesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MissedIntakeReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "med_missed";
    private static final String CHANNEL_NAME = "Пропущенные приёмы";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MedicineReminderReceiver", "onReceive called!");

        String medId = intent.getStringExtra("medId");
        String medName = intent.getStringExtra("medName");
        String dateTimeKey = intent.getStringExtra("dateTimeKey");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (medId == null || medName == null || dateTimeKey == null || userId == null) return;

        // Проверка, не было ли уже принято лекарство (не создавать дубликаты)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("medicines")
                .document(medId)
                .collection("intakes")
                .document(medName + "_" + dateTimeKey)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Отметить как ❌ пропущено
                        Map<String, Object> data = new HashMap<>();
                        data.put("medicine", medName);
                        data.put("datetime", dateTimeKey);
                        data.put("status", false);
                        data.put("timestamp", System.currentTimeMillis());

                        db.collection("users")
                                .document(userId)
                                .collection("medicines")
                                .document(medId)
                                .collection("intakes")
                                .document(medName + "_" + dateTimeKey)
                                .set(data);

                        // Push-уведомление о пропуске
                        sendMissedNotification(context, medName, dateTimeKey);
                    }
                });
    }

    private void sendMissedNotification(Context context, String medName, String dateTimeKey) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Канал для уведомлений (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Напоминания о пропущенных приёмах лекарств");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            manager.createNotificationChannel(channel);
        }

        // Открывать MedicinesActivity при нажатии
        Intent openIntent = new Intent(context, MedicinesActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openPending = PendingIntent.getActivity(
                context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "Лекарство пропущено!";
        String text = (medName != null && !medName.isEmpty())
                ? "Вы пропустили приём: " + medName
                : "Пропущен приём лекарства!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pill)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(openPending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(Color.RED)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        int notifId = (medName + "_missed_" + dateTimeKey).hashCode();
        manager.notify(notifId, builder.build());
    }
}
