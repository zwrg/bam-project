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

public class Preferences {
    private static volatile SharedPreferences instance;
    private static final int KEY_SIZE = 256;
    private static final String sharedPreferencesFile = "BAM_PROJECT_SHARED_PREFERENCES";
    private static final String MASTER_KEY_ALIAS = "MK_ALIAS";
    private static final String USER_ID_PREFERENCE = "BAM_USER_ID";
    public static final String USERNAME_PREFERENCE = "BAM_USERNAME";
    public static final String USER_PASSWORD_PREFERENCE = "BAM_USER_PASSWORD";

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static SharedPreferences getInstance(final Context context) {
        SharedPreferences result = instance;
        if (result != null) {
            return result;
        }

        synchronized (Preferences.class) {
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
    public static String getPreference(Context context, String key, String defaultValue) {
        return getInstance(context).getString(key, defaultValue);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void savePreference(Context context, String key, String value) {
        SharedPreferences.Editor editor = getInstance(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static int getUserId(Context context) {
        return getInstance(context).getInt(Preferences.USER_ID_PREFERENCE, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void setUserId(Context context, int value) {
        SharedPreferences.Editor editor = getInstance(context).edit();
        editor.putInt(Preferences.USER_ID_PREFERENCE, value);
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void removeUserId(Context context) {
        SharedPreferences.Editor editor = getInstance(context).edit();
        editor.remove(Preferences.USER_ID_PREFERENCE);
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void removePreference(Context context, String key) {
        SharedPreferences.Editor editor = getInstance(context).edit();
        editor.remove(key);
        editor.apply();
    }


}

