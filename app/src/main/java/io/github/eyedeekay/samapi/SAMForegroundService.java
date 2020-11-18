package io.github.eyedeekay.samapi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import net.i2p.sam.SAMBridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class SAMForegroundService extends Service {
    public static final String CHANNEL_ID = "SAMSAMForegroundServiceChannel";
    public SAMBridge SAM_BRIDGE;
    public static String[] DefaultArgs = new String[]{"sam.keys", "127.0.0.1", "7656", "i2cp.tcp.host=127.0.0.1", "i2cp.tcp.port=7654"};

    /*{
        try {
            SAM_BRIDGE = new SAMBridge("127.0.0.1",
                    7656,
                    false,
                    SAM_PROPERTIES(),
                    "sam.keys",
                    new File("sam_config"));
        } catch (IOException e) {
            Log.e("Foreground", e.toString());
            e.printStackTrace();
        }
    }*/
    public SAMForegroundService(){
        try {
            SAM_BRIDGE = new SAMBridge("127.0.0.1",
                    7656,
                    false,
                    SAM_PROPERTIES(),
                    "sam.keys",
                    new File("sam_config"));
        } catch (IOException e) {
            Log.e("Foreground", e.toString());
            e.printStackTrace();
        }
    }

    public SAMForegroundService(SAMBridge sam_bridge) {
        SAM_BRIDGE = sam_bridge;
    }

    public Uri resToURI(int resourceId) {
        Resources resources = getResources();
        Log.i("Foreground", "Getting the default properties");
        Uri uri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resourceId))
                .appendPath(resources.getResourceTypeName(resourceId))
                .appendPath(resources.getResourceEntryName(resourceId))
                .build();
        return uri;
    }

    public Properties SAM_PROPERTIES() throws IOException {
        Log.i("Foreground", "Getting the default properties");
        Properties sam_properties = new Properties();
        //int resource = getResources().getIdentifier("sam_config", "id", getPackageName());
        sam_properties.setProperty("i2cp.tcp.host","127.0.0.1");
        sam_properties.setProperty("i2cp.tcp.port","7654");
        //in.close();
        return sam_properties;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.v("SAMForegroundService","onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        PendingIntent newActivity = PendingIntent.getActivity(this, 0, new Intent(this, SAMService.class), 0);

        Intent serviceIntent = new Intent(this, SAMForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Stop Foreground Service in Android");
        serviceIntent.setAction("STOPFOREGROUND_ACTION");
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        String input = intent.getStringExtra("inputExtra");
        Log.v("SAMSAMForegroundService" , input);

        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SAM API Service")
                .setContentText("SAM API Service is running")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),android.R.drawable.ic_dialog_alert))
                .setWhen(System.currentTimeMillis())
                .addAction(R.drawable.ic_launcher_foreground, "Stop", pStopSelf)
                .addAction(R.drawable.ic_launcher_foreground, "NewActivity", newActivity)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();


        if (intent.getAction().equals( "STARTFOREGROUND_ACTION")) {
            Log.i("SAMForegroundService", "Received Start Foreground Intent ");
            try {
                SAM_BRIDGE.startup();
            }catch(IOException ioe){
                Log.e("ForegroundService", ioe.toString());
            }
            startForeground(1, notification);

        }
        else if (intent.getAction().equals( "STOPFOREGROUND_ACTION")) {
            Log.i("SAMForegroundService", "Received Stop Foreground Intent");
            //your end servce code
            String args[] = new String[]{""};
            SAM_BRIDGE.shutdown(args);
            stopForeground(true);
            stopSelfResult(startId);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
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