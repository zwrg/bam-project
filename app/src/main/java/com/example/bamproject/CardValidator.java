package com.example.bamproject;

import android.util.Log;

public class CardValidator {
    private final String TAG = "Card Validator";

    public boolean isNumberInvalid(String cardNumber) {
        if (cardNumber.length() != 16) {
            Log.d(TAG, "card number != 16 => " + cardNumber.length());
            return true;
        }
        String regex = "^[0-9]{16}$";
        boolean matches = cardNumber.matches(regex);
        Log.d(TAG, "card number is matching => " + matches);
        return !matches;
    }

    public boolean isValidityInvalid(String cardValidity) {
        if (cardValidity.length() != 5) {
            Log.d(TAG, "card validity != 5 => " + cardValidity.length());
            return true;
        }
        String regex = "^(0[1-9]|1[0-2])\\/([0-9]{2})$";
        boolean matches = cardValidity.matches(regex);
        Log.d(TAG, "card validity is matching => " + matches);
        return !matches;
    }

    public boolean isCvvInvalid(String cardCvv) {
        if (cardCvv.length() != 3) {
            Log.d(TAG, "card cvv != 3 => " + cardCvv.length());
            return true;
        }
        try {
            Integer.parseInt(cardCvv);
        } catch (NumberFormatException e) {
            Log.d(TAG, "card cvv error: " + e.toString());
            return true;
        }
        return false;
    }

    public AddCardErrorEnum checkDataValidity(String cardName, String cardNumber, String cardValidity, String cardCvv) {
        if (cardName.isEmpty() || cardNumber.isEmpty() || cardValidity.isEmpty() || cardCvv.isEmpty()) {
            return AddCardErrorEnum.EMPTY_FIELDS;
        }

        if (isNumberInvalid(cardNumber)) {
            return AddCardErrorEnum.WRONG_NUMBER;
        }
        if (isValidityInvalid(cardValidity)) {
            return AddCardErrorEnum.WRONG_VALIDITY;
        }
        if (isCvvInvalid(cardCvv)) {
            return AddCardErrorEnum.WRONG_CVV;
        }
        return AddCardErrorEnum.GOOD;
    }
}
