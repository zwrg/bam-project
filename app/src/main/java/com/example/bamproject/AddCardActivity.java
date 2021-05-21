package com.example.bamproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class AddCardActivity extends AppCompatActivity {
    final String TAG = "AddCard Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
    }

    public void onSaveHandler(View view) {
        clearErrorsView();

        EditText cardNameView = findViewById(R.id.add_card_name);
        String cardName = cardNameView.getText().toString();

        EditText cardNumberView = findViewById(R.id.add_card_number);
        String cardNumber = cardNumberView.getText().toString();

        EditText cardValidityView = findViewById(R.id.add_card_validity);
        String cardValidity = cardValidityView.getText().toString();

        EditText cardCvvView = findViewById(R.id.add_card_cvv);
        String cardCvv = cardCvvView.getText().toString();

        CardValidityEnum addCardError = CardValidator.checkDataValidity(cardName, cardNumber, cardValidity, cardCvv);
        if (addCardError == CardValidityEnum.GOOD) {
            AppDatabase database = AppDatabase.getInstance(getApplicationContext());
            CardDao cardDao = database.cardDao();
            // todo add other user ID check
            int currentUserId = Preferences.getUserId(getApplicationContext());

            if (currentUserId == 0) {
                throw new Error("User ID = 0");
            }

            new Thread(() -> {
                Card card = new Card(currentUserId, cardName, cardNumber, cardValidity, cardCvv);
                cardDao.insertCards(card);

                leaveActivity();
            }).start();
        } else {
            showErrorsView(addCardError);
        }

    }

    private void leaveActivity() {
        finish();
    }

    private void clearErrorsView() {
        TextView errorView = findViewById(R.id.card_error);
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
        TextView errorView = findViewById(R.id.card_error);
        errorView.setText(error);
    }
}