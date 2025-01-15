package com.example.taskmanager2;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class NotificationHelper {
    private static final String CHANNEL_ID = "TaskManagerChannel";
    private static final String CHANNEL_NAME = "Task Notifications";
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
                    NotificationManager.IMPORTANCE_HIGH  // Set high importance for important notifications
            );
            channel.setDescription("Notifications for upcoming tasks");
            channel.enableVibration(true);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public void scheduleNotification(Task task) {
        long currentTime = System.currentTimeMillis();
        long taskDueTime = task.getDueDate();
        if (taskDueTime <= currentTime) {
            return;  // Don't schedule notifications for past-due tasks
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("taskId", task.getId());
        intent.putExtra("taskTitle", task.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar taskCalendar = Calendar.getInstance();
        taskCalendar.setTimeInMillis(task.getDueDate());
        taskCalendar.set(Calendar.HOUR_OF_DAY, task.getDueHours());
        taskCalendar.set(Calendar.MINUTE, task.getDueMinutes());
        long notificationTime = taskCalendar.getTimeInMillis() - (5 * 60 * 1000);  // Notify 5 minutes before

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
            );
        }
    }
}
