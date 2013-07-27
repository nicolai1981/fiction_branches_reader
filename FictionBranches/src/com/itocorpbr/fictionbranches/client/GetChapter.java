package com.itocorpbr.fictionbranches.client;

import java.io.InputStream;

import android.net.Uri;

import com.itocorpbr.fictionbranches.client.ClientResult.REQUEST;
import com.itocorpbr.fictionbranches.common.request.http.HttpClient;
import com.itocorpbr.fictionbranches.common.request.http.HttpRequest;

public class GetChapter extends HttpRequest {
    private ClientResult mResult = null;
    private String mPage = null;
    private String mParent = null;

    public GetChapter(HttpClient httpClient, String page, String parent, HttpRequestListener listener) {
        super(httpClient, listener);
        mResult = new ClientResult(REQUEST.GET_CHAPTER);
        mPage = page;
        mParent = parent;
    }

    @Override
    protected HTTP_METHOD getRequestMethod() {
        return HTTP_METHOD.GET;
    }

    @Override
    protected Uri getRequestUri() {
        return Uri.parse(ClientParser.WEB_SERVER + "fbstorypage.pl?path=nospamstory&page=" + mPage);
    }

    @Override
    protected String getRequestContentType() {
        return CONTENT_TYPE_XML;
    }

    @Override
    protected String getRequestCharset() {
        return CHARSET_ISO_8859_1;
    }

    @Override
    protected String getRequestContent() {
        if (Log.ENABLED) Log.get().method();
        return null;
    }

    @Override
    protected boolean handleResponseContent(InputStream is, String contentType) {
        if (Log.ENABLED) Log.get().method();

        String html = inputStreamToString(is);
        if (html != null) {
            return handleEvent(html);
        }
        return false;
    }

    private boolean handleEvent(String html) {
        if (Log.ENABLED) Log.get().method();

        if (!ClientParser.parseChapterRequestResult(mPage, mParent, html, mResult)) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(boolean result) {
        if (Log.ENABLED) Log.get().method();

        HttpRequestListener listener = mListener.get();
        if (listener != null) {
            listener.onHttpRequestFinished(mResult);
        }
        mListener.clear();
    }
}
