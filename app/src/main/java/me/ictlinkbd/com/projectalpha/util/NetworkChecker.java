package me.ictlinkbd.com.projectalpha.util;

import android.app.Application;

import com.droidnet.DroidNet;

public class NetworkChecker extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DroidNet.init(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        DroidNet.getInstance().removeAllInternetConnectivityChangeListeners();
    }
}
