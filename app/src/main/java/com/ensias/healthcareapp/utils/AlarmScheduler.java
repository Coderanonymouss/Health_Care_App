package com.ensias.healthcareapp.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ensias.healthcareapp.patient.medicine.receiver.MedicineReminderReceiver;

public class AlarmScheduler {
    // Установить напоминание о приёме лекарства
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleMedicineReminder(Context context, String medName, String dateTimeKey, long triggerAtMillis) {
        Intent intent = new Intent(context, MedicineReminderReceiver.class);
        intent.putExtra("medName", medName);
        intent.putExtra("dateTimeKey", dateTimeKey);

        // Делаем уникальный requestCode — например, на основе хеша даты и имени лекарства
        int requestCode = (medName + dateTimeKey).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Используем setExactAndAllowWhileIdle для точного срабатывания (Android 6+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }
}
