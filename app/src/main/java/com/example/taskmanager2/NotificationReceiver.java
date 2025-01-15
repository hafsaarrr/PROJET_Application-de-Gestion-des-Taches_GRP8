package com.example.taskmanager2;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("taskId", -1);
        String taskTitle = intent.getStringExtra("taskTitle");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "TaskManagerChannel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // Example default icon
                .setContentTitle("Task Due Soon")
                .setContentText("Task \"" + taskTitle + "\" is due in 5 minutes!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(taskId, builder.build());
    }
}
