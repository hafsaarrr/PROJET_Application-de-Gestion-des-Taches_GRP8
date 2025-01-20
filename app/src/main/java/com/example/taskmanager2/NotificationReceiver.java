package com.example.taskmanager2;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "task_notification_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String taskTitle = intent.getStringExtra("taskTitle");

            // Handle null taskTitle gracefully
            if (taskTitle == null) {
                taskTitle = "Task Reminder"; // Default fallback title
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Reminder: " + taskTitle)
                    .setContentText("This is your reminder for the task.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                // Use a safe fallback for hashCode in case taskTitle was null
                notificationManager.notify(taskTitle.hashCode(), builder.build());
            }
        } else {
            // Log an error or handle the null intent scenario
            android.util.Log.e("NotificationReceiver", "Received null intent in onReceive.");
        }
    }
}
