package com.example.bamproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity {
    final String TAG = "Home Activity";
    private final String databaseFileName = "CardBackupDatabase.csv";
    private final int WRITE_READ_FILE_REQUEST_CODE = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        checkUser();

        refreshCardList();
    }

    private void checkUser() {
        final String username = Preferences.getPreference(getApplicationContext(), Preferences.USERNAME_PREFERENCE, "");
        final String password = Preferences.getPreference(getApplicationContext(), Preferences.USER_PASSWORD_PREFERENCE, "");

        if (username.isEmpty() || password.isEmpty()){
            throw new Error("unauthorized access");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkUser();

        refreshCardList();
    }

    @Override
    public void onBackPressed() {
        logout();
    }

    private void refreshCardList() {
//        Log.d(TAG, "Refreshing Card List");
        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        CardDao cardDao = database.cardDao();
        int userId = Preferences.getUserId(getApplicationContext());
        if (userId != 0) {
            new Thread(() -> {
                List<Card> cards = cardDao.getUserCards(userId);
//                Log.d(TAG, cards.toString());
                runOnUiThread(() -> updateView(cards));
            }).start();
        }
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

    @RequiresApi(api = Build.VERSION_CODES.N)
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
        if (item.getItemId() == R.id.menu_import) {
            importDatabase();
            return true;
        }
        if (item.getItemId() == R.id.menu_export) {
            exportDatabase();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void importDatabase() {
        String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (hasNoPermission(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, WRITE_READ_FILE_REQUEST_CODE);
        } else {
            importDatabaseFromFile();
        }
    }

    private void importDatabaseFromFile() {
        File downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File databaseFile = new File(downloadsPath, databaseFileName);
        // todo change to manually selecting file
        if (checkIfFileExists(databaseFile)) {
            try {
                FileReader fileReader = new FileReader(databaseFile);
                CSVReader reader = new CSVReader(fileReader);
                List<String[]> data = reader.readAll();
                List<Card> cards = new ArrayList<Card>();
                for (int i = 0; i < data.size(); i++) {
                    CardShort cardShort = parseDataFromCsv(data.get(i));
                    if (cardShort != null) {
                        int currentUserId = Preferences.getUserId(getApplicationContext());
                        if (currentUserId == 0) {
                            throw new Error("User ID = 0");
                        }
                        Card card = new Card(currentUserId, cardShort.cardName, cardShort.cardNumber, cardShort.cardValidity, cardShort.cvv);
                        cards.add(card);
                    } else {
                        Log.e(TAG, "import error");
                        Toast.makeText(getApplicationContext(), "import error", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                AppDatabase database = AppDatabase.getInstance(getApplicationContext());
                CardDao cardDao = database.cardDao();
                // todo add other user ID check

                new Thread(() -> {
                    for (int i = 0; i < cards.size(); i++) {
                        cardDao.insertCards(cards.get(i));
                    }
                    refreshCardList();
                    Log.i(TAG, "import successful");
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "import successful", Toast.LENGTH_SHORT).show());

                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "no file found - " + databaseFile.toString());
            Toast.makeText(getApplicationContext(), "no file found - " + databaseFile.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    private CardShort parseDataFromCsv(String[] record) {
        if (record.length != 4) {
            Log.e(TAG, "bad database format");
            Toast.makeText(getApplicationContext(), "bad database format", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            String cardName = record[0];
            String cardNumber = record[1];
            String cardValidity = record[2];
            String cardCvv = record[3];
            if (CardValidator.checkDataValidity(cardName, cardNumber, cardValidity, cardCvv) == CardValidityEnum.GOOD) {
                return new CardShort(cardName, cardNumber, cardValidity, cardCvv);
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void exportDatabase() {
        String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (hasNoPermission(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, WRITE_READ_FILE_REQUEST_CODE);
        } else {
            exportDatabaseToFile();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void exportDatabaseToFile() {
        File downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File databaseFile = new File(downloadsPath, databaseFileName);
        int userId = Preferences.getUserId(getApplicationContext());
        if (userId != 0) {
            new Thread(() -> {
                AppDatabase database = AppDatabase.getInstance(getApplicationContext());
                CardDao cardDao = database.cardDao();
                List<Card> cards = cardDao.getUserCards(userId);
                if (cards.size() != 0) {
                    FileWriter fileWriter = null;
                    try {
                        fileWriter = new FileWriter(databaseFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    CSVWriter writer = new CSVWriter(fileWriter);
                    List<String[]> shortCards = cards.stream()
                            .map(card -> new CardShort(card.cardName, card.cardNumber, card.cardValidity, card.cvv).toStringArray())
                            .collect(Collectors.toList());
                    // note: not thread safe
                    writer.writeAll(shortCards);
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "export successful");
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "export successful", Toast.LENGTH_SHORT).show());
                } else {
                    Log.e(TAG, "no cards to save");
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "no cards to save", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
    }

    private static boolean hasNoPermission(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_READ_FILE_REQUEST_CODE) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportDatabase();
            } else {
                Log.e(TAG, "no permissions granted");
            }
        }
    }

    private boolean checkIfFileExists(File databaseFile) {
        return databaseFile.exists();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void logout() {
//        Log.d(TAG, "Logout successful");
        Preferences.removeUserId(getApplicationContext());
        Preferences.removePreference(getApplicationContext(), Preferences.USERNAME_PREFERENCE);
        Preferences.removePreference(getApplicationContext(), Preferences.USER_PASSWORD_PREFERENCE);

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void onAddCardHandler(View view) {
        Intent intent = new Intent(HomeActivity.this, AddCardActivity.class);
        startActivity(intent);
    }
}