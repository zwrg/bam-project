package com.example.bamproject;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Query("SELECT uid, username, password FROM " + User.tableName + " WHERE username = :username AND password = :password LIMIT 1")
    User checkUserInDatabase(String username, String password);

    @Query("SELECT username FROM " + User.tableName + " WHERE username = :username")
    String findUser(String username);

    @Query("SELECT uid, username, password FROM " + User.tableName + " WHERE password = :password AND uid = :uid")
    User checkPassword(int uid, String password);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);
}
