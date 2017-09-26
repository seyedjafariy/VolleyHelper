package com.worldsnas.volleyhelper;

/**
 * Created by jafari on 9/10/2017.
 */

public interface ResponseListener<T> {
    void onResponse(T response, int responseCode, long responseTime);
}
