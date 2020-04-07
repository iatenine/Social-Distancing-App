package com.example.locationtestproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class serviceExtendedClass extends Service {
    public static final String CHANNEL_ID = "COVID SD";
    public static final int NOT_ID = 6450;
    private static final String TAG = "Service Logger";

    private Notification notification;
    private NotificationChannel notificationChannel;


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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationChannel = new NotificationChannel(CHANNEL_ID, "SD Scanner", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("Broadcasting location and scanning for nearby messages");
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(notificationChannel);
        }

        notification = new androidx.core.app.NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                .setContentTitle("Social Distancing Scanner")
                .setContentText("You are currently broadcasting your message")
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
