package com.example.xulinchao.xweather.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by xulinchao on 2017/5/27.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(final String address, final Callback call){
        OkHttpClient okHttpClient=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(call);

    }
}
