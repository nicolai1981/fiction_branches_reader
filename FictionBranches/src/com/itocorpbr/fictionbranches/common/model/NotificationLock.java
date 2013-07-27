package com.itocorpbr.fictionbranches.common.model;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.net.Uri;

public class NotificationLock {

    private final ReentrantLock  mLock;
    private final ArrayList<Uri> mNotifications;
    private final Context        mContext;

    public NotificationLock(Context context) {
        mContext = context.getApplicationContext();
        mNotifications = new ArrayList<Uri>();
        mLock = new ReentrantLock();
    }

    public void lock() {
        mLock.lock();
    }

    public void unlock() {
        if (mLock.getHoldCount() == 1) {
            notifyChanges();
        }
        mLock.unlock();
    }

    public void addNotifyChange(Uri uri) {
        lock();
        synchronized (mNotifications) {
            if (!mNotifications.contains(uri)) {
                mNotifications.add(uri);
            }
        }
        unlock();
    }

    private void notifyChanges() {
        synchronized (mNotifications) {
            for (Uri uri : mNotifications) {
                mContext.getContentResolver().notifyChange(uri, null);
                if (Log.ENABLED) Log.get().value("notifyChange", uri.toString());
            }
            mNotifications.clear();
        }
    }
}
