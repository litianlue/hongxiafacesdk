package com.dfxh;

import android.app.Application;
import android.content.Context;

import com.rokid.facelib.RokidFace;

import faceapi.PermissionUtils;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();



        context = getApplicationContext();
        mInstance = this;
    }
    public static App mInstance;
    private static Context context;
    public static Context getContext() {
        return context;
    }
}
