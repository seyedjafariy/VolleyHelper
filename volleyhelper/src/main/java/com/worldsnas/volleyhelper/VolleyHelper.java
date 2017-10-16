package com.worldsnas.volleyhelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Seyed Jafari on 9/24/2017.
 */

public class VolleyHelper<T> {
    private String TAG = VolleyHelper.class.getSimpleName();
    private static long objCount = 0;

    @SuppressLint("StaticFieldLeak")
    private static Context sGlobalContext;
    private static RequestQueue sRequestQueue;
    private RequestQueue localRequestQueue;
    private Context localContext;

    private GsonRequest<T> mRequest;
    private TypeToken<T> requestedType;

    private String requestTag;
    private Uri mUri;
    private Integer retryTimes;
    private boolean noCacheRequest = false;

    private int requestMethod = Request.Method.GET;
    private String contentType;
    private String baseUrl;

    private ResponseListener<T> mResponse;
    private Response.ErrorListener mErrorListener;

    private ArrayMap<String, String> headers;
    private ArrayMap<String, String> query;
    private ArrayMap<String, String> formBody;
    private String fileAddress;
    private String jsonBody;

    private Integer offSet;
    private String offSetTag;


    public static void init(Context context) {
        sGlobalContext = context;
    }

    /**
     * when starting a new request this method will be called to retrieve the Global Queue
     *
     * @return RequestQueue
     */
    private static RequestQueue getGlobalRequestQueue() {
        if (sGlobalContext == null)
            throw new NullPointerException("VolleyHelper Must Be Initialized In Application Class");
        else {
            if (sRequestQueue == null) {
                sRequestQueue = Volley.newRequestQueue(sGlobalContext);
            }

            return sRequestQueue;
        }

    }

    /**
     * when starting a new request this method will be called to retrieve the Local Queue
     *
     * @return RequestQueue
     */
    private RequestQueue getLocalRequestQueue() {
        if (localRequestQueue == null) {
            localRequestQueue = Volley.newRequestQueue(localContext);
        }

        return localRequestQueue;
    }

    /**
     * adds the created volley request to the volley queue with its corresponding tag
     */
    private void addToRequestQueue() {
        if (localContext != null)
            getLocalRequestQueue().add(mRequest);
        else
            getGlobalRequestQueue().add(mRequest);
    }

    private void cancelPendingRequests(Object tag) {
        RequestQueue queue;
        if (localContext != null)
            queue = getLocalRequestQueue();
        else
            queue = getGlobalRequestQueue();

        if (queue != null) {
            queue.cancelAll(tag);
        }
    }

    /**
     * cancel the request using provided TAG
     */
    public void cancel() {
        if (mRequest != null) {
            mRequest.cancelRequest();
            mRequest.cancel();
        }
        RequestQueue queue;
        if (localContext != null)
            queue = getLocalRequestQueue();
        else
            queue = getGlobalRequestQueue();
        if (queue != null)
            queue.cancelAll(requestTag);
    }

    /**
     * cancels all pending global request in the queue
     */
    public static void cancelAll() {
        RequestQueue queue;
        queue = getGlobalRequestQueue();

        if (queue != null)
            queue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
    }

    public VolleyHelper(TypeToken<T> requestedType) {
        objCount++;
        requestTag = "Req_" + objCount;
        this.requestedType = requestedType;
    }

    public VolleyHelper(Class<T> requestedClass) {
        this(TypeToken.get(requestedClass));
    }

    public VolleyHelper<T> with(Context context) {
        this.localContext = context;
        return this;
    }

    public VolleyHelper<T> release(){
        localContext = null;
        mResponse = null;
        mErrorListener = null;
        mRequest.release();
        return this;
    }

