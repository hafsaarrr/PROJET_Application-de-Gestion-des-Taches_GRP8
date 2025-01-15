package com.example.taskmanager2;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TaskManager";
    private static final int DATABASE_VERSION = 2; // Increased version for schema change

    private static final String TABLE_TASKS = "tasks";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DUE_DATE = "due_date"; // Stored in milliseconds
    private static final String KEY_DUE_TIME = "due_time"; // Store time in minutes since midnight
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_COMPLETED = "completed";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DUE_DATE + " INTEGER," // Store full date in milliseconds
                + KEY_DUE_TIME + " INTEGER," // Store minutes since midnight (0-1439)
                + KEY_PRIORITY + " INTEGER,"
                + KEY_CATEGORY + " TEXT,"
                + KEY_COMPLETED + " INTEGER" + ")";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add the new time column to existing table
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + KEY_DUE_TIME + " INTEGER DEFAULT 0");
        }
    }

    // Add a new task
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, task.getTitle());
        values.put(KEY_DESCRIPTION, task.getDescription());
        values.put(KEY_DUE_DATE, task.getDueDate());
        values.put(KEY_DUE_TIME, task.getDueTimeInMinutes());
        values.put(KEY_PRIORITY, task.getPriority());
        values.put(KEY_CATEGORY, task.getCategory());
        values.put(KEY_COMPLETED, task.isCompleted() ? 1 : 0);
        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    @SuppressLint("Range")
    public Task getTask(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        Task task = null;
        if (cursor != null && cursor.moveToFirst()) {
            task = new Task();
            task.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
            task.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
            task.setDescription(cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)));
            task.setDueDate(cursor.getLong(cursor.getColumnIndex(KEY_DUE_DATE)));
            task.setDueTimeInMinutes(cursor.getInt(cursor.getColumnIndex(KEY_DUE_TIME)));
            task.setPriority(cursor.getInt(cursor.getColumnIndex(KEY_PRIORITY)));
            task.setCategory(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)));
            task.setCompleted(cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1);
            cursor.close();
        }
        return task;
    }

    @SuppressLint("Range")
    public List<Task> getAllTasks() {
        return getTasksByCompletionStatus(-1); // -1 retrieves all tasks
    }

    @SuppressLint("Range")
    public List<Task> getCompletedTasks() {
        return getTasksByCompletionStatus(1); // 1 for completed tasks
    }

    @SuppressLint("Range")
    public List<Task> getUncompletedTasks() {
        return getTasksByCompletionStatus(0); // 0 for uncompleted tasks
    }

    @SuppressLint("Range")
    private List<Task> getTasksByCompletionStatus(int completedStatus) {
        List<Task> taskList = new ArrayList<>();
        String selectQuery;

        if (completedStatus == -1) {
            selectQuery = "SELECT * FROM " + TABLE_TASKS;
        } else {
            selectQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + KEY_COMPLETED + "=" + completedStatus;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
                task.setDescription(cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)));
                task.setDueDate(cursor.getLong(cursor.getColumnIndex(KEY_DUE_DATE)));
                task.setDueTimeInMinutes(cursor.getInt(cursor.getColumnIndex(KEY_DUE_TIME)));
                task.setPriority(cursor.getInt(cursor.getColumnIndex(KEY_PRIORITY)));
                task.setCategory(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)));
                task.setCompleted(cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED)) == 1);
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, task.getTitle());
        values.put(KEY_DESCRIPTION, task.getDescription());
        values.put(KEY_DUE_DATE, task.getDueDate());
        values.put(KEY_DUE_TIME, task.getDueTimeInMinutes());
        values.put(KEY_PRIORITY, task.getPriority());
        values.put(KEY_CATEGORY, task.getCategory());
        values.put(KEY_COMPLETED, task.isCompleted() ? 1 : 0);
        return db.update(TABLE_TASKS, values, KEY_ID + "=?",
                new String[]{String.valueOf(task.getId())});
    }

    public boolean deleteTask(int id) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            // Log pour le débogage
            Log.d("DatabaseHelper", "Attempting to delete task with ID: " + id);

            // Vérifier si la tâche existe avant de la supprimer
            Cursor cursor = db.query(TABLE_TASKS, new String[]{KEY_ID},
                    KEY_ID + "=?", new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.close();

                // Effectuer la suppression
                int result = db.delete(TABLE_TASKS,
                        KEY_ID + "=?",
                        new String[]{String.valueOf(id)});

                Log.d("DatabaseHelper", "Delete result: " + result);
                return result > 0;
            } else {
                Log.d("DatabaseHelper", "Task not found with ID: " + id);
                if (cursor != null) cursor.close();
                return false;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting task: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}