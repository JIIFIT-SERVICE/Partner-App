package com.company.jifiit;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.notification.NotificationActionReceiver;
import com.company.jifiit.notification.NotificationsActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "jifiit_channel";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "🔹 FCM Message Received");

        String title = "";
        String body = "";
        String type = "default";
        String sound = "notification_tone";
        boolean showOkButton = false;
        String notificationId = String.valueOf(System.currentTimeMillis());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "📩 Data Payload: " + remoteMessage.getData().toString());

            // Extract title and body from the data payload
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                type = remoteMessage.getData().getOrDefault("type", "default");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sound = remoteMessage.getData().getOrDefault("sound", "notification_tone");
            }
            showOkButton = "true".equals(remoteMessage.getData().get("showMarkAsReadButton"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                notificationId = remoteMessage.getData().getOrDefault("notification_id", String.valueOf(System.currentTimeMillis()));
            }

            Log.d(TAG, "🔔 Notification Received - Title: " + title + ", Body: " + body);

            showNotification(title, body, type, sound, showOkButton, notificationId);
            if ("new_order".equals(type)){
                updateNotificationTimeOnServer(notificationId);
            }
        }
    }



    @SuppressLint("MissingPermission")
    private void showNotification(String title, String message, String type, String sound, boolean showOkButton, String notificationId) {
        Uri soundUri = ("weekly_off".equals(type)) ? null : getSoundUri(sound, type); // No sound for weekly off
        createNotificationChannel(type, soundUri);

        String channelId = "default_channel";
        if ("new_order".equals(type) || "reminder".equals(type)) {
            channelId = "new_order_channel";
        } else if ("weekly_off".equals(type)) {
            channelId = "weekly_off_channel";
        } else if ("Wallet".equals(type)) {
            channelId = "wallet_channel";
        }else if ("leave".equals(type)) {
            channelId = "leave_channel";
        }

        // Main notification intent
        Intent notificationIntent = new Intent(this, NotificationsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action button intent
        PendingIntent okPendingIntent = null;
        if (showOkButton) {
            Intent okIntent = new Intent(this, NotificationActionReceiver.class);
            okIntent.setAction("ACTION_OK");
            okIntent.putExtra("notification_id", notificationId);
            okPendingIntent = PendingIntent.getBroadcast(
                    this, 1, okIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (showOkButton && okPendingIntent != null) {
            builder.addAction(R.drawable.ic_check, "OK", okPendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(Integer.parseInt(notificationId), builder.build());
        }

        Log.d(TAG, "🔔 Notification Shown - Type: " + type + ", Title: " + title);
    }



    private Uri getSoundUri(String soundName, String type) {
        if (soundName.endsWith(".mp3")) {
            soundName = soundName.substring(0, soundName.lastIndexOf("."));
        }

        int resId = getResources().getIdentifier(soundName, "raw", getPackageName());
        if (resId != 0 && (type.equals("new_order") || type.equals("reminder"))) {
            Log.d(TAG, "✅ Custom Sound Found: " + soundName);
            return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + resId);
        }

        Log.d(TAG, "⚠️ Sound file not found, using default sound.");
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }


    private void createNotificationChannel(String type, Uri soundUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            String channelId;
            String channelName;
            int importance;
            boolean enableVibration = true;

            if ("new_order".equals(type) || type.equals("reminder")) {
                channelId = "new_order_channel";
                channelName = "New Order Notifications";
                importance = NotificationManager.IMPORTANCE_HIGH;
            }else if ("weekly_off".equals(type)) {
                channelId = "weekly_off_channel";
                channelName = "Weekly Off Notifications";
                importance = NotificationManager.IMPORTANCE_HIGH; // Set HIGH priority
                enableVibration = true; // Enable vibration
                soundUri = Settings.System.DEFAULT_NOTIFICATION_URI; // Use default notification sound
            } else if ("Wallet".equals(type)) {
                channelId = "wallet_channel";
                channelName = "Wallet Notifications";
                importance = NotificationManager.IMPORTANCE_HIGH; // Set HIGH priority
                enableVibration = true; // Enable vibration
                soundUri = Settings.System.DEFAULT_NOTIFICATION_URI; // Use default notification sound
            }
            else if ("leave".equals(type)) {
                channelId = "leave_channel";
                channelName = "Leave Notifications";
                importance = NotificationManager.IMPORTANCE_HIGH; // Set HIGH priority
                enableVibration = true; // Enable vibration
                soundUri = Settings.System.DEFAULT_NOTIFICATION_URI; // Use default notification sound
            }else {
                channelId = "default_channel";
                channelName = "General Notifications";
                importance = NotificationManager.IMPORTANCE_DEFAULT;
            }

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription("Channel for " + type);

            if (soundUri != null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                channel.setSound(soundUri, audioAttributes);
            } else {
                channel.setSound(null, null); // No sound for weekly off
            }

            channel.enableLights(true);
            channel.enableVibration(enableVibration);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    private void updateNotificationTimeOnServer(String notificationId) {
        String url = Constant.update_notification_time;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d("FCM", "Notification time updated successfully on order Id: " + notificationId),
                error -> Log.e("FCM", "Failed to update notification time", error)) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("order_id", notificationId); // Send as a string to avoid precision loss
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

}
