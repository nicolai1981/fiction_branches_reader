package com.itocorpbr.fictionbranches.common.request.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.itocorpbr.fictionbranches.Application;
import com.itocorpbr.fictionbranches.client.ClientResult;


public abstract class HttpRequest {

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML  = "application/xml";
    public static final String CONTENT_TYPE_MULTIPART  = "multipart/form-data";

    public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";
    public static final String CHARSET_UTF_8 = "UTF-8";

    public enum HTTP_METHOD {
        GET,
        POST,
        PUT,
        DELETE
    }

    private final HttpClient mHttpClient;
    protected final WeakReference<HttpRequestListener> mListener;
    protected int mStatusCode;

    public HttpRequest(HttpClient httpClient, HttpRequestListener listener) {
        mHttpClient = httpClient;
        mListener = new WeakReference<HttpRequestListener>(listener);
    }

    public final void start() {
        new MyRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void)null);
    }

    protected abstract HTTP_METHOD getRequestMethod();

    protected abstract Uri getRequestUri();

    protected Map<String, String> getRequestParams() {
        return null;
    }

    protected String getRequestContentType() {
        return null;
    }

    protected String getRequestCharset() {
        return null;
    }

    protected String getRequestContent() {
        return null;
    }

    protected boolean handleResponseStatus(int statusCode, String message) {
        if (Log.ENABLED) {
            Log.get().method();
            Log.get().value("statusCode", statusCode);
            Log.get().value("message", message);
        }
        mStatusCode = statusCode;
        return (statusCode == 200);
    }

    protected boolean handleResponseContent(InputStream is, String contentType) {
        if (Log.ENABLED) {
            Log.get().method();
            Log.get().value("contentType", contentType);
            Log.get().value("content", inputStreamToString(is));
        }
        return true;
    }

    protected String inputStreamToString(InputStream is) {
        if (Log.ENABLED) Log.get().method();

        BufferedReader reader;
        try {
            if (getRequestCharset() == null) {
                reader = new BufferedReader(new InputStreamReader(is));
            } else {
                reader = new BufferedReader(new InputStreamReader(is, getRequestCharset()));
            }
        } catch (UnsupportedEncodingException e1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            if (Log.ENABLED) Log.get().d(sb.toString());
            return sb.toString();
        } catch (IOException e) {
        }
        return null;
    }

    protected boolean inputStreamToXML(InputStream is, DefaultHandler contentHandler) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(contentHandler);
            xr.parse(new InputSource(is));
            return true;
        } catch(ParserConfigurationException pce) {
        } catch(SAXException se) {
        } catch(IOException ioe) {
        }
        return false;
    }

    protected JSONObject inputStreamToJSONObject(InputStream is) {
        if (Log.ENABLED) Log.get().method();
        try {
            return new JSONObject(inputStreamToString(is));
        } catch (JSONException e) {
            if (Log.ENABLED) Log.get().e(e);
        }
        return null;
    }

    protected JSONArray inputStreamToJSONArray(InputStream is) {
        if (Log.ENABLED) Log.get().method();
        try {
            return new JSONArray(inputStreamToString(is));
        } catch (JSONException e) {
            if (Log.ENABLED) Log.get().e(e);
        }
        return null;
    }

    protected void onPostExecute(boolean result) {
    }

    protected boolean execute() {
        if (Log.ENABLED) Log.get().method();
        boolean result = false;
        Uri uri = getRequestUri();

        Map<String, String> params = getRequestParams();
        if (params != null) {
            Uri.Builder builder = uri.buildUpon();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
            uri = builder.build();
        }

        if (Log.ENABLED) {
            Log.get().value("url", uri.toString());
            Log.get().value("method", getRequestMethod().toString());
        }

        switch (getRequestMethod()) {
            case GET:
                result = handleResponse(mHttpClient.get(uri));
            break;
            case DELETE:
                result = handleResponse(mHttpClient.delete(uri));
            break;
            case POST:
                result = handleResponse(mHttpClient.post(uri, getRequestContent(), getRequestContentType()));
            break;
            case PUT:
                result = handleResponse(mHttpClient.put(uri, getRequestContent(), getRequestContentType()));
            break;
        }
        return result;
    }

    private boolean handleResponse(HttpResponse response) {
        if (Log.ENABLED) Log.get().method();
        boolean result = false;
        try {
            if (response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                String message = response.getStatusLine().getReasonPhrase();

                if (handleResponseStatus(statusCode, message)) {
                    String contentType = null;

                    for (Header header : response.getAllHeaders()) {
                        if (Log.ENABLED) Log.get().value("header", header.getName() + ":" + header.getValue());
                        if (TextUtils.equals("Content-Type", header.getName())) {
                            contentType = header.getValue();
                        }
                    }
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream instream = entity.getContent();
                        result = handleResponseContent(instream, contentType);
                        instream.close();
                    }
                }
            }
        } catch (IOException e) {
        }
        return result;
    }

    private class MyRequest extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return HttpRequest.this.execute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            HttpRequest.this.onPostExecute(result);
        }
    }

    public interface HttpRequestListener {
        void onHttpRequestFinished(ClientResult result);
    }

    /**
     * Check if the device is connected to Internet.
     * 
     * @return true if device is connected.
     */
    public static boolean isOnline() {
        Context context = Application.getContext();
        ConnectivityManager connectivityMng = (ConnectivityManager) context
                                                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityMng.getActiveNetworkInfo();
        if ((netInfo != null) && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
