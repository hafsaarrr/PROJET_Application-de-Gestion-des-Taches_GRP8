package com.example.taskmanager2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private Context context;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
        void onDeleteClick(Task task);
    }

    public TaskAdapter(Context context, List<Task> taskList, OnTaskClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Set title
        holder.titleText.setText(task.getTitle());

        // Format and set date with time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = dateFormat.format(new Date(task.getDueDate()));
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d",
                task.getDueHours(),
                task.getDueMinutes());
        holder.dateText.setText(String.format("%s at %s", dateStr, timeStr));

        // Set priority with more detailed information
        String priorityLabel;
        switch (task.getPriority()) {
            case 1:
                priorityLabel = "High Priority";
                break;
            case 2:
                priorityLabel = "Medium Priority";
                break;
            case 3:
                priorityLabel = "Low Priority";
                break;
            default:
                priorityLabel = "Priority: " + task.getPriority();
        }
        holder.priorityText.setText(priorityLabel);

        // Set category
        holder.categoryText.setText(task.getCategory());

        // Delete button handler
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Task taskToDelete = taskList.get(adapterPosition);
                    listener.onDeleteClick(taskToDelete);
                }
            }
        });

        // Set background color based on priority
        int color;
        switch (task.getPriority()) {
            case 1:
                color = ContextCompat.getColor(context, R.color.priority_high);
                break;
            case 2:
                color = ContextCompat.getColor(context, R.color.priority_medium);
                break;
            case 3:
                color = ContextCompat.getColor(context, R.color.priority_low);
                break;
            default:
                color = ContextCompat.getColor(context, R.color.priority_low);
        }
        holder.itemView.setBackgroundColor(color);

        // Optional: Add visual indication for tasks due soon
        if (isTaskDueSoon(task)) {
            holder.dateText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        } else {
            holder.dateText.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }
    }

    private boolean isTaskDueSoon(Task task) {
        long currentTime = System.currentTimeMillis();
        // Consider task due soon if it's within 24 hours
        return (task.getDueDate() - currentTime) <= 24 * 60 * 60 * 1000;
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> newTasks) {
        taskList.clear();
        taskList.addAll(newTasks);
        notifyDataSetChanged();
    }

    public void removeTask(Task task) {
        int position = taskList.indexOf(task);
        if (position != -1) {
            taskList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, taskList.size());
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, dateText, priorityText, categoryText;
        ImageButton deleteButton;

        TaskViewHolder(View view) {
            super(view);
            titleText = view.findViewById(R.id.task_title);
            dateText = view.findViewById(R.id.task_date);
            priorityText = view.findViewById(R.id.task_priority);
            categoryText = view.findViewById(R.id.task_category);
            deleteButton = view.findViewById(R.id.btn_delete);

            view.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(taskList.get(position));
                }
            });

            view.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTaskLongClick(taskList.get(position));
                    return true;
                }
                return false;
            });
        }
    }
}