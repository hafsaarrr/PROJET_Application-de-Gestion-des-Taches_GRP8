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

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private Context context;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
        void onDeleteClick(Task task); // Nouvelle méthode
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
        holder.titleText.setText(task.getTitle());
        holder.dateText.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date(task.getDueDate())));
        holder.priorityText.setText("Priority: " + task.getPriority());
        holder.categoryText.setText(task.getCategory());

        // Gestionnaire du clic sur le bouton de suppression
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
            case 3:
                color = ContextCompat.getColor(context, R.color.priority_low);
                break;
            case 2:
                color = ContextCompat.getColor(context, R.color.priority_medium);
                break;
            case 1:
                color = ContextCompat.getColor(context, R.color.priority_high);
                break;
            default:
                color = ContextCompat.getColor(context, R.color.priority_low);
        }
        holder.itemView.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> newTasks) {
        taskList.clear(); // Vider la liste actuelle
        taskList.addAll(newTasks); // Ajouter les nouvelles tâches
        notifyDataSetChanged(); // Rafraîchir l'affichage
    }

    // Alternative plus efficace avec animation :
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
