package com.example.taskmanager2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {
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

        dbHelper = new DatabaseHelper(this);
        notificationHelper = new NotificationHelper(this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = dbHelper.getAllTasks();
        adapter = new TaskAdapter(this, taskList, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        fab.setOnClickListener(v -> showAddTaskDialog());

        // Sorting spinner setup
        sortSpinner = findViewById(R.id.sort_spinner);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
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

        // Filtering spinner setup
        filterSpinner = findViewById(R.id.filter_spinner);
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
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
        Spinner prioritySpinner = view.findViewById(R.id.spinner_priority);
        Spinner categorySpinner = view.findViewById(R.id.spinner_category);

        builder.setView(view)
                .setTitle("Add New Task")
                .setPositiveButton("Add", (dialog, which) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                    Task task = new Task(
                            titleEdit.getText().toString(),
                            descEdit.getText().toString(),
                            calendar.getTimeInMillis(),
                            prioritySpinner.getSelectedItemPosition() + 1,
                            categorySpinner.getSelectedItem().toString()
                    );

                    long id = dbHelper.addTask(task);
                    task.setId((int) id);
                    taskList.add(task);
                    adapter.notifyItemInserted(taskList.size() - 1);

                    notificationHelper.scheduleNotification(task);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sortTasks(int sortOption) {
        List<Task> sortedList = new ArrayList<>(taskList); // Create a copy of the taskList for sorting

        switch (sortOption) {
            case 0: // Due Date
                Collections.sort(sortedList, (t1, t2) -> Long.compare(t1.getDueDate(), t2.getDueDate()));
                break;
            case 1: // Priority
                Collections.sort(sortedList, (t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));
                break;
            case 2: // Category
                Collections.sort(sortedList, (t1, t2) -> t1.getCategory().compareToIgnoreCase(t2.getCategory()));
                break;
        }

        adapter.updateTasks(sortedList); // Update the adapter with the sorted list
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


    private void showEditTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        EditText titleEdit = view.findViewById(R.id.edit_title);
        EditText descEdit = view.findViewById(R.id.edit_description);
        DatePicker datePicker = view.findViewById(R.id.date_picker);
        Spinner prioritySpinner = view.findViewById(R.id.spinner_priority);
        Spinner categorySpinner = view.findViewById(R.id.spinner_category);
        CheckBox completedCheckBox = view.findViewById(R.id.checkbox_completed);

        titleEdit.setText(task.getTitle());
        descEdit.setText(task.getDescription());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(task.getDueDate());
        datePicker.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        prioritySpinner.setSelection(task.getPriority() - 1);

        String[] categories = getResources().getStringArray(R.array.task_categories);
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(task.getCategory())) {
                categorySpinner.setSelection(i);
                break;
            }
        }

        completedCheckBox.setChecked(task.isCompleted());

        builder.setView(view)
                .setTitle("Edit Task")
                .setPositiveButton("Save", (dialog, which) -> {
                    calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                    task.setTitle(titleEdit.getText().toString());
                    task.setDescription(descEdit.getText().toString());
                    task.setDueDate(calendar.getTimeInMillis());
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

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
