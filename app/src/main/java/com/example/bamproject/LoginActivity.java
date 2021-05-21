package com.example.bamproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void logInUser(User user) {
//        Log.d(TAG, "Login successful");

        Preferences.setUserId(getApplicationContext(), user.uid);
        Preferences.savePreference(getApplicationContext(), Preferences.USERNAME_PREFERENCE, user.username);
        Preferences.savePreference(getApplicationContext(), Preferences.USER_PASSWORD_PREFERENCE, user.password);

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
            User user = userDao.checkUserInDatabase(username, password);
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