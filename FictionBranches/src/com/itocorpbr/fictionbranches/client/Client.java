package com.itocorpbr.fictionbranches.client;

import android.content.Context;

import com.itocorpbr.fictionbranches.common.request.http.HttpClient;
import com.itocorpbr.fictionbranches.common.request.http.HttpRequest.HttpRequestListener;

public class Client {
    private final HttpClient mHttpClient;

    public Client(Context context) {
        mHttpClient = new HttpClient();
    }

    public void getChapter(String page, HttpRequestListener listener) {
        if (Log.ENABLED) Log.get().method();
        new GetChapter(mHttpClient, page, listener).start();
    }

    public void getLatestChapter(int days, HttpRequestListener listener) {
        if (Log.ENABLED) Log.get().method();
        new GetLatestChapter(mHttpClient, days, listener).start();
    }
}
