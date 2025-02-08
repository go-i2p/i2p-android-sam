package io.github.eyedeekay.samapi;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import net.i2p.sam.SAMBridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     * This is where you should do all of your normal static set up: create views, bind data to lists, etc.
     * This method also provides you with a Bundle containing the activity's previously frozen state, if there was one.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /**
     * Starts the foreground service.
     */
    public void startService(View view) {
        Log.i("main","starting");
        Intent serviceIntent = new Intent(this, SAMForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Start Foreground Service in Android");
        startService(serviceIntent);
    }

    /**
     * Stops the foreground service.
     */
    public void stopService(View view) {
        Log.i("main","stopping");
        Intent serviceIntent = new Intent(this, SAMForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Stop Foreground Service in Android");
        serviceIntent.setAction("STOPFOREGROUND_ACTION");
        stopService(serviceIntent);
    }
}