package com.example.bamproject;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.sqlite.db.SupportSQLiteQueryBuilder;

public class CardProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.example.bamproject";
    static final String URL = "content://" + PROVIDER_NAME + "/card";
    static final int uriCode = 101;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "card", uriCode);
        uriMatcher.addURI(PROVIDER_NAME, "card/*", uriCode);
    }

    private static AppDatabase database = null;

    public CardProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.item";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        database = AppDatabase.getInstance(getContext());
        return database != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        if (uriMatcher.match(uri) == uriCode) {
            SupportSQLiteQueryBuilder queryBuilder = SupportSQLiteQueryBuilder
                    .builder(Card.tableName)
                    .columns(projection)
                    .selection(selection, selectionArgs)
                    .orderBy(sortOrder);

            cursor = database.getOpenHelper().getReadableDatabase().query(queryBuilder.create());
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}