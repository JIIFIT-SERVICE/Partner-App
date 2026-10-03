package com.company.jifiit.Hotel.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;

public class HotelRegistrationAdapter
        extends RecyclerView.Adapter<HotelRegistrationAdapter.PageViewHolder> {

    private final int[] layouts = {
            R.layout.hotel_page_1,
            R.layout.hotel_page_2,
            R.layout.hotel_page_3,
            R.layout.hotel_page_4,
            R.layout.hotel_page_5,
            R.layout.hotel_page_7
    };

    // No-argument constructor - Fixed the issue
    public HotelRegistrationAdapter() {
        // Empty constructor
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);

        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PageViewHolder holder,
            int position) {
        // Nothing needed here
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return layouts[position];
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}