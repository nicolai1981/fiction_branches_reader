package com.itocorpbr.fictionbranches.client;

public class ClientResult {
    public enum REQUEST {
        GET_CHAPTER,
        GET_LATEST_CHAPTERS,
        NONE
    }

    public REQUEST mReqId;
    public Object mData = null;

    public ClientResult(REQUEST reqId) {
        mReqId = reqId;
    }
}
