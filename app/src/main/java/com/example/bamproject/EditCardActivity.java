package com.example.bamproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import static com.example.bamproject.Constants.SHARED_PREFS;
import static com.example.bamproject.Constants.USER_ID;

public class EditCardActivity extends AppCompatActivity {
    final String TAG = "Edit Card Activity";
    private Card currentCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_edit_card);

        int cardId = getIntent().getIntExtra("cardUid", 0);
        if (cardId == 0) {
            Log.d(TAG, "card id = 0, sth wrong");
            throw new Error(TAG + "card id = 0, sth wrong");
        }

        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        CardDao cardDao = database.cardDao();
        new Thread(() -> {
            Card card = cardDao.getCard(cardId);
            Log.d(TAG, card.toString());
            currentCard = card;
            runOnUiThread(this::updateView);
        }).start();
    }

    private void updateView() {
        EditText cardNameView = findViewById(R.id.card_edit_text_name);
        EditText cardNumberView = findViewById(R.id.card_edit_text_number);
        EditText cardValidityView = findViewById(R.id.card_edit_text_validity);
        EditText cardCvvView = findViewById(R.id.card_edit_text_cvv);

        cardNameView.setText(currentCard.cardName);
        cardNumberView.setText(currentCard.cardNumber);
        cardValidityView.setText(currentCard.cardValidity);
        cardCvvView.setText(currentCard.cvv);
    }

    public void onSaveHandler(View view) {
        clearErrorsView();

        EditText cardNameView = findViewById(R.id.card_edit_text_name);
        String cardName = cardNameView.getText().toString();

        EditText cardNumberView = findViewById(R.id.card_edit_text_number);
        String cardNumber = cardNumberView.getText().toString();

        EditText cardValidityView = findViewById(R.id.card_edit_text_validity);
        String cardValidity = cardValidityView.getText().toString();

        EditText cardCvvView = findViewById(R.id.card_edit_text_cvv);
        String cardCvv = cardCvvView.getText().toString();

        AddCardErrorEnum addCardError = checkDataValidity(cardName, cardNumber, cardValidity, cardCvv);
        if (addCardError == AddCardErrorEnum.GOOD) {
            currentCard.cardName = cardName;
            currentCard.cardNumber = cardNumber;
            currentCard.cardValidity = cardValidity;
            currentCard.cvv = cardCvv;

            AppDatabase database = AppDatabase.getInstance(getApplicationContext());
            CardDao cardDao = database.cardDao();
            // todo add other user ID check
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
            int currentUserId = sharedPreferences.getInt(USER_ID, 0);

            if (currentUserId == 0) {
                throw new Error("User ID = 0");
            }

            new Thread(() -> {
                cardDao.updateCard(currentCard);

                leaveActivity();
            }).start();
        } else {
            showErrorsView(addCardError);
        }
    }

    private AddCardErrorEnum checkDataValidity(String cardName, String cardNumber, String cardValidity, String cardCvv) {
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

    private boolean isNumberInvalid(String cardNumber) {
        if (cardNumber.length() != 16) {
            Log.d(TAG, "card number != 16 => " + cardNumber.length());
            return true;
        }
        String regex = "^[0-9]{16}$";
        boolean matches = cardNumber.matches(regex);
        Log.d(TAG, "card number is matching => " + matches);
        return !matches;
    }

    private boolean isValidityInvalid(String cardValidity) {
        if (cardValidity.length() != 5) {
            Log.d(TAG, "card validity != 5 => " + cardValidity.length());
            return true;
        }
        String regex = "^(0[1-9]|1[0-2])\\/([0-9]{2})$";
        boolean matches = cardValidity.matches(regex);
        Log.d(TAG, "card validity is matching => " + matches);
        return !matches;
    }

    private boolean isCvvInvalid(String cardCvv) {
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

    private void clearErrorsView() {
        TextView errorView = findViewById(R.id.card_edit_error);
        errorView.setText("");
    }

    private void showErrorsView(AddCardErrorEnum registerError) {
        String error = "";
        switch (registerError) {
            case EMPTY_FIELDS:
                error = "There are some empty fields";
                break;
            case WRONG_NUMBER:
                error = "Bad Card Number";
                break;
            case WRONG_VALIDITY:
                error = "Bad Validity Format";
                break;
            case WRONG_CVV:
                error = "Bad CVV Number";
                break;
            default:
                error = "";
                break;
        }
        TextView errorView = findViewById(R.id.card_edit_error);
        errorView.setText(error);
    }

    private void leaveActivity() {
        Intent intent = new Intent(EditCardActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    public void onCancelHandler(View view) {
        finish();
    }
}