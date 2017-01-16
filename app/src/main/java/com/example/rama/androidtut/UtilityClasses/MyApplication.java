package com.example.rama.androidtut.UtilityClasses;


import android.app.Application;

import static com.example.rama.androidtut.UtilityClasses.ConnectivityReceiver.connectivityReceiverListener;

public class MyApplication extends Application {

    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiverListener listener) {
        connectivityReceiverListener = listener;
    }
}
