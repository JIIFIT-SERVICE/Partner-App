package com.company.jifiit.notification;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationActionReceiver";

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("ACTION_OK".equals(intent.getAction())) {
            String notificationId = intent.getStringExtra("notification_id");

            Log.d(TAG, "✅ Notification " + notificationId + " marked as read.");

            // ✅ Send API request to update the server
            markNotificationAsRead(context, notificationId);
        }
    }

    @SuppressLint("LongLogTag")
    private void markNotificationAsRead(Context context, String notificationId) {
        String url = Constant.notificationUpdate; // ✅ Replace with your actual API URL

        Log.d(TAG, "📤 Sending request to mark notification " + notificationId + " as read...");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "✅ Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.optString("status");

                        if ("success".equals(status)) {
                            Log.d(TAG, "🎉 Notification marked as read successfully!");

                            // ✅ Remove notification from tray only after successful server update
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                            notificationManager.cancel(Integer.parseInt(notificationId));
                            Log.d(TAG, "✅ Notification " + notificationId + " removed from tray.");
                        } else {
                            Log.e(TAG, "❌ Server error: " + jsonObject.optString("message"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "❌ JSON Parsing error", e);
                        Log.e(TAG, "❌ Invalid JSON response: " + response);
                    }
                },
                error -> Log.e(TAG, "❌ Volley Error: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("notification_id", notificationId); // ✅ Ensure backend expects this key
                return params;
            }
        };

        // ✅ Use context to initialize RequestQueue properly
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
}