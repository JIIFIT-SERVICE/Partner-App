package com.company.jifiit.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;

import java.util.List;

public class RescheduleAdapter extends RecyclerView.Adapter<RescheduleAdapter.ViewHolder> {

    private List<String[]> datesList;
    private OnDateClickListener onDateClickListener;
    private int selectedPosition = -1;

    // Constructor with pre-selected date
    public RescheduleAdapter(List<String[]> datesList, OnDateClickListener onDateClickListener, String preSelectedDate) {
        this.datesList = datesList;
        this.onDateClickListener = onDateClickListener;

        // Find the position of the pre-selected date
        for (int i = 0; i < datesList.size(); i++) {
            if (datesList.get(i)[1].equals(preSelectedDate)) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String displayDate = datesList.get(position)[0];
        String fullDate = datesList.get(position)[1];

        holder.textViewDate.setText(displayDate);

        if (selectedPosition == position) {
            holder.textViewDate.setText("");
            holder.textViewDate.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.green_tick, 0, 0);
        } else {
            holder.textViewDate.setText(displayDate);
            holder.textViewDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onDateClickListener != null) {
                onDateClickListener.onDateClick(fullDate);
            }

            int previousPosition = selectedPosition;
            selectedPosition = position;

            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return datesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }

    public interface OnDateClickListener {
        void onDateClick(String fullDate);
    }
}
