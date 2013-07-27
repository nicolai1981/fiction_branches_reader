package com.itocorpbr.fictionbranches;

import com.itocorpbr.fictionbranches.common.log.Logger;

class Log {

    public static final boolean ENABLED = Logger.ENABLED && true;
    public static final String  TAG = "GCM";

    public static Logger sInstance = new Logger(TAG, ENABLED);

    public static Logger get() {
        return sInstance;
    }
}
