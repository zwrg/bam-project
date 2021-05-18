package com.example.bamproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import static com.example.bamproject.Constants.PASSWORD;
import static com.example.bamproject.Constants.SHARED_PREFS;
import static com.example.bamproject.Constants.USERNAME;

public class MainActivity extends AppCompatActivity {
    final String TAG = "Register Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        Log.d(TAG, sharedPreferences.getString(USERNAME, ""));
        Log.d(TAG, sharedPreferences.getString(PASSWORD, ""));
    }

    public void onLoginHandler(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void onRegisterHandler(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}