package com.example.locationtestproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcel;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.security.Permission;

public class MainActivity extends AppCompatActivity {

    Location lastLoc;
    LocationManager locationManager;
    Message mActiveMessage;


    MessageListener mMessageListener = new MessageListener(){
        @Override
        public void onFound(Message message){
            //String strMessage = message.toString();
            //String[] coords = strMessage.split(" ");

            //double lat = Double.parseDouble(coords[0]);
            //double lon = Double.parseDouble(coords[1]);

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

            Boolean test = location.getLongitude() == newLoc.getLongitude();
            Boolean test1 = location.getLatitude() == newLoc.getLatitude();

            if (test == true && test1 == true){
                aToast("Resounding success");
            }
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager =(LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 123);
                startTracking();
                return;
            }
        else{

            }

        startTracking();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Nearby.getMessagesClient(this).unpublish(mActiveMessage);
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
    }

    public void aToast(String msg){
        Toast t = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        t.show();
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
