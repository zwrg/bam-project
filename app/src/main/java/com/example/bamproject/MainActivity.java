package com.example.bamproject;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    final String TAG = "Register Activity";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int userId = Preferences.getUserId(getApplicationContext());
        final String username = Preferences.getPreference(getApplicationContext(), Preferences.USERNAME_PREFERENCE, "");
        final String password = Preferences.getPreference(getApplicationContext(), Preferences.USER_PASSWORD_PREFERENCE, "");

        if (userId != 0 && !username.isEmpty() && !password.isEmpty()) {
//            Log.d(TAG, userId + "");
//            Log.d(TAG, username);
//            Log.d(TAG, password);

            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        }
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