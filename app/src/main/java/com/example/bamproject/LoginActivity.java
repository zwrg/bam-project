package com.example.bamproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static com.example.bamproject.Constants.PASSWORD;
import static com.example.bamproject.Constants.SHARED_PREFS;
import static com.example.bamproject.Constants.USERNAME;

public class LoginActivity extends AppCompatActivity {
    final String TAG = "Login Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onLoginHandler(View view) {
        clearErrorsView();

        EditText usernameView = findViewById(R.id.login_password);
        String username = usernameView.getText().toString();

        EditText passwordView = findViewById(R.id.login_username);
        String password = passwordView.getText().toString();

        logIn(username, password);
    }

    private void logInUser(User user) {
        Log.d(TAG, "Login successful");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USERNAME, user.username);
        editor.putString(PASSWORD, user.password);
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    private void logIn(String username, String password) {
        clearErrorsView();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorsView(LoginErrorEnum.EMPTY_FIELDS);
            return;
        }

        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        UserDao userDao = database.userDao();
        new Thread(() -> {
            // todo add encryption
            User user = userDao.findUser(username, password);
            if (user == null) {
                runOnUiThread(() -> showErrorsView(LoginErrorEnum.CREDENTIALS_ERROR));
            } else {
                runOnUiThread(() -> showErrorsView(LoginErrorEnum.GOOD));
                logInUser(user);
            }
        }).start();
    }

    private void clearErrorsView() {
        TextView errorView = findViewById(R.id.login_error);
        errorView.setText("");
    }

    private void showErrorsView(LoginErrorEnum registerError) {
        String error = "";
        switch (registerError) {
            case EMPTY_FIELDS:
                error = "There are some empty fields";
                break;
            case CREDENTIALS_ERROR:
                error = "Wrong credentials";
                break;
            default:
                error = "";
                break;
        }
        TextView errorView = findViewById(R.id.login_error);
        errorView.setText(error);
    }
}