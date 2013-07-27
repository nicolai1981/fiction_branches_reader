package com.itocorpbr.fictionbranches.common.request.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.net.Uri;

public class HttpClient {

    private static final int TIMEOUT = 60 * 1000;

    public HttpResponse post(Uri uri, String content, String contentType) {
        if (Log.ENABLED)
            Log.get().d("content:<" + content + ">");

        HttpPost request = new HttpPost(uri.toString());
        if ((content != null) && (contentType != null)) {
            request.addHeader("Content-Type", contentType);
            request.setEntity(getEntityParams(content));
        }

        return getHttpResponse(request);
    }

    public HttpResponse put(Uri uri, String content, String contentType) {
        if (Log.ENABLED)
            Log.get().d("content:<" + content + ">");

        HttpPut request = new HttpPut(uri.toString());
        if ((content != null) && (contentType != null)) {
            request.addHeader("Content-Type", contentType);
            request.setEntity(getEntityParams(content));
        }

        return getHttpResponse(request);
    }

    public HttpResponse get(Uri uri) {
        HttpGet request = new HttpGet(uri.toString());

        return getHttpResponse(request);
    }

    public HttpResponse delete(Uri uri) {
        HttpDelete request = new HttpDelete(uri.toString());

        return getHttpResponse(request);
    }

    protected HttpEntity getEntityParams(String content) {
        StringEntity entity = null;
        try {
            entity = new StringEntity(content, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return entity;
    }

    /**
     * @param request
     * @return
     * @throws HttpException
     */
    protected HttpResponse getHttpResponse(HttpRequestBase request) {
        HttpResponse httpResponse = null;

        try {
            HttpParams params = request.getParams();
            HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, TIMEOUT);
            DefaultHttpClient httpClient = new DefaultHttpClient(params);
            httpResponse = httpClient.execute(request);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return httpResponse;
    }

    private static class CustomSSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory {
        private SSLSocketFactory mFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) 
                                      throws IOException, UnknownHostException {
            return mFactory.createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return mFactory.createSocket();
        }

        public CustomSSLSocketFactory() throws KeyManagementException, UnrecoverableKeyException,
                NoSuchAlgorithmException, KeyStoreException {
            super(null);
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                TrustManager[] tm = new TrustManager[] {
                        new X509_Trust_Manager()
                };
                context.init(null, tm, new SecureRandom());

                mFactory = context.getSocketFactory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class X509_Trust_Manager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
