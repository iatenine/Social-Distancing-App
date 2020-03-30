package com.example.locationtestproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.security.Permission;

public class MainActivity extends AppCompatActivity {

    final int FREQUENT_UDPATES = 1000;
    final int BATTERY_SAVER = 5000;
    Boolean boolCautious = true;
    Location lastLoc;
    LocationManager locationManager;
    Message mActiveMessage;

    Activity masterClass = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void buildNotification(){
        Notification n = new Notification();

    }

    MessageListener mMessageListener = new MessageListener(){
        @Override
        public void onFound(Message message){
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
    };

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            String lat = String.valueOf(location.getLatitude());
            String lon = String.valueOf(location.getLongitude());

            String comb = lat + " " + lon;
            String[] gps_coords = comb.trim().split("\\s+");

            Location newLoc = new Location(LocationManager.GPS_PROVIDER);
            newLoc.setLatitude(Double.parseDouble(gps_coords[0]));
            newLoc.setLongitude(Double.parseDouble(gps_coords[1]));
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

    @Override
    protected void onStop() {
        super.onStop();
        Nearby.getMessagesClient(this).unpublish(mActiveMessage);
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
    }

    //Enables or disable broadcasting based on user's agreement
    public void updateAgreement(View view){
        Button button = findViewById(R.id.btnBroadcastBegin);
        CheckBox cb = findViewById(R.id.checkBoxAgreement);
        button.setEnabled(cb.isChecked());
    }

    public void flipCautious(View view){
        Switch cautiousSwtich = findViewById(R.id.switchConserve);
        boolCautious = cautiousSwtich.isChecked();
        aToast("Cautious = " + boolCautious);
    }

    public void playTone(){
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
        mp.start();
    }

    public void aToast(String msg){
        Toast t = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        t.show();
    }

    public void requestTracking(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_main);
                locationManager =(LocationManager) getSystemService(LOCATION_SERVICE);

                if (ActivityCompat.checkSelfPermission(masterClass, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    if (ActivityCompat.checkSelfPermission(masterClass, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(masterClass, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 123);
                        startTracking();
                        return;
                    }
                    else{

                    }

                startTracking();
            }
        };

        r.run();
    }

    @SuppressLint("MissingPermission")
    public void startTracking(){
        aToast("Tracking begins");
        updateLastLoc();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, (float) 0.5, locationListener);
    }

    @SuppressLint("MissingPermission")
    public void updateLastLoc(){
        lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private void publish(String message) {
        mActiveMessage = new Message(message.getBytes());
        Nearby.getMessagesClient(this).publish(mActiveMessage);
    }

}
