package com.ensias.healthcareapp.patient.receiver;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ensias.healthcareapp.R;

public class MedicineReminderReceiver extends BroadcastReceiver {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public void onReceive(Context context, Intent intent) {
        String medName = intent.getStringExtra("medName");
        Log.d("REMINDER_DEBUG", "onReceive: Уведомление пришло!");
        // Действие для "ҚАБЫЛДАУ"
        Intent takenIntent = new Intent(context, MedicineTakenReceiver.class);
        takenIntent.setAction("com.ensias.healthcareapp.ACTION_TAKE_MEDICINE");
        takenIntent.putExtra("medName", medName);

        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                context, (medName + "taken").hashCode(), takenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "med_reminders",
                    "Дәрі еске салғыш",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "med_reminders")
                .setSmallIcon(R.drawable.ic_medicine)
                .setContentTitle("Дәрі ішу уақыты")
                .setContentText(medName + " дәрісін ішу уақыты келді!")
                .addAction(R.drawable.ic_done, "ҚАБЫЛДАУ", takenPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify((medName + System.currentTimeMillis()).hashCode(), builder.build());
    }
}
