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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Logger";
    private static final String STANDARD = "STD-COVID-SD";

    MediaPlayer alertPlayer;
    MediaPlayer clearPlayer;

    private MessageListener messageListener;
    private Message message;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastLoc;
    private Location nearestLoc;

    final float THRESHOLD = (float) 15;  //Meters at which alarm should begin playing (roughly 12m margin of error)
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
        i = new Intent(getApplicationContext(), serviceExtendedClass.class);
        alertPlayer  = MediaPlayer.create(getApplicationContext(), R.raw.alert_tone);
        clearPlayer = MediaPlayer.create(getApplicationContext(), R.raw.clear_tone);

        nearestLoc = getNullLoc();
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
        return STANDARD + " " + l.getLatitude() + " " + l.getLongitude();
    }

    private Location parseLocFromMessage(Message m){
        String msg_str = new String(m.getContent());
        String[] s = msg_str.split("\\s+");

        Log.d(TAG, "parseLocFromMessage: " + msg_str);
        String[] coords = msg_str.split("\\s+");

        Location thisLoc = getNullLoc();

        if (isValidMsg(m)) {
            Log.d(TAG, "parseLocFromMessage: Str recognized as valid");
            thisLoc.setLatitude(Double.parseDouble(coords[1]));
            thisLoc.setLongitude(Double.parseDouble(coords[2]));
        }

        return thisLoc;
    }

    private Location getNullLoc(){
        Location ret = new Location(LocationManager.GPS_PROVIDER);
        ret.setLatitude(0);
        ret.setLongitude(0);
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
    private boolean isValidMsg(Message msg) {
        String msg_str = new String(msg.getContent());
        String[] s = msg_str.split("\\s+");
        return s[0].compareTo(STANDARD) == 0;
    }

    public double getDistToMsg(Message m) {
        return lastLoc.distanceTo(parseLocFromMessage(m));
    }

    public void broadcastMessage() {
        if (broadcasting)
            Nearby.getMessagesClient(this).publish(message);
    }

    public void removeLoc(Message m){
        Location queryLoc = parseLocFromMessage(m);
        if(nearestLoc == queryLoc)
            nearestLoc = getNullLoc();
    }

    public void updateNearestLoc(Message msg){
        Log.d(TAG, "updateNearestLoc: Nearest loc may become: " + new String(msg.getContent()));
        if (getDistToMsg(msg) < lastLoc.distanceTo(nearestLoc)){
            Log.d(TAG, "updateNearestLoc: Nearest loc accepted");
                nearestLoc = parseLocFromMessage(msg);
        }
        Log.d(TAG, "updateNearestLoc: New nearest loc = " + nearestLoc.toString());
        }


    public void updateAlerts(){
        Log.d(TAG, "updateAlerts: Threshold: " + THRESHOLD);
        Log.d(TAG, "updateAlerts: CurrLoc " + lastLoc.toString());
        Log.d(TAG, "updateAlerts: nearLoc " + nearestLoc.toString());
        Log.d(TAG, "updateAlerts: distanceto: " + lastLoc.distanceTo(nearestLoc));
            if (lastLoc.distanceTo(nearestLoc) <= THRESHOLD) {
                Log.d(TAG, "updateAlerts: Tone should play");
                playAlertTone();
            } else {
                Log.d(TAG, "updateAlerts: Tone should stop");
                playClearTone();
            }
        }


    //Override functions
    public void setUpNearby() {
        Log.d(TAG, "setUpNearby: Called");
        message = new Message("Init".getBytes());
        messageListener = new MessageListener() {
            @Override
            public void onFound(Message m) {
                Log.d(TAG, "Found message: " + new String(m.getContent()));
                updateNearestLoc(m);
                updateAlerts();
            }

            @Override
            public void onLost(Message m) {
                Log.d(TAG, "Lost sight of message: " + new String(m.getContent()));
                removeLoc(m);
                updateAlerts();
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
        if(boolCautious)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FREQUENT_UDPATES, (float) 0.2, locationListener);
        else
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, BATTERY_SAVER, (float) 0.2, locationListener);
    }

    public void playAlertTone() {
        alertPlayer.start();
        aToast(R.string.nearby_alert);
    }

    public void playClearTone(){
        clearPlayer.start();
        aToast(R.string.clear_notice);
    }

    public void aToast(int resID){
        Toast toast = Toast.makeText(this, resID, Toast.LENGTH_LONG);
        toast.show();
    }

}
