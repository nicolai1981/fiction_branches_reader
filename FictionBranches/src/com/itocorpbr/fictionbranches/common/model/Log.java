package com.itocorpbr.fictionbranches.common.model;

import com.itocorpbr.fictionbranches.common.log.Logger;

class Log {

    public static final boolean ENABLED = Logger.ENABLED && true;
    public static final String  TAG = "Commom.Model";

    public static Logger sInstance = new Logger(TAG, ENABLED);

    public static Logger get() {
        return sInstance;
    }
}