    public VolleyHelper<T> setRequestMethod(int requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    public VolleyHelper<T> setResponseListener(ResponseListener<T> response) {
        this.mResponse = response;
        return this;
    }

    public VolleyHelper<T> setErrorListener(Response.ErrorListener errorListener) {
        this.mErrorListener = errorListener;
        return this;
    }

    public VolleyHelper<T> setRequestTag(String requestTag) {
        this.requestTag = requestTag;
        return this;
    }

    public VolleyHelper<T> setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public VolleyHelper<T> shouldNotCache(boolean shouldNotCache) {
        this.noCacheRequest = shouldNotCache;
        return this;
    }

    public VolleyHelper<T> setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public VolleyHelper<T> setOffSet(int offSet) {
        this.offSet = offSet;
        this.offSetTag = "page";
        return this;
    }

    public VolleyHelper<T> setOffSetTag(String offSetTag) {
        this.offSetTag = offSetTag;
        return this;
    }

    public VolleyHelper<T> setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public VolleyHelper<T> addFormBody(String key, String value) {
        if (formBody == null)
            formBody = new ArrayMap<>();
        formBody.put(key, value);
        return this;
    }

    public VolleyHelper<T> addQuery(String key, String value) {
        if (query == null)
            query = new ArrayMap<>();
        query.put(key, value);
        return this;
    }

    public VolleyHelper<T> addHeader(String key, String value) {
        if (headers == null)
            headers = new ArrayMap<>();
        headers.put(key, value);
        return this;
    }

    public VolleyHelper<T> sendFile(String fileAddress){
        this.fileAddress = fileAddress;
        return this;
    }

    private VolleyHelper<T> setRequestedType(TypeToken<T> requestedType){
        this.requestedType = requestedType;
        return this;
    }

    private VolleyHelper<T> requestBuilder(){
        mRequest = new GsonRequest<>(requestMethod, mUri.toString(), mResponse, mErrorListener);

        if (contentType != null)
            mRequest.setBodyContentType(contentType);
        if (headers != null)
            mRequest.setHeader(headers);
        if (formBody != null)
            mRequest.setFormData(formBody);
        if (jsonBody != null)
            mRequest.setJsonBody(jsonBody);

        if (this.retryTimes != null) {
            mRequest.setRetryPolicy(new DefaultRetryPolicy(
                    retryTimes,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }

        if (this.noCacheRequest)
            mRequest.setShouldCache(false);

        mRequest.setType(requestedType);

        mRequest.setTag(TextUtils.isEmpty(requestTag) ? TAG : requestTag);

        return this;
    }

    public VolleyHelper<T> start() {
        uriBuilder();

        requestBuilder();

        addToRequestQueue();
        return this;
    }

    /**
     * builds the url for our request
     */
    private void uriBuilder() {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();

        if (query != null && query.size() > 0)
            for (int i = 0; i<query.size() ; i++)
                builder.appendQueryParameter(query.keyAt(i), query.valueAt(i));

        if (offSet != null) {
            builder.appendQueryParameter(offSetTag, offSet.toString());
        }

        mUri = builder.build();
    }

    /**
     * Automatically starts the same request with different offsets
     *
     * @param offSet
     */
    public VolleyHelper<T> newOffSet(int offSet) {
        this.offSet = offSet;
        start();
        return this;
    }

    /**
     * Automatically starts the same request with different offsets and new Listener
     *
     * @param offSet
     * @param responseListener
     */
    public VolleyHelper<T> newOffSet(int offSet, ResponseListener<T> responseListener) {
        this.offSet = offSet;
        this.mResponse = responseListener;
        start();
        return this;
    }

    /**
     * updates the OffSet and Response.Listener and creates new request which has to be started manually
     *
     * @param offSet
     * @param responseListener
     */
    public VolleyHelper<T> setOffSet(int offSet, ResponseListener<T> responseListener) {
        this.offSet = offSet;
        this.mResponse = responseListener;
        return this;
    }

    public String getUrl() {
        return mUri.toString();
    }

    public String getRequestTag() {
        return requestTag;
    }

}
