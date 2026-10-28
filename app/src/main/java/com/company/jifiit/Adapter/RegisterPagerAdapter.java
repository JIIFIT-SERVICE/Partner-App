
package com.company.jifiit.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;

public class RegisterPagerAdapter
        extends RecyclerView.Adapter<RegisterPagerAdapter.PageViewHolder> {

    private final int[] layouts = {
            R.layout.register_page_1,
            R.layout.register_page_experienced,
            R.layout.register_page_job_city_select,
            R.layout.register_page_4
    };

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);

        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PageViewHolder holder,
            int position
    ) {
        // Page views are initialized from Register.java.
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
        PageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
