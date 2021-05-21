package com.example.bamproject;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Card.class}, version = 12)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    public abstract CardDao cardDao();

    private static final String DB_NAME = "BamProject_DATABASE_ENCRYPTED1.db";
    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(final Context context) {
        AppDatabase result = instance;
        if (result != null) {
            return result;
        }

        synchronized (AppDatabase.class) {
            if (instance == null) {
                // todo make good passphrase
                final SupportFactory factory = new SupportFactory(SQLiteDatabase.getBytes(Keys.getKey(context).toCharArray()));
                instance = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                        .openHelperFactory(factory)
                        .fallbackToDestructiveMigration()
                        .build();
//                instance = Room.databaseBuilder(context, AppDatabase.class, DB_NAME).fallbackToDestructiveMigration().build();
            }
            return instance;
        }
    }
}
