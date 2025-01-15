package com.example.taskmanager2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Task> taskList;
    private NotificationHelper notificationHelper;
    private Spinner sortSpinner;
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();
        setupRecyclerView();
        setupSpinners();

        requestNotificationPermission();
    }

    // Method to request notification permission for Android 13+
    private void requestNotificationPermission() {
        // No permission checks are necessary
        // Simply leave the method empty or remove it if not needed
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can schedule notifications now
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, show message
                Toast.makeText(this, "Notification permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeComponents() {
        dbHelper = new DatabaseHelper(this);
        notificationHelper = new NotificationHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        fab.setOnClickListener(v -> showAddTaskDialog());
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = dbHelper.getAllTasks();
        adapter = new TaskAdapter(this, taskList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSpinners() {
        sortSpinner = findViewById(R.id.sort_spinner);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this, R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortTasks(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        filterSpinner = findViewById(R.id.filter_spinner);
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this, R.array.filter_options, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterTasks(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        EditText titleEdit = view.findViewById(R.id.edit_title);
        EditText descEdit = view.findViewById(R.id.edit_description);
        DatePicker datePicker = view.findViewById(R.id.date_picker);
        TextView timeText = view.findViewById(R.id.text_time);  // Make sure this is a TextView in your XML
        Spinner prioritySpinner = view.findViewById(R.id.spinner_priority);
        Spinner categorySpinner = view.findViewById(R.id.spinner_category);

        // Initialize time selection with current time
        Calendar calendar = Calendar.getInstance();
        final int[] selectedHour = {calendar.get(Calendar.HOUR_OF_DAY)};
        final int[] selectedMinute = {calendar.get(Calendar.MINUTE)};
        updateTimeDisplay(timeText, selectedHour[0], selectedMinute[0]);

        // Setup time picker for timeText
        timeText.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view1, hourOfDay, minute) -> {
                        selectedHour[0] = hourOfDay;
                        selectedMinute[0] = minute;
                        updateTimeDisplay(timeText, hourOfDay, minute);
                    },
                    selectedHour[0],
                    selectedMinute[0],
                    true
            );
            timePickerDialog.show();
        });

        builder.setView(view)
                .setTitle("Add New Task")
                .setPositiveButton("Add", (dialog, which) -> {
                    calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                    Task task = new Task(
                            titleEdit.getText().toString(),
                            descEdit.getText().toString(),
                            calendar.getTimeInMillis(),
                            selectedHour[0] * 60 + selectedMinute[0], // Convert hours and minutes to total minutes
                            prioritySpinner.getSelectedItemPosition() + 1,
                            categorySpinner.getSelectedItem().toString()
                    );

                    // Set the time component
                    task.setTime(selectedHour[0], selectedMinute[0]);

                    long id = dbHelper.addTask(task);
                    task.setId((int) id);
                    taskList.add(task);
                    adapter.notifyItemInserted(taskList.size() - 1);

                    notificationHelper.scheduleNotification(task);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showEditTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        EditText titleEdit = view.findViewById(R.id.edit_title);
        EditText descEdit = view.findViewById(R.id.edit_description);
        DatePicker datePicker = view.findViewById(R.id.date_picker);
        TextView timeText = view.findViewById(R.id.text_time);
        Spinner prioritySpinner = view.findViewById(R.id.spinner_priority);
        Spinner categorySpinner = view.findViewById(R.id.spinner_category);
        CheckBox completedCheckBox = view.findViewById(R.id.checkbox_completed);

        // Set existing values
        titleEdit.setText(task.getTitle());
        descEdit.setText(task.getDescription());
        completedCheckBox.setChecked(task.isCompleted());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(task.getDueDate());
        datePicker.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        final int[] selectedHour = {task.getDueHours()};
        final int[] selectedMinute = {task.getDueMinutes()};
        updateTimeDisplay(timeText, selectedHour[0], selectedMinute[0]);

        // Setup time picker
        timeText.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view1, hourOfDay, minute) -> {
                        selectedHour[0] = hourOfDay;
                        selectedMinute[0] = minute;
                        updateTimeDisplay(timeText, hourOfDay, minute);
                    },
                    selectedHour[0],
                    selectedMinute[0],
                    true
            );
            timePickerDialog.show();
        });

        prioritySpinner.setSelection(task.getPriority() - 1);

        String[] categories = getResources().getStringArray(R.array.task_categories);
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(task.getCategory())) {
                categorySpinner.setSelection(i);
                break;
            }
        }

        builder.setView(view)
                .setTitle("Edit Task")
                .setPositiveButton("Save", (dialog, which) -> {
                    calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                    task.setTitle(titleEdit.getText().toString());
                    task.setDescription(descEdit.getText().toString());
                    task.setDueDate(calendar.getTimeInMillis());
                    task.setTime(selectedHour[0], selectedMinute[0]);
                    task.setPriority(prioritySpinner.getSelectedItemPosition() + 1);
                    task.setCategory(categorySpinner.getSelectedItem().toString());
                    task.setCompleted(completedCheckBox.isChecked());

                    dbHelper.updateTask(task);
                    adapter.notifyDataSetChanged();

                    if (!task.isCompleted()) {
                        notificationHelper.scheduleNotification(task);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateTimeDisplay(TextView timeText, int hour, int minute) {
        timeText.setText(String.format("%02d:%02d", hour, minute));
    }

    private void sortTasks(int sortOption) {
        List<Task> sortedList = new ArrayList<>(taskList);

        switch (sortOption) {
            case 0: // Due Date and Time
                Collections.sort(sortedList, (t1, t2) -> {
                    int dateCompare = Long.compare(t1.getDueDate(), t2.getDueDate());
                    if (dateCompare == 0) {
                        return Integer.compare(t1.getDueTimeInMinutes(), t2.getDueTimeInMinutes());
                    }
                    return dateCompare;
                });
                break;
            case 1: // Priority
                Collections.sort(sortedList, (t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));
                break;
            case 2: // Category
                Collections.sort(sortedList, (t1, t2) -> t1.getCategory().compareToIgnoreCase(t2.getCategory()));
                break;
        }

        adapter.updateTasks(sortedList);
    }

    private void filterTasks(int filterOption) {
        List<Task> filteredList;
        switch (filterOption) {
            case 0: // All Tasks
                filteredList = dbHelper.getAllTasks();
                break;
            case 1: // Completed Tasks
                filteredList = taskList.stream()
                        .filter(Task::isCompleted)
                        .collect(Collectors.toList());
                break;
            case 2: // Uncompleted Tasks
                filteredList = taskList.stream()
                        .filter(task -> !task.isCompleted())
                        .collect(Collectors.toList());
                break;
            default:
                filteredList = taskList;
        }
        adapter.updateTasks(filteredList);
    }

    // Existing interface implementations

    @Override
    public void onTaskClick(Task task) {
        showEditTaskDialog(task);
    }

    @Override
    public void onTaskLongClick(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteTask(task.getId());
                    taskList.remove(task);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteClick(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteTask(task.getId())) {
                        adapter.removeTask(task);
                        Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
