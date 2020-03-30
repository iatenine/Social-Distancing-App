package com.example.locationtestproject;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.nearby.messages.Message;

public class MainActivity extends AppCompatActivity {

    final int FREQUENT_UDPATES = 1000;
    final int BATTERY_SAVER = 5000;
    Boolean boolCautious = true;

    Activity masterClass = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
    * UI Functions
    *
    * */
    public void startBroadcast(View view){
        Intent i = new Intent(getApplicationContext(), serviceExtendedClass.class);

        //TODO
        //GET THESE TO WORK!!!!
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(i);
        }
        else{
            startService(i);
        }
    }

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

    public void aToast(String msg){
        Toast t = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        t.show();
    }

    /*
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

    public void startTracking(){
        updateLastLoc();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, (float) 0.5, locationListener);
    }

    public void updateLastLoc(){
        lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private void publish(String message) {
        mActiveMessage = new Message(message.getBytes());
        Nearby.getMessagesClient(this).publish(mActiveMessage);
    }
     */
}
