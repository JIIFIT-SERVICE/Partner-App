package com.company.jifiit.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;

import java.util.List;

public class DatesAdapter extends RecyclerView.Adapter<DatesAdapter.DateViewHolder> {

    private List<String[]> datesList; // List of date pairs
    private OnDateClickListener onDateClickListener;
    private int selectedPosition = -1; // To track the selected position

    public DatesAdapter(List<String[]> datesList, OnDateClickListener onDateClickListener) {
        this.datesList = datesList;
        this.onDateClickListener = onDateClickListener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        String displayDate = datesList.get(position)[0]; // EE \n d format
        String fullDate = datesList.get(position)[1]; // yyyy-MM-dd format

        holder.textViewDate.setText(displayDate);

        // Change button color to green if selected, else default
        if (selectedPosition == position) {
            // Replace text with a green tick
            holder.textViewDate.setText("");
            holder.textViewDate.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.green_tick, 0, 0); // Icon at the top
        } else {
            // Restore default text
            holder.textViewDate.setText(displayDate);
            holder.textViewDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0); // Remove icon
        }

        // Set click listener for the date item
        holder.itemView.setOnClickListener(v -> {
            if (onDateClickListener != null) {
                onDateClickListener.onDateClick(fullDate); // Pass full date
            }
            // Update selected position and refresh the adapter
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return datesList.size();
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }

    // Interface for date click callback
    public interface OnDateClickListener {
        void onDateClick(String fullDate);
    }
}
