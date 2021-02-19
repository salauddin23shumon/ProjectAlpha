package com.sss.bitm.projectalpha.nearByPlace.geofencing;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;


import com.sss.bitm.projectalpha.R;

import static com.sss.bitm.projectalpha.MainActivity.CurrentLocation;


public class GeoFencingIntentService extends IntentService {


    private final String channelID = "my_channel_id";

    public GeoFencingIntentService() {
        super("GeoFencingIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        int transitionType = geofencingEvent.getGeofenceTransition();
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        String transitionTypeSt = "";
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                transitionTypeSt = "Entered";
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                transitionTypeSt = "Exited";
                break;
        }


        if (CurrentLocation!=null) {
            ArrayList<String> geofencesId = new ArrayList<>();
            for (Geofence g : triggeringGeofences) {
                geofencesId.add(g.getRequestId());
            }
            String notificationST = transitionTypeSt + " : " + TextUtils.join(", ", geofencesId);
            sendNotification(notificationST);

        } else {
            stopSelf();
        }

    }

    private void sendNotification(String notificationST) {

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setContentTitle(notificationST);
        builder.setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "geofencing";
            String description = "area";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            manager.createNotificationChannel(channel);
        }

        manager.notify(112, builder.build());

    }


}
