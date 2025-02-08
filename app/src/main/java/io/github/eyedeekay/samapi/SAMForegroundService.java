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

import android.os.AsyncTask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import net.i2p.sam.SAMBridge;
import net.i2p.router.Router;
import net.i2p.app.ClientAppState;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Objects;

public class SAMForegroundService extends Service {
    public static final String CHANNEL_ID = "SAMSAMForegroundServiceChannel";
    public Router router = new Router();
    public SAMBridge SAM_BRIDGE;
    public static String[] DefaultArgs = new String[] { "sam.keys", "127.0.0.1", "7656", "i2cp.tcp.host=127.0.0.1",
            "i2cp.tcp.port=7654" };

    public SAMForegroundService() {
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

    public boolean startSAMBridge() {
        if (SAM_BRIDGE == null) {
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
                return false;
            }
        }
        if (SAM_BRIDGE.getState() == ClientAppState.RUNNING) {
            return true;
        } else {
            String[] args = new String[] {
                    "sam.keys",
                    "127.0.0.1",
                    "7656",
                    // "7656",
                    // "i2cp.tcp.host=127.0.0.1",
                    // "i2cp.tcp.port=7654",
            };
            router.getContext().routerAppManager().addAndStart(SAM_BRIDGE, args);
            router.getContext().routerAppManager().register(SAM_BRIDGE);
            return true;
        }
    }

    public Properties getRouterProperties() throws IOException {
        Properties router_properties = new Properties();
        router_properties.setProperty("i2p.dir.base", getFilesDir().getAbsolutePath());
        // disable router console and all apps except for i2cp
        return router_properties;
    }

    public boolean startRouter() {
        try {
            if (router == null) {
                router = new Router();
                router.setKillVMOnEnd(false);
                router.setUPnPScannerCallback(new SSDPLocker(SAMForegroundService.this));
                router.runRouter();
                Log.i("SAMForegroundService", "I2P Router started successfully");
                return true;
            } else {
                if (router.isRunning()) {
                    Log.i("SAMForegroundService", "I2P Router is already running");
                } else {
                    router.runRouter();
                    Log.i("SAMForegroundService", "I2P Router started successfully");
                }
                return true;
            }
        } catch (Exception e) {
            Log.e("SAMForegroundService", "Failed to start I2P Router", e);
        }
        return false;
    }

    public boolean stopSAMBridge() {
        router.getContext().routerAppManager().unregister(SAM_BRIDGE);
        if (SAM_BRIDGE == null) {
            return false;
        }
        SAM_BRIDGE.shutdown(DefaultArgs);

        return true;
    }

    public boolean stopRouter() {
        if (router == null) {
            return false;
        }
        router.shutdownGracefully();
        return true;
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
        // sam_properties.setProperty("i2cp.tcp.host", "127.0.0.1");
        // sam_properties.setProperty("i2cp.tcp.port", "7654");
        return sam_properties;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("SAMForegroundService", "onCreate");
    }

    public int getDrawableResource(String resourceName) {
        int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
        if (resId == 0) {
            Log.w("SAMForegroundService", "Drawable resource not found: " + resourceName);
            resId = android.R.drawable.ic_dialog_alert; // fallback resource
        }
        return resId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new StartupTask().execute(intent);
        return START_NOT_STICKY;
    }

    private class StartupTask extends AsyncTask<Intent, Void, Void> {
        @Override
        protected Void doInBackground(Intent... intents) {
            Intent intent = intents[0];
            if (Objects.equals(intent.getAction(), "STARTFOREGROUND_ACTION")) {
                Log.i("SAMForegroundService", "Received Start Foreground Intent ");
                if (startRouter()) {
                    Log.i("SAMForegroundService", "I2P Router started successfully");
                } else {
                    Log.e("SAMForegroundService", "Failed to start I2P Router");
                }
                if (startSAMBridge()) {
                    Log.i("SAMForegroundService", "SAM Bridge started successfully");
                } else {
                    Log.e("SAMForegroundService", "Failed to start SAM Bridge");
                }
                startForeground(1, createNotification(intent));
            } else if (Objects.equals(intent.getAction(), "STOPFOREGROUND_ACTION")) {
                Log.i("SAMForegroundService", "Received Stop Foreground Intent");
                if (stopSAMBridge()) {
                    Log.i("SAMForegroundService", "SAM Bridge stopped successfully");
                } else {
                    Log.e("SAMForegroundService", "Failed to stop SAM Bridge");
                }
                if (stopRouter()) {
                    Log.i("SAMForegroundService", "I2P Router stopped successfully");
                } else {
                    Log.e("SAMForegroundService", "Failed to stop I2P Router");
                }
            }
            return null;
        }
    }

    private Notification createNotification(Intent intent) {
        PendingIntent newActivity;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            newActivity = PendingIntent.getActivity(this, 0, new Intent(this, SAMService.class),
                    PendingIntent.FLAG_IMMUTABLE);
        } else {
            newActivity = PendingIntent.getActivity(this, 0, new Intent(this, SAMService.class), 0);
        }

        String input = intent.getStringExtra("inputExtra");
        if (input != null) {
            Log.v("SAMSAMForegroundService", input);
        } else {
            Log.v("SAMSAMForegroundService", "No inputExtra provided");
        }
        Intent serviceIntent = new Intent(this, SAMForegroundService.class);
        serviceIntent.setAction("STOPFOREGROUND_ACTION");
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, serviceIntent, PendingIntent.FLAG_IMMUTABLE);

        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SAM API Service")
                .setContentText("SAM API Service is running")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_dialog_alert))
                .setWhen(System.currentTimeMillis())
                .addAction(getDrawableResource("ic_launcher_foreground"), "Stop", pStopSelf)
                .addAction(getDrawableResourceId("ic_launcher_foreground"), "NewActivity", newActivity)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (router != null) {
            router.shutdownGracefully();
            router = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int getDrawableResourceId(String resourceName) {
        int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
        if (resId == 0) {
            Log.w("SAMForegroundService", "Drawable resource not found: " + resourceName);
            resId = android.R.drawable.ic_dialog_alert; // fallback resource
        }
        return resId;
    }

    /**
     * Creates a notification channel for the foreground service.
     * This is required for Android O and above to display notifications.
     */
    private void createNotificationChannel() {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel serviceChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "Foreground Service Channel",
                        NotificationManager.IMPORTANCE_DEFAULT);

                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}