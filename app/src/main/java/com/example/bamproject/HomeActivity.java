package com.example.bamproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import static com.example.bamproject.Constants.PASSWORD;
import static com.example.bamproject.Constants.SHARED_PREFS;
import static com.example.bamproject.Constants.USERNAME;
import static com.example.bamproject.Constants.USER_ID;

public class HomeActivity extends AppCompatActivity {
    final String TAG = "Home Activity";
    private final String databaseFileName = "CardBackupDatabase.csv";
    private final int WRITE_READ_FILE_REQUEST_CODE = 7;

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
                    runOnUiThread(() -> updateView(cards));
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
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (hasNoPermission(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, WRITE_READ_FILE_REQUEST_CODE);
            } else {
                importDatabaseFromFile();
            }
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
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                        int currentUserId = sharedPreferences.getInt(USER_ID, 0);
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
            CardValidator cardValidator = new CardValidator();
            String cardName = record[0];
            String cardNumber = record[1];
            String cardValidity = record[2];
            String cardCvv = record[3];
            if (cardValidator.checkDataValidity(cardName, cardNumber, cardValidity, cardCvv) == AddCardErrorEnum.GOOD) {
                return new CardShort(cardName, cardNumber, cardValidity, cardCvv);
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void exportDatabase() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (hasNoPermission(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, WRITE_READ_FILE_REQUEST_CODE);
            } else {
                exportDatabaseToFile();
            }
        } else {
            exportDatabaseToFile();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void exportDatabaseToFile() {
        File downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File databaseFile = new File(downloadsPath, databaseFileName);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(USER_ID, 0);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
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

//    private void createFile(File databaseFile) {
//        if (checkIfFileExists(databaseFile)) {
//            try {
//                boolean fileDeleted = databaseFile.delete();
//                boolean fileCreated = false;
//                if (fileDeleted) {
//                    Log.d(TAG, "database file deleted");
//                    fileCreated = databaseFile.createNewFile();
//                }
//                if (fileCreated) {
//                    Log.d(TAG, "database file created");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            try {
//                boolean fileCreated = databaseFile.createNewFile();
//                if (fileCreated) {
//                    Log.d(TAG, "database file created");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }


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