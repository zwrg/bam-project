package com.example.bamproject;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CardDao {
    @Transaction
    @Query("SELECT * FROM " + Card.tableName + " WHERE userId = :userId")
    List<Card> getUserCards(int userId);

    @Transaction
    @Query("SELECT * FROM " + Card.tableName + " WHERE uid = :cardId")
    Card getCard(int cardId);

    @Transaction
    @Insert
    long insertUser(User user);

    @Insert
    void insertCards(Card... cards);

    @Transaction
    @Update(entity = Card.class)
    void updateCard(Card card);

    @Transaction
    @Delete
    void removeCard(Card card);
}
