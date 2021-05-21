package com.example.bamproject;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Card.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    public abstract CardDao cardDao();

    private static final String DB_NAME = "BamProject_DATABASE.db";
    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(final Context context) {
        AppDatabase result = instance;
        if (result != null) {
            return result;
        }

        synchronized (AppDatabase.class) {
            if (instance == null) {
                instance = Room.databaseBuilder(context, AppDatabase.class, DB_NAME).fallbackToDestructiveMigration().build();
            }
            return instance;
        }
    }
}
