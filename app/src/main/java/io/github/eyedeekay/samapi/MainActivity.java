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

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startService(View view) {
        Log.i("main","starting");
        Intent serviceIntent = new Intent(this, SAMForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Start Foreground Service in Android");
        //方法一
        serviceIntent.setAction("STARTFOREGROUND_ACTION");
        startService(serviceIntent);
        //方法二
        // startService(serviceIntent);
    }

    public void stopService(View view) {
        Log.i("main","stopping");
        Intent serviceIntent = new Intent(this, SAMForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Stop Foreground Service in Android");
        //方法一
        serviceIntent.setAction("STOPFOREGROUND_ACTION");
        startService(serviceIntent);
        //方法二
        //stopService(serviceIntent);
    }
}