package com.example.bamproject;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Query("SELECT uid, username, password from " + User.tableName + " where username = :username and password = :password LIMIT 1")
    User findUser(String username, String password);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);
}
