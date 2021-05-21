package com.example.bamproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

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
//            Log.d(TAG, "card id = 0, sth wrong");
            throw new Error(TAG + "card id = 0, sth wrong");
        }

        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        CardDao cardDao = database.cardDao();
        new Thread(() -> {
            currentCard = cardDao.getCard(cardId);
            //            Log.d(TAG, currentCard.toString());
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

    @RequiresApi(api = Build.VERSION_CODES.M)
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

        CardValidityEnum addCardError = CardValidator.checkDataValidity(cardName, cardNumber, cardValidity, cardCvv);
        if (addCardError == CardValidityEnum.GOOD) {
            currentCard.cardName = cardName;
            currentCard.cardNumber = cardNumber;
            currentCard.cardValidity = cardValidity;
            currentCard.cvv = cardCvv;

            AppDatabase database = AppDatabase.getInstance(getApplicationContext());
            CardDao cardDao = database.cardDao();
            // todo add other user ID check

            int currentUserId = Preferences.getUserId(getApplicationContext());

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

    private void clearErrorsView() {
        TextView errorView = findViewById(R.id.card_edit_error);
        errorView.setText("");
    }

    private void showErrorsView(CardValidityEnum registerError) {
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