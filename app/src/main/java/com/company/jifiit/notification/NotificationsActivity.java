package com.company.jifiit.notification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.BookingAdapter;
import com.company.jifiit.Adapter.NotificationAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.model.BookingModel;
import com.company.jifiit.model.NotificationModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsActivity extends AppCompatActivity {

    private List<NotificationModel> notificationList;
    private NotificationAdapter adapter;
    BigInteger Id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notifications");
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String name = sp.getString("Name", "Default Name");
        String idString = sp.getString("Id", "");
         Id = new BigInteger(idString);
        fetchNotifications(Id);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>(); // Initialize an empty list
        adapter = new NotificationAdapter(notificationList, this);
        recyclerView.setAdapter(adapter);
    }


    private void fetchNotifications(BigInteger id) {
        // Check for network connectivity
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construct API URL to fetch notifications
        String url = Constant.Notificationfetch + id;

        // Show a loading toast (Optional)
        // Toast.makeText(this, "Loading notifications...", Toast.LENGTH_SHORT).show();

        // Create a Volley request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Define the request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String status = response.getString("Status");
                        if ("success".equalsIgnoreCase(status)) {
                            JSONArray dataArray = response.optJSONArray("notifications");
                            if (dataArray != null && dataArray.length() > 0) {
                                List<NotificationModel> newNotifications = new ArrayList<>();

                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject notificationObject = dataArray.getJSONObject(i);

                                    // Extract notification details
                                    String title = notificationObject.optString("title", "No Title");
                                    String message = notificationObject.optString("massage", "No Message");
                                    String timestamp = notificationObject.optString("created_at", String.valueOf(System.currentTimeMillis()));

                                    // Add to list
                                    NotificationModel notification = new NotificationModel(title, message, timestamp);
                                    newNotifications.add(notification);
                                }
                                adapter.updateNotifications(newNotifications); // Update the adapter
                            } else {
                                Toast.makeText(this, "No notifications found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Failed to fetch notifications.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parsing error occurred.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle errors
                    Toast.makeText(this, "Error fetching notifications: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Add the request to the queue
        requestQueue.add(jsonObjectRequest);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Format timestamp dynamically
    public static String getFormattedTime(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 1) {
            return "1 sec ago";
        } else if (seconds < 60) {
            return seconds + " secs ago";
        } else if (minutes < 60) {
            return minutes + " mins ago";
        } else if (hours < 24) {
            return hours + " hrs ago";
        } else if (days < 7) {
            return days + " days ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}
