package com.example.taskmanager2;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public class NotificationHelper {
    private static final String CHANNEL_ID = "task_notification_channel";
    private static final String CHANNEL_NAME = "Task Notifications";
    private static final String CHANNEL_DESC = "Notifications for tasks";
    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void scheduleNotification(Task task) {
        // Get the AlarmManager system service
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create an Intent to trigger the NotificationReceiver
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_title", task.getTitle());

        // Create a PendingIntent for the notification
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Configure the alarm time
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(task.getDueDate());
        alarmTime.set(Calendar.HOUR_OF_DAY, task.getDueHours());

        // Subtract 5 minutes and handle negative minutes
        int dueMinutes = task.getDueMinutes() - 5;
        if (dueMinutes < 0) {
            alarmTime.add(Calendar.HOUR_OF_DAY, -1); // Adjust the hour
            dueMinutes += 60; // Roll over to the previous hour
        }
        alarmTime.set(Calendar.MINUTE, dueMinutes);
        alarmTime.set(Calendar.SECOND, 0); // Set seconds to 0 for precision

        // Schedule the alarm only if the time is in the future
        if (alarmTime.getTimeInMillis() > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.getTimeInMillis(),
                    pendingIntent
            );
            Log.d("NotificationHelper", "Notification scheduled for: " + alarmTime.getTime());
        } else {
            Log.w("NotificationHelper", "Task due time is in the past. Notification not scheduled.");
        }
    }


    public void cancelNotification(Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
