package com.company.jifiit.Adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;
import com.company.jifiit.model.NotificationModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationModel> notificationList;
    private Context context;
    private final Handler handler = new Handler();
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged(); // Refresh timestamps
            handler.postDelayed(this, 60000); // Update every 1 minute
        }
    };

    public NotificationAdapter(List<NotificationModel> notificationList, Context context) {
        this.notificationList = (notificationList != null) ? notificationList : List.of();
        this.context = context;
        handler.postDelayed(updateRunnable, 60000); // Start auto-update
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);

        holder.notificationTitle.setText(notification.getTitle());
        holder.notificationMessage.setText(notification.getMessage());

        // Set formatted time
        holder.notificationTime.setText(getTimeAgo(notification.getTimestamp()));
    }

    // Function to calculate "X time ago" from timestamp (in milliseconds)
    private String getTimeAgo(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = sdf.parse(timestamp);
            if (date == null) return "Invalid date";

            long notificationTimeMillis = date.getTime();
            long currentTimeMillis = System.currentTimeMillis();
            long diffMillis = currentTimeMillis - notificationTimeMillis;

            if (diffMillis < 0) return "Future date"; // Handle future timestamps

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffMillis);

            if (seconds < 60) return "Just now";
            if (minutes < 60) return minutes + " min ago";
            if (hours < 24) return hours + " hours ago";
            if (days == 1) return "Yesterday";
            if (days < 7) return days + " days ago";
            if (days < 30) return (days / 7) + " weeks ago";

            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Invalid date";
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView notificationTitle, notificationMessage, notificationTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationMessage = itemView.findViewById(R.id.notificationMessage);
            notificationTime = itemView.findViewById(R.id.notificationTimestamp);
        }
    }

    // Method to update notifications dynamically
    public void updateNotifications(List<NotificationModel> newNotifications) {
        this.notificationList = (newNotifications != null) ? newNotifications : List.of();
        notifyDataSetChanged();
    }

    // Stop auto-update when Adapter is destroyed
    public void stopUpdating() {
        handler.removeCallbacks(updateRunnable);
    }
}