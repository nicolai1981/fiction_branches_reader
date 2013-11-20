package com.itocorpbr.fictionbranches.common.log;

import android.util.Log;

public class Logger {

    public static final boolean ENABLED = true;

    private final String  mTag;
    private final boolean mEnabled;

    public Logger(String tag, boolean enabled) {
        mTag = tag;
        mEnabled = enabled;
    }

    public void value(String key, String value) {
        if (ENABLED) if (mEnabled) Log.d(mTag, "+" + key + ": " + value);
    }

    public void value(String key, int value) {
        if (ENABLED) if (mEnabled) Log.d(mTag, "+" + key + ": " + value);
    }

    public void value(String key, boolean value) {
        if (ENABLED) if (mEnabled) Log.d(mTag, "+" + key + ": " + value);
    }

    public void value(String key, long value) {
        if (ENABLED) if (mEnabled) Log.d(mTag, "+" + key + ": " + value);
    }

    public void d(String message) {
        if (ENABLED) if (mEnabled) Log.d(mTag, message);
    }

    public void d(String message, Throwable exception) {
        if (ENABLED) if (mEnabled) Log.d(mTag, message, exception);
    }

    public void e(String message) {
        if (ENABLED) if (mEnabled) Log.e(mTag, message);
    }

    public void e(Throwable exception) {
        if (ENABLED) if (mEnabled) Log.e(mTag, "Throwable:", exception);
    }

    public void method() {
        if (ENABLED) if (mEnabled) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String className = stackTrace[3].getClassName();
            className = className.substring(1 + className.lastIndexOf('.'));
            Log.d(mTag, className + "." + stackTrace[3].getMethodName());
        }
    }

}
