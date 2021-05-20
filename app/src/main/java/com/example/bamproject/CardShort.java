package com.example.bamproject;

import androidx.annotation.NonNull;

public class CardShort {
    public String cardName;

    public String cardNumber;

    public String cardValidity;

    public String cvv;

    @NonNull
    @Override
    public String toString() {
        return "(" + cardName + ", " + cardNumber + ", " + cardValidity + ", " + cvv + ")";
    }

    public String[] toStringArray(){
        return new String[] {cardName, cardNumber, cardValidity, cvv};
    }

    public CardShort(String cardName, String cardNumber, String cardValidity, String cvv) {
        this.cardName = cardName;
        this.cardNumber = cardNumber;
        this.cardValidity = cardValidity;
        this.cvv = cvv;
    }
}
