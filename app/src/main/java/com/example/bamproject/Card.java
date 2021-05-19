package com.example.bamproject;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = Card.tableName)
public class Card {
    public static final String tableName = "CARD_TABLE";

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ForeignKey
            (entity = User.class,
                    parentColumns = "uid",
                    childColumns = "userId",
                    onDelete = CASCADE
            )
    @ColumnInfo(name = "userId")
    public int userId;

    @ColumnInfo(name = "cardName")
    public String cardName;

    @ColumnInfo(name = "cardNumber")
    public String cardNumber;

    @ColumnInfo(name = "cardValidity")
    public String cardValidity;

    @ColumnInfo(name = "cvv")
    public String cvv;

    @NonNull
    @Override
    public String toString() {
        return "(" + cardName + ", " + cardNumber + ", " + cardValidity + ", " + cvv + ")";
    }

    public Card(int userId, String cardName, String cardNumber, String cardValidity, String cvv) {
        this.userId = userId;
        this.cardName = cardName;
        this.cardNumber = cardNumber;
        this.cardValidity = cardValidity;
        this.cvv = cvv;
    }
}

