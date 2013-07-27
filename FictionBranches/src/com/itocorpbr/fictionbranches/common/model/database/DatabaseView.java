package com.itocorpbr.fictionbranches.common.model.database;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.itocorpbr.fictionbranches.common.model.Model;
import com.itocorpbr.fictionbranches.common.model.Model.NotifyChangeListener;

public abstract class DatabaseView extends Model implements BaseColumns, NotifyChangeListener {

    private static final String DROP_VIEW = "DROP VIEW IF EXISTS ";

    private final DatabaseManager mDbManager;

    public DatabaseView(Context context, DatabaseManager dbManager) {
        super(dbManager);
        mDbManager = dbManager;
    }

    protected Cursor query(String[] projection, String selection, String[] selectionArgs, String orderBy) {
        return mDbManager.query(this, projection, selection, selectionArgs, orderBy);
    }

    protected Cursor query(String sql, String[] selectionArgs) {
        return mDbManager.rawQuery(this, sql, selectionArgs);
    }

    protected void execSQL(String sql) {
        mDbManager.execSQL(sql);
    }

    @Override
    public void onAddNotifyChange() {
        addNotifyChange();
    }

    protected void addNotificationTable(String name) {
        mDbManager.getModel(name).addNotifyChangeListener(this);
    }

    @Override
    public void onDeleteModel() {
        execSQL(DROP_VIEW + getModelName());
    }
}
