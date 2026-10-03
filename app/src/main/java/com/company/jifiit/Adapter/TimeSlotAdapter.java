package com.company.jifiit.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {
    private final List<String> timeSlots;
    private final TimeSlotClickListener listener;
    private String selectedTime;

    public TimeSlotAdapter(List<String> timeSlots, String selectedTime, TimeSlotClickListener listener) {
        this.timeSlots = timeSlots;
        this.selectedTime = selectedTime;
        this.listener = listener;
    }

    @Override
    public TimeSlotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TimeSlotViewHolder holder, int position) {
        String time = timeSlots.get(position);
        holder.tvTimeSlot.setText(time);

        // Highlight if this is the selected time
        if (time.equals(selectedTime)) {
            holder.itemView.setBackgroundResource(R.drawable.time_slot_selected_bg);
            holder.tvTimeSlot.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.cold_blue));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.time_slot_bg);
            holder.tvTimeSlot.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
        }

        holder.itemView.setOnClickListener(v -> listener.onTimeSlotClick(time));
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimeSlot;

        public TimeSlotViewHolder(View itemView) {
            super(itemView);
            tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
        }
    }

    public interface TimeSlotClickListener {
        void onTimeSlotClick(String time);
    }
}