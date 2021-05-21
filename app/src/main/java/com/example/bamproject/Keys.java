package com.example.bamproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.RequiresApi;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Keys {
    private static volatile SharedPreferences instance;
    private static final int KEY_SIZE = 256;
    private static final String sharedPreferencesFile = "BAM_PROJECT_SHARED_PREFERENCES_KEYS";
    private static final String MASTER_KEY_ALIAS = "KEY_ALIAS";
    private static final String DATABASE_KEY_ALIAS = "DATABASE_KEY_ALIAS";
    private static final String DB_KEY = "DB_KEY";

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static SharedPreferences getInstance(final Context context) {
        SharedPreferences result = instance;
        if (result != null) {
            return result;
        }

        synchronized (Keys.class) {
            if (instance == null) {
                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        MASTER_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(KEY_SIZE)
                        .build();
                String mainKey = null;
                try {
                    mainKey = MasterKeys.getOrCreate(spec);
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                SharedPreferences sharedPreferences = null;
                try {
                    sharedPreferences = EncryptedSharedPreferences.create(
                            mainKey,
                            sharedPreferencesFile,
                            context,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                instance = sharedPreferences;
            }
            return (SharedPreferences) instance;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String getKey(Context context) {
        if (!isKeySet(context)) {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    DATABASE_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE)
                    .build();
            String mainKey = null;
            try {
                mainKey = MasterKeys.getOrCreate(spec);
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
            SharedPreferences.Editor editor = getInstance(context).edit();
            editor.putString(DB_KEY, mainKey);
            editor.apply();
        }
        return getInstance(context).getString(DB_KEY, "");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isKeySet(Context context) {
        return !getInstance(context).getString(DB_KEY, "").equals("");
    }
}
