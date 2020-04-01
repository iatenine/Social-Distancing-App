package com.example.locationtestproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

public class serviceExtendedClass extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final int NOT_ID = 6450;
    private static final String TAG = "Service Logger";

    private MessageListener messageListener;
    private Message message;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastLoc;

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

        messageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.d(TAG, "onFound: Found message: " + message.toString());
            }

            @Override
            public void onLost(Message message) {
            }
        };


        Nearby.getMessagesClient(getBaseContext()).subscribe(messageListener);
        requestTracking();
        updateLastLoc();
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
        Nearby.getMessagesClient(this).unpublish(message);
        Nearby.getMessagesClient(this).unsubscribe(messageListener);
        stopSelf();
    }

    private void createNotificationChannel() {
        Log.d(TAG, "serviceExtendedClass: createNotificationChannel() called");

        Intent serviceIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, serviceIntent, 0);


        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Social Distancing Scanner")
                .setContentText("Text herejrklajflk ")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
    }

    public void playTone() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
        mp.start();
    }


    public void updateLastLoc() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    public void requestTracking() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d(TAG, "onLocationChanged: Location has changed");
                        message = new Message("Hi there".getBytes());
                        Nearby.getMessagesClient(getBaseContext()).publish(message);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                };

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, (float) 0.5, locationListener);
            }
        };

        r.run();
    }

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

}
