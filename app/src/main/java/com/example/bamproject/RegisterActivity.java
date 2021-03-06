package com.example.bamproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    final String TAG = "Register Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void onRegisterHandler(View view) {
        clearErrorsView();

        EditText usernameView = findViewById(R.id.register_username);
        String username = usernameView.getText().toString();

        EditText passwordView = findViewById(R.id.register_password);
        String password = passwordView.getText().toString();

        EditText repeatPasswordView = findViewById(R.id.register_repeat_password);
        String repeatPassword = repeatPasswordView.getText().toString();

        RegisterErrorEnum registerError = checkCredentials(username, password, repeatPassword);
        if (registerError == RegisterErrorEnum.GOOD) {
            registerUser(username, password);
        } else {
            showErrorsView(registerError);
        }
    }

    private void clearErrorsView() {
        TextView errorView = findViewById(R.id.register_error);
        errorView.setText("");
    }

    private void showErrorsView(RegisterErrorEnum registerError) {
        String error = "";
        switch (registerError) {
            case EMPTY_FIELDS:
                error = "There are some empty fields";
                break;
            case PASSWORDS_MISMATCH:
                error = "Password did not match";
                break;
            case USER_EXISTS:
                error = "User already exists";
                break;
            default:
                error = "";
                break;
        }
        TextView errorView = findViewById(R.id.register_error);
        errorView.setText(error);
    }

    private RegisterErrorEnum checkCredentials(String username, String password, String repeatPassword) {
        if (username.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
            return RegisterErrorEnum.EMPTY_FIELDS;
        }
        if (!password.equals(repeatPassword)) {
            return RegisterErrorEnum.PASSWORDS_MISMATCH;
        }
        return RegisterErrorEnum.GOOD;
    }

    public void registerUser(String username, String password) {
        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        UserDao userDao = database.userDao();
        new Thread(() -> {
            User user = new User(username, password);
            String checkIfUserInDatabase = userDao.findUser(username);
            if (checkIfUserInDatabase != null && checkIfUserInDatabase.equals(username)) {
                runOnUiThread(() -> showErrorsView(RegisterErrorEnum.USER_EXISTS));
            } else {
                userDao.insertAll(user);
                logInUser(user);
            }

        }).start();
    }

    private void logInUser(User user) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}