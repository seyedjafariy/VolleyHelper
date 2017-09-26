package com.worldsnas.volleyhelper;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static com.worldsnas.volleyhelper.ContentType.FORM_DATA;


/**
 * Created by jafari on 9/10/2017.
 */

public class GsonRequest<T> extends Request<T> {
    //    private final Response.Listener<T> mListener1;
    private final String TAG = GsonRequest.class.getSimpleName();
    private ResponseListener<T> mListener;
    private Gson mGson;
    private Map<String, String> header;
    private Map<String, String> formData;
    private int responseCode;
    private long responseTime;
    private String bodyContentType;
    private String jsonBody;
    private TypeToken<T> requestedType;
    private boolean shouldRespond = true;

    public GsonRequest(int method, String url, ResponseListener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        mGson = new Gson();
    }

    public GsonRequest(String url, ResponseListener<T> listener, Response.ErrorListener errorListener) {
        this(0, url, listener, errorListener);
    }

    @SuppressWarnings("unchecked")
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        this.responseCode = response.statusCode;
        this.responseTime = response.networkTimeMs;

        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException var4) {
            parsed = new String(response.data);
        }
        Log.d(TAG, "response in String: " + parsed);
        if (requestedType.equals(new TypeToken<String>() {}))
            return Response.success((T)parsed, HttpHeaderParser.parseCacheHeaders(response));
        else
            return Response.success((T) mGson.fromJson(parsed, requestedType.getType()), HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(T t) {
        if (shouldRespond)
            mListener.onResponse(t, responseCode, responseTime);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        try {
            return jsonBody == null ? super.getBody() : jsonBody.getBytes("utf-8");
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", jsonBody, "utf-8");
            return super.getBody();
        }
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        if (bodyContentType != null && bodyContentType.equalsIgnoreCase(FORM_DATA)) {
            return formData;
        } else
            return super.getParams();
    }

    @Override
    public String getBodyContentType() {
        if (bodyContentType != null)
            return bodyContentType;
        else
            return super.getBodyContentType();

    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return header;
    }

    public GsonRequest<T> setHeader(Map<String, String> header) {
        this.header = header;
        return this;
    }

    public GsonRequest<T> setBodyContentType(String bodyContentType) {
        this.bodyContentType = bodyContentType;
        return this;
    }

    public GsonRequest<T> setFormData(Map<String, String> formData) {
        this.formData = formData;
        return this;
    }

    public GsonRequest<T> setJsonBody(String jsonBody) {
        this.jsonBody = jsonBody;
        return this;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public GsonRequest<T> setType(TypeToken<T> requestedType) {
        this.requestedType = requestedType;
        return this;
    }

    public void release(){
        mListener = null;
    }

    public GsonRequest<T> cancelRequest() {
        shouldRespond = false;
        return this;
    }

}
