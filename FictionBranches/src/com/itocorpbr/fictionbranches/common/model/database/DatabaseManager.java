package com.itocorpbr.fictionbranches.common.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.itocorpbr.fictionbranches.common.model.Model;
import com.itocorpbr.fictionbranches.common.model.ModelManager;

/**
 * Implements "connection sent" database, which stores which email addresses where already sent a
 * connect invitation.
 */
public class DatabaseManager extends ModelManager {

    private final SQLiteOpenHelper mDbHelper;
    private SQLiteDatabase         mDatabase;

    /**
     * Default constructor. Private to guarantee there is ever only one instance of
     * DatabaseOpenHelper. This avoids concurrency problems accessing the DB.
     *
     * @param context
     */
    public DatabaseManager(Context context, String name, int version) {
        super(context, name, version);
        mDbHelper = new DatabaseHelper(context, name, version);
    }

    protected Cursor query(Model model, String[] projection, String selection, String[] selectionArgs, String orderBy) {
        mDatabase = mDbHelper.getReadableDatabase();
        Cursor cursor = mDatabase.query(model.getModelName(), projection, selection, selectionArgs, null, null, orderBy);
        if (cursor == null) {
            throw new IllegalArgumentException();
        }
        cursor.setNotificationUri(getContext().getContentResolver(), model.getUri());
        return cursor;
    }

    protected Cursor query(boolean distinct, Model model, String[] projection, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        mDatabase = mDbHelper.getReadableDatabase();
        Cursor cursor = mDatabase.query(distinct, model.getModelName(), projection, selection, selectionArgs, groupBy, having, orderBy, null);
        if (cursor == null) {
            throw new IllegalArgumentException();
        }
        cursor.setNotificationUri(getContext().getContentResolver(), model.getUri());
        return cursor;
    }

    protected Cursor rawQuery(Model model, String sql, String[] selectionArgs) {
        mDatabase = mDbHelper.getReadableDatabase();
        Cursor cursor = mDatabase.rawQuery(sql, selectionArgs);
        if (cursor == null) {
            throw new IllegalArgumentException();
        }
        cursor.setNotificationUri(getContext().getContentResolver(), model.getUri());
        return cursor;
    }

    protected boolean insert(Model model, ContentValues contentValues) {
        mDatabase = mDbHelper.getWritableDatabase();
        long id = 0;
        boolean result = false;
        try{
            mDatabase.beginTransaction();
            id = mDatabase.insert(model.getModelName(), null, contentValues);
            if (id >= 0) {
                result = true;
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
        if (result) {
            model.addNotifyChange();
        }
        return result;
    }

    protected boolean replace(Model model, ContentValues contentValues, String selection, String[] selectionArgs) {
        mDatabase = mDbHelper.getWritableDatabase();
        boolean result = false;
        try {
            mDatabase.beginTransaction();
            long id = mDatabase.replace(model.getModelName(), null, contentValues);
            if (id >= 0) {
                result = true;
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
        if(result){
            model.addNotifyChange();
        }
        return result;
    }

    protected boolean update(Model model, ContentValues contentValues, String selection, String[] selectionArgs) {
        mDatabase = mDbHelper.getWritableDatabase();
        boolean result = false;
        try {
            mDatabase.beginTransaction();
            long count = mDatabase.update(model.getModelName(), contentValues, selection, selectionArgs);
            if (count > 0L) {
                result = true;
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
        if(result){
            model.addNotifyChange();
        }
        return result;
    }

    protected boolean delete(Model model, String selection, String[] selectionArgs) {
        mDatabase = mDbHelper.getWritableDatabase();
        boolean result = false;
        try{
            mDatabase.beginTransaction();
            int count = mDatabase.delete(model.getModelName(), selection, selectionArgs);
            if (count > 0) {
                result = true;
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
        if(result){
            model.addNotifyChange();
        }
        return result;
    }

    protected void execSQL(String sql) {
        if (mDatabase != null) {
            mDatabase.execSQL(sql);
        }
    }

    protected void beginTransaction() {
        if (mDatabase != null) {
            mDatabase.beginTransaction();
        }
    }

    protected void endTransaction(boolean forceOk) {
        if (mDatabase != null) {
            if (forceOk) {
                mDatabase.setTransactionSuccessful();
            }
            mDatabase.endTransaction();
        }
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name, int version) {
            super(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            mDatabase = database;
            onCreateModel();
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            mDatabase = database;
            onUpgradeModel(oldVersion, newVersion);
        }

        @Override
        public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            mDatabase = database;
            downgrade(oldVersion, newVersion);
        }
    }
}
