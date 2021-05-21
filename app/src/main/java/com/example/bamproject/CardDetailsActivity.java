package com.example.bamproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CardDetailsActivity extends AppCompatActivity {
    final String TAG = "Card Details Activity";
    private Card currentCard;
    private boolean isDataHidden = true;

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
            Card card = cardDao.getCard(cardId);
//            Log.d(TAG, card.toString());
            currentCard = card;
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
        isDataHidden = !isDataHidden;
        updateView();
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