package com.example.locationtestproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Logger";
    private static final String STANDARD = "STD-COVID-SD";

    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    MediaPlayer mp;

    private MessageListener messageListener;
    private Message message;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastLoc;
    private Location nearestLoc;

    final float THRESHOLD = 3;  //Meters at which alarm should begin playing
    final int FREQUENT_UDPATES = 1000;
    final int BATTERY_SAVER = 5000;
    Intent i;
    boolean broadcasting = false;
    boolean boolCautious = true;

    //Lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mp = MediaPlayer.create(getApplicationContext(), notification);
        i = new Intent(getApplicationContext(), serviceExtendedClass.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpNearby();
        setUpGPS();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Nearby.getMessagesClient(this).unpublish(message);
        Nearby.getMessagesClient(this).unsubscribe(messageListener);
    }

    private String constructLocString(Location l) {
        String ret = STANDARD + " " + String.valueOf(l.getLatitude()) + " " + String.valueOf(l.getLongitude());
        return ret;
    }

    //UI Functions
    public void startBroadcast(View view) {
        broadcasting = true;
        ContextCompat.startForegroundService(getApplicationContext(), i);
    }

    public void stopBroadcast(View view) {
        broadcasting = false;
        stopService(i);
    }

    public void updateAgreement(View view) {
        Button button = findViewById(R.id.btnBroadcastBegin);
        CheckBox cb = findViewById(R.id.checkBoxAgreement);
        button.setEnabled(cb.isChecked());
    }

    public void flipCautious(View view) {
        Switch cautiousSwtich = findViewById(R.id.switchConserve);
        boolCautious = cautiousSwtich.isChecked();
        locUpdateRequest();
    }

    //Math/parsing functions
    private boolean isValidMsg(String[] s) {
        if (s.length == 3 && s[0].compareTo(STANDARD) == 0)
            return true;
        else
            return false;
    }

    public double getDistToMsg(Message message) {
        String[] coords = message.toString().split("\\s+");
        if (isValidMsg(coords) == false)
            return 999999;
        Location otherLoc = new Location(LocationManager.GPS_PROVIDER);
        //Lat + Lon
        otherLoc.setLatitude(Double.parseDouble(coords[1]));
        otherLoc.setLongitude(Double.parseDouble(coords[2]));

        return lastLoc.distanceTo(otherLoc);
    }

    public void broadcastMessage() {
        if (broadcasting == true)
            Nearby.getMessagesClient(this).publish(message);
    }

    //Override functions
    public void setUpNearby() {
        Log.d(TAG, "setUpNearby: Called");
        message = new Message("Init".getBytes());
        messageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                String msg_str = new String(message.getContent());
                Log.d(TAG, "Found message: " + new String(message.getContent()));
                if (isValidMsg(msg_str.split("\\s+")) == true) {
                    if (getDistToMsg(message) > THRESHOLD) {
                        playTone();
                        Log.d(TAG, "onFound: Too close!");
                    } else {
                        Log.d(TAG, "onFound: Far enough");
                        stopTone();
                    }
                } else
                    Log.d(TAG, "onFound: Invalid message");
            }

            @Override
            public void onLost(Message message) {
                Log.d(TAG, "Lost sight of message: " + new String(message.getContent()));
                stopTone();
            }

        };

        Nearby.getMessagesClient(this).subscribe(messageListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Not sure what to do with this...
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public void setUpGPS() {
        Log.d(TAG, "setUpGPS: Called");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLoc != null)
            message = new Message(constructLocString(lastLoc).getBytes());
        Log.d(TAG, "Initial Location: " + new String(message.getContent()));

        broadcastMessage();


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: Publishing location");
                lastLoc = location;
                message = new Message(constructLocString(location).getBytes());
                broadcastMessage();
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

        locUpdateRequest();
    }

    public void locUpdateRequest() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        if(boolCautious == true)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FREQUENT_UDPATES, (float) 0.2, locationListener);
        else
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, BATTERY_SAVER, (float) 0.2, locationListener);
    }

    public void playTone() {
        mp.setLooping(true);
        mp.start();
    }

    public void stopTone(){
        mp.stop();
    }

}
