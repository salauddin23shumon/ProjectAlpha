package com.sss.bitm.projectalpha.nearByPlace.geofencing;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;


public class GeoFencingOperation {

    private Context context;
    private GeofencingClient client;
    private PendingIntent pendingIntent;
    private ArrayList<Geofence> geofenceArrayList = new ArrayList<>();
//    private NearByPlaceDetailFragment mapFragmentContainer = new NearByPlaceDetailFragment();

    public GeoFencingOperation(Context context) {
        this.context = context;
        client = LocationServices.getGeofencingClient(context);
    }


    public void addToGeoFencing(String placeName, double lat, double lng) {

        pendingIntent = null;

        Geofence geofence = new Geofence.Builder()
                .setRequestId(placeName)
                .setCircularRegion(lat, lng, 200)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(2 * 60 * 60 * 1000)
                .build();

        geofenceArrayList.add(geofence);


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        client.addGeofences(geofencingRequest(), pendingIntentRequest()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Area is marked", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private GeofencingRequest geofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceArrayList);
        return builder.build();
    }

    private PendingIntent pendingIntentRequest(){

        if (pendingIntent != null){
            return pendingIntent;
        }else {

            Intent intent = new Intent(context, GeoFencingIntentService.class);
            pendingIntent = PendingIntent.getService(context, 11, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            return pendingIntent;
        }
    }
}
