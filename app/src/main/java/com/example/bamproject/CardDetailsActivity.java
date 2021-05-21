package com.example.bamproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CardDetailsActivity extends AppCompatActivity {
    final String TAG = "Card Details Activity";
    private Card currentCard;
    private boolean isDataHidden = true;
    private String passwordCheck = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_card_details);

        int cardId = getIntent().getIntExtra("cardUid", 0);
        if (cardId == 0) {
//            Log.d(TAG, "card id = 0, sth wrong");
            throw new Error(TAG + "card id = 0, sth wrong");
        }

        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        CardDao cardDao = database.cardDao();
        new Thread(() -> {
            currentCard = cardDao.getCard(cardId);
            //            Log.d(TAG, card.toString());
            runOnUiThread(this::updateView);
        }).start();
    }

    private void updateView() {
        TextView cardNameView = findViewById(R.id.card_details_text_name);
        TextView cardNumberView = findViewById(R.id.card_details_text_number);
        TextView cardValidityView = findViewById(R.id.card_details_text_validity);
        TextView cardCvvView = findViewById(R.id.card_details_text_cvv);
        TextView cardButtonView = findViewById(R.id.card_details_button_show);

        if (isDataHidden) {
            cardButtonView.setText(R.string.show);
            cardNameView.setText(currentCard.cardName);
            cardNumberView.setText(hideNumber(currentCard.cardNumber));
            cardValidityView.setText(hideValidity());
            cardCvvView.setText(hideCvv());
        } else {
            cardButtonView.setText(R.string.hide);
            cardNameView.setText(currentCard.cardName);
            cardNumberView.setText(currentCard.cardNumber);
            cardValidityView.setText(currentCard.cardValidity);
            cardCvvView.setText(currentCard.cvv);
        }
    }

    private String hideCvv() {
        return "***";
    }

    private String hideValidity() {
        return "**/**";
    }

    private String hideNumber(String cardNumber) {
        return "************" + cardNumber.substring(cardNumber.length() - 4);
    }

    public void onShowHandler(View view) {
        if (isDataHidden) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Password check");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                passwordCheck = input.getText().toString();
                AppDatabase database = AppDatabase.getInstance(getApplicationContext());
                UserDao userDao = database.userDao();
                int userId = Preferences.getUserId(getApplicationContext());
                if (userId != 0) {
                    new Thread(() -> {
                        User user = userDao.checkPassword(userId, passwordCheck);
                        if (user == null) {
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Bad password", Toast.LENGTH_LONG).show());
                        } else {
                            isDataHidden = !isDataHidden;
                            runOnUiThread(this::updateView);
                        }
                    }).start();

                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        } else {
            isDataHidden = !isDataHidden;
            updateView();
        }
    }

    public void onEditHandler(View view) {
        Intent intent = new Intent(view.getContext(), EditCardActivity.class);
        intent.putExtra("cardUid", currentCard.uid);
        view.getContext().startActivity(intent);
    }

    public void onRemoveHandler(View view) {
        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        CardDao cardDao = database.cardDao();
        new Thread(() -> {
            cardDao.removeCard(currentCard);
//            Log.d(TAG, "removing card");
            currentCard = null;
            finish();
        }).start();
    }
}