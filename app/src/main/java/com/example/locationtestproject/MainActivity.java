package com.example.locationtestproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    final int FREQUENT_UDPATES = 1000;
    final int BATTERY_SAVER = 5000;
    Intent i;
    Boolean boolCautious = true;

    Activity masterClass = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        i = new Intent(getApplicationContext(), serviceExtendedClass.class);
    }

    /*
    * UI Functions
    *
    * */
    public void startBroadcast(View view){
        ContextCompat.startForegroundService(getApplicationContext(), i);
    }

    public void stopBroadcast(View view){
        stopService(i);
    }

    public void updateAgreement(View view){
        Button button = findViewById(R.id.btnBroadcastBegin);
        CheckBox cb = findViewById(R.id.checkBoxAgreement);
        button.setEnabled(cb.isChecked());
    }

    public void flipCautious(View view){
        Switch cautiousSwtich = findViewById(R.id.switchConserve);
        boolCautious = cautiousSwtich.isChecked();
    }
}
