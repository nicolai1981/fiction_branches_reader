package com.itocorpbr.fictionbranches.common.request.http;

import com.itocorpbr.fictionbranches.common.log.Logger;

class Log {

    public static final boolean ENABLED = Logger.ENABLED && true;
    public static final String  TAG = "Common.Http";

    public static Logger sInstance = new Logger(TAG, ENABLED);

    public static Logger get() {
        return sInstance;
    }
}
