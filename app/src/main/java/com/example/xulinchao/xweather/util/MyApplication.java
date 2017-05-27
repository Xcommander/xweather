package com.example.xulinchao.xweather.util;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by xulinchao on 2017/5/27.
 */

public class MyApplication extends Application {
    private static  Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext=getApplicationContext();
        LitePal.initialize(mContext);
    }
    public static Context getContext(){
        return mContext;
    }
}
