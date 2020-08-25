package me.ictlinkbd.com.projectalpha.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import static me.ictlinkbd.com.projectalpha.util.AppPermission.chkGPS;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.chkLocationPermission;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.showAlert;


public class LocationTask {

    private Context context;
    private FusedLocationProviderClient locationProviderClient;
    private boolean isLocationOpen=false;
    private String TAG = "LocationTask ";

    public LocationTask(Context context) {
        this.context = context;
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
//        setGpsServices(new CheckGpsServices(context));
//        Log.e(TAG, "LocationTask const: called");
    }


//    public CheckGpsServices getGpsServices() {
//        return gpsServices;
//    }
//
//    public void setGpsServices(CheckGpsServices gpsServices) {
//        this.gpsServices = gpsServices;
//    }


    public boolean isLocationOpen() {
        return isLocationOpen;
    }

    public void setLocationOpen(boolean locationOpen) {
        isLocationOpen = locationOpen;
    }

    public void getDeviceLastLocation(final LastLocationCallBack callBack) {
        locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    if (callBack != null)
                        callBack.getDeviceLastLocation(location);
                    Log.e(TAG + " last location", "location =! null " + location.getLatitude());

                } else {
                    Log.e(TAG + " last location", "location = null ");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, " " + e.getLocalizedMessage());
            }
        });
    }


//    public void getLocationUpdates(LocationCallback locationCallback) {
//        Log.e(TAG, "getLocationUpdates: called");
//        locationProviderClient.requestLocationUpdates(getLocationRequest(), locationCallback, null);
//    }

    public void getLocationUpdates(LocationCallback locationCallback) {
        if (chkGPS(context)) {
            isLocationOpen=true;
            Log.e(TAG, "getLocationUpdates: called");
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(20 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            isLocationOpen=true;
            showAlert(context);
        }
    }


    public void getLocationUpdates(LocationCallback locationCallback,int interval, int fastestInterval) {
        if (chkGPS(context)) {
            isLocationOpen=true;
            Log.e(TAG, "getLocationUpdates: called");
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(interval * 1000);
            locationRequest.setFastestInterval(fastestInterval * 1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            isLocationOpen=true;
            showAlert(context);
        }
    }
    

    public void stopLocationUpdate(LocationCallback locationCallback) {
        Log.e(TAG, "stopLocationUpdate: called");
        isLocationOpen=false;
        locationProviderClient.removeLocationUpdates(locationCallback);
    }


    public interface LastLocationCallBack {
        void getDeviceLastLocation(Location location);
    }

}
