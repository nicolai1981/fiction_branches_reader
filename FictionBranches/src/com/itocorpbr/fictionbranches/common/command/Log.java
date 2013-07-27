package com.itocorpbr.fictionbranches.common.command;

import com.itocorpbr.fictionbranches.common.log.Logger;

class Log {

    public static final boolean ENABLED = Logger.ENABLED && true;
    public static final String  TAG = "Command";

    public static Logger sInstance = new Logger(TAG, ENABLED);

    public static Logger get() {
        return sInstance;
    }
}
