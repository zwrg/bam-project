package com.example.bamproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import static com.example.bamproject.Constants.PASSWORD;
import static com.example.bamproject.Constants.SHARED_PREFS;
import static com.example.bamproject.Constants.USERNAME;
import static com.example.bamproject.Constants.USER_ID;

public class HomeActivity extends AppCompatActivity {
    final String TAG = "Home Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        refreshCardList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshCardList();
    }

    @Override
    public void onBackPressed() {
        logout();
    }

    private void refreshCardList() {
        Log.d(TAG, "Refreshing Card List");
        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        CardDao cardDao = database.cardDao();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(USER_ID, 0);
        if (userId != 0) {
            new Thread(() -> {
                List<Card> cards = cardDao.getUserCards(userId);
                Log.d(TAG, cards.toString());
                if (cards.size() == 0) {
                    runOnUiThread(this::clearView);
                } else {
                    runOnUiThread(() -> updateView(cards));
                }
            }).start();
        }
    }


    private void clearView() {

    }

    private void updateView(List<Card> cards) {
        RecyclerView recyclerView = findViewById(R.id.list_recycler_view);
        CustomAdapter userCustomAdapter = new CustomAdapter(cards);
        recyclerView.setAdapter(userCustomAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            logout();
            return true;
        }
        if (item.getItemId() == R.id.menu_refresh) {
            refreshCardList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        Log.d(TAG, "Logout successful");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(USERNAME);
        editor.remove(PASSWORD);
        editor.remove(USER_ID);
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void onAddCardHandler(View view) {
        Intent intent = new Intent(HomeActivity.this, AddCardActivity.class);
        startActivity(intent);
    }
}