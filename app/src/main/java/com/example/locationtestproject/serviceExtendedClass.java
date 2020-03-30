package com.example.locationtestproject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

public class serviceExtendedClass extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private MessageListener messageListener;
    private Message message;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastLoc;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void aToast(String msg){
        Toast t = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        t.show();
    }

    public void playTone(){
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
        mp.start();
    }


    @SuppressLint("MissingPermission")
    public void updateLastLoc(){
        lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Info", "Got to service!!");

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Social Distancing Alerts")
                .setContentText("A notification sound will play if any devices are detected at an unsafe distance")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);


        Toast.makeText(this, "Hi there", Toast.LENGTH_SHORT).show();

        messageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                String strMessage = message.toString();
                String[] coords = strMessage.split("\\s+");
                Location otherLoc = new Location(LocationManager.GPS_PROVIDER);
                updateLastLoc();

                double lat = Double.parseDouble(coords[0]);
                double lon = Double.parseDouble(coords[1]);

                otherLoc.setLatitude(Double.parseDouble(coords[0]));
                otherLoc.setLongitude(Double.parseDouble(coords[1]));

                double gap = lastLoc.distanceTo(otherLoc);
                aToast("Distance of " + gap);
            }

            @Override
            public void onLost(Message message) {
            }
        };
        message = new Message("Hello world".getBytes());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("Service", "exist aici");
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        Nearby.getMessagesClient(this).publish(message);
        Nearby.getMessagesClient(this).subscribe(messageListener);

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Inregistrare persoane din apropiere")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Nearby.getMessagesClient(this).unpublish(message);
        Nearby.getMessagesClient(this).unsubscribe(messageListener);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}
