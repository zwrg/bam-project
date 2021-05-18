package com.example.bamproject;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    private static final String DB_NAME = "BamProject_DATABASE.db";
    private static volatile AppDatabase instance;

    private static final int NUMBER_OF_THREADS = 4;

    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(final Context context) {
        AppDatabase result = instance;
        if(result != null) {
            return result;
        }

        synchronized(AppDatabase.class) {
            if(instance == null) {
                instance = Room.databaseBuilder(context, AppDatabase.class, DB_NAME).build();
            }
            return instance;
        }
    }
}
