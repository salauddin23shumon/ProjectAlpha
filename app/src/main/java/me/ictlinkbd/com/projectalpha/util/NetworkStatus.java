package me.ictlinkbd.com.projectalpha.util;

import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static me.ictlinkbd.com.projectalpha.util.Utility.isNetworkAvailable;

public class NetworkStatus {

    private static FirebaseAuth auth;
    private static FirebaseUser user;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void chkNetworkCallback(ConnectivityManager manager, final NetStatusCallBack callBack) {
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Log.i("*****", "connected to net" );
                initFirebase(callBack);
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Log.i("*****", "connection lost");
                callBack.networkStatus(false, null);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager.registerDefaultNetworkCallback(networkCallback);
        }else {
            if (isNetworkAvailable(manager)) {
                initFirebase(callBack);
            }else
                callBack.networkStatus(false,null);
        }
    }

    public static void initFirebase(NetStatusCallBack callBack){
        Log.i("inside", "initFirebase");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        callBack.networkStatus(true, auth);
    }

    public interface NetStatusCallBack{
        void networkStatus(boolean isConnected, FirebaseAuth auth);
    }
}
