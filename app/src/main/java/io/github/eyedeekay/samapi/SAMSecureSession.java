package net.i2p.android.router.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import net.i2p.router.Router;
import net.i2p.router.RouterContext;
import net.i2p.sam.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Implements SAMSecureSessionInterface on Android platforms using a Toast
 * as the interactive channel.
 *
 * @since 1.8.0
 */
public class SAMSecureSession implements SAMSecureSessionInterface {
    private static final Map<String, Boolean> results = new ConcurrentHashMap<>();
    private final Context mCtx;
    private final Random rng = new Random();
    private static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static void affirmResult(String clientId) {
        // Util.d("Affirmed result for: " + clientId);

        results.put(clientId, true);
    }

    public static boolean denyResult(String clientId) {
        // Util.d("Denied result for: " + clientId);

        results.put(clientId, false);
        return false;
    }

    public static boolean checkResult(String clientId) {
        // while result is null, wait
        while (results.get(clientId) == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return results.get(clientId);
    }

    public SAMSecureSession(Context ctx) {
        mCtx = ctx;
    }

    public String generateString() {
        int length = rng.nextInt(6) + 10;
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

    public String getID(Properties i2cpProps, Properties props) {
        String ID = props.getProperty("USER");
        if (ID == null)
            ID = i2cpProps.getProperty("inbound.nickname");
        if (ID == null)
            ID = i2cpProps.getProperty("outbound.nickname");
        if (ID == null)
            ID = props.getProperty("ID");
        if (ID == null) {
            MessageDigest messageDigest = null;
            try {
                messageDigest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                ID = "No_ID_Present";
            }
            if (messageDigest != null) {
                String combinedProps = i2cpProps.toString() + props.toString();
                messageDigest.update(combinedProps.getBytes());
                ID = messageDigest.digest().toString();
            } else {
                ID = "No_ID_Present" + generateString();
            }
        }
        return ID;
    }

    private void deleteAllDeniedSessions() {
        for (String key : results.keySet()) {
            if (results.get(key) == false) {
                results.remove(key);
            }
        }
    }

    @Override
    public boolean approveOrDenySecureSession(Properties i2cpProps, Properties props) throws SAMException {
        deleteAllDeniedSessions();
        final String ID = getID(i2cpProps, props);
        // if already approved, return true
        if (results.get(ID) != null)
            if (results.get(ID) == true)
                return true;

        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setTitle("Secure Session Request");
        builder.setMessage("A secure session request has been received from " + ID + ". Do you want to accept it?");
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                affirmResult(ID);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                denyResult(ID);
                dialog.dismiss();
            }
        });
        return checkResult(ID);
    }
}
