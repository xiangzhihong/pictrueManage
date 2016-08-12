package com.xzh.picturesmanager.base;

import android.app.Application;

import com.xzh.picturesmanager.utils.YMTImageLoader;

public class YmatouApplication extends Application {

    private static YmatouApplication mInstance = null;

    public static synchronized YmatouApplication getInstance() {
        if (mInstance == null) {
            mInstance = new YmatouApplication();
        }
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        init();
    }

    private void init() {
         YMTImageLoader.init();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        YMTImageLoader.clearMemoryCache();
    }

}
