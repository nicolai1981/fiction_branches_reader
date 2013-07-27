package com.itocorpbr.fictionbranches.common.model;

import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;

public abstract class Model {

    private final ArrayList<NotifyChangeListener> mNotifyChangeListeners;
    private final Uri mContentUri;
    private final ModelManager mModelManager;

    public Model(ModelManager modelManager) {
        mModelManager = modelManager;
        mNotifyChangeListeners = new ArrayList<NotifyChangeListener>();
        mContentUri = Uri.withAppendedPath(Uri.parse("content://" + mModelManager.getContext().getPackageName()), getModelName());
    }

    public void addNotifyChangeListener(NotifyChangeListener listener) {
        mNotifyChangeListeners.add(listener);
    }

    public Uri getUri() {
        return mContentUri;
    }

    protected void lock() {
        mModelManager.getNotificationLock().lock();
    }

    protected void unlock() {
        mModelManager.getNotificationLock().unlock();
    }

    public void addNotifyChange() {
        addNotifyChange(getUri());
    }

    protected void addNotifyChange(Uri uri) {
        mModelManager.getNotificationLock().addNotifyChange(uri);
        for (NotifyChangeListener listener: mNotifyChangeListeners) {
            listener.onAddNotifyChange();
        }
    }

    protected void setNotificationUri(Cursor cursor) {
        setNotificationUri(cursor, getUri());
    }

    protected void setNotificationUri(Cursor cursor, Uri uri) {
        cursor.setNotificationUri(mModelManager.getContext().getContentResolver(), uri);
    }

    public void onUpgradeModel(int oldVersion, int newVersion) {
        onDeleteModel();
        onCreateModel();
    }

    public void onDowngradeModel(int oldVersion, int newVersion) {
        onDeleteModel();
        onCreateModel();
    }

    public void onAttachModel() {
    }

    public abstract void onDeleteModel();

    public abstract void onCreateModel();

    public abstract String getModelName();

    public interface NotifyChangeListener {
        void onAddNotifyChange();
    }
}
