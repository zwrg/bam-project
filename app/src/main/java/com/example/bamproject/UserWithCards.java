package com.example.bamproject;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class UserWithCards {
    @Embedded
    public User user;
    @Relation(
            parentColumn = "userId",
            entity = User.class,
            entityColumn = "userCreatorId"
    )
    public List<Card> cards;
    public UserWithCards(User user, List<Card> cards) {
        this.user = user;
        this.cards = cards;
    }
}
