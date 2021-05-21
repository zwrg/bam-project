package com.example.bamproject;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT uid, username, password FROM " + User.tableName + " WHERE username = :username AND password = :password LIMIT 1")
    User checkUserInDatabase(String username, String password);

    @Query("SELECT username FROM " + User.tableName + " WHERE username = :username")
    String findUser(String username);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);
}
