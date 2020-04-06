package com.example.locationtestproject;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class serviceExtendedClass extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final int NOT_ID = 6450;
    private static final String TAG = "Service Logger";

    private Notification notification;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "serviceExtendedClass: onCreate() called");

        createNotificationChannel();
        startForeground(NOT_ID, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "serviceExtendedClass: onStartCommand() called");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "serviceExtendedClass: onDestroy() called");
        stopSelf();
    }

    private void createNotificationChannel() {
        Log.d(TAG, "serviceExtendedClass: createNotificationChannel() called");

        Intent serviceIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, serviceIntent, 0);


        notification = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                .setContentTitle("Social Distancing Scanner")
                .setContentText("You are currently broadcasting your message. We will play an alert sound if you come with an unsafe threshold of another person running another compatible social distancing app")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
    }

    /*
    public void playTone() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
        mp.start();
    }

     */
    /*
    //Returns distance in meters between current loc and provide lat/lon combo
    public double getGapFromMessage (Message message) {
        String[] coords = message.toString().split("\\s+");
        Location otherLoc = new Location(LocationManager.GPS_PROVIDER);
        updateLastLoc();    //Get most recent loc

        //Lat + Lon
        otherLoc.setLatitude(Double.parseDouble(coords[0]));
        otherLoc.setLongitude(Double.parseDouble(coords[1]));

        return lastLoc.distanceTo(otherLoc);
    }

     */

}
