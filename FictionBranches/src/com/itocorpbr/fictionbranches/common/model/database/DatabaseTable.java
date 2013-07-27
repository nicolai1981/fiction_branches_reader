package com.itocorpbr.fictionbranches.common.model.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.itocorpbr.fictionbranches.common.model.Model;

public abstract class DatabaseTable extends Model implements BaseColumns {

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    private final DatabaseManager mDbManager;

    public DatabaseTable(Context context, DatabaseManager dbManager) {
        super(dbManager);
        mDbManager = dbManager;
    }

    protected Cursor query(String[] projection, String selection, String[] selectionArgs, String orderBy) {
        return mDbManager.query(this, projection, selection, selectionArgs, orderBy);
    }

    protected Cursor query(boolean distinct, String[] projection, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return mDbManager.query(distinct, this, projection, selection, selectionArgs, groupBy, having, orderBy);
    }
    
    protected Cursor rawQuery(String sql, String[] selectionArgs) {
        return mDbManager.rawQuery(this, sql, selectionArgs);
    }

    protected boolean insert(ContentValues contentValues) {
        return mDbManager.insert(this, contentValues);
    }

    protected boolean replace(ContentValues contentValues, String selection, String[] selectionArgs) {
        return mDbManager.replace(this, contentValues, selection, selectionArgs);
    }

    protected boolean update(ContentValues contentValues, String selection, String[] selectionArgs) {
        return mDbManager.update(this, contentValues, selection, selectionArgs);
    }

    protected boolean delete(String selection, String[] selectionArgs) {
        return mDbManager.delete(this, selection, selectionArgs);
    }

    protected void putValue(ContentValues values, String key, String value) {
        if (value == null) {
            values.putNull(key);
        } else {
            values.put(key, value);
        }
    }

    protected void execSQL(String sql) {
        mDbManager.execSQL(sql);
    }

    @Override
    public void onDeleteModel() {
        execSQL(DROP_TABLE + getModelName());
    }

    protected String convertSQLParam(String text) {
        return text.replace("'", "''").replace("%", "[%]");
    }

    public class BatchOperation {
        private static final int BATCH_SIZE = 100;

        private final ArrayList<Runnable> mOperationList;
        private final int                 mMaxSize;

        public BatchOperation() {
            this(BATCH_SIZE);
        }

        public BatchOperation(int maxSize) {
            mOperationList = new ArrayList<Runnable>();
            mMaxSize = maxSize;
        }

        protected void addOperation(Runnable operation) {
            mOperationList.add(operation);
            if (mOperationList.size() >= mMaxSize) {
                flush();
            }
        }

        public void flush() {
            lock();
            mDbManager.beginTransaction();
            try {
                for (Runnable operation : mOperationList) {
                    operation.run();
                }
                mOperationList.clear();
            } finally {
                mDbManager.endTransaction(true);
                unlock();
            }
        }
    }
}
