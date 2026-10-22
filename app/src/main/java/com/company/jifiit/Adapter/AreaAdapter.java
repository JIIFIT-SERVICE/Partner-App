
package com.company.jifiit.Adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class AreaAdapter extends RecyclerView.Adapter<AreaAdapter.AreaViewHolder> {

    public static class Area {
        public final String id;
        public final String name;

        public Area(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public interface SelectionListener {
        void onSelectionChanged();
    }

    private final ArrayList<Area> allAreas = new ArrayList<>();
    private final ArrayList<Area> visibleAreas = new ArrayList<>();

    private final Set<String> selectedIds;
    private final SelectionListener listener;

    public AreaAdapter(
            Set<String> selectedIds,
            SelectionListener listener
    ) {
        this.selectedIds = selectedIds;
        this.listener = listener;
    }

    public void setAreas(ArrayList<Area> areas) {
        allAreas.clear();
        allAreas.addAll(areas);
        filter("");
    }

    public void filter(String query) {
        visibleAreas.clear();

        String search = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        for (Area area : allAreas) {
            if (area.name.toLowerCase(Locale.ROOT).contains(search)) {
                visibleAreas.add(area);
            }
        }

        notifyDataSetChanged();
    }

    public void clear() {
        allAreas.clear();
        visibleAreas.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AreaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_area_chip, parent, false);

        return new AreaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AreaViewHolder holder,
            int position
    ) {
        Area area = visibleAreas.get(position);
        boolean selected = selectedIds.contains(area.id);

        holder.name.setText(area.name);

        holder.card.setStrokeColor(ColorStateList.valueOf(
                Color.parseColor(selected ? "#1F5FE0" : "#DFE6F0")
        ));

        holder.card.setCardBackgroundColor(
                Color.parseColor(selected ? "#EAF1FF" : "#FFFFFF")
        );

        holder.check.setImageResource(
                selected
                        ? android.R.drawable.checkbox_on_background
                        : android.R.drawable.checkbox_off_background
        );

        holder.check.setImageTintList(ColorStateList.valueOf(
                Color.parseColor(selected ? "#1F5FE0" : "#8491A5")
        ));

        holder.name.setTextColor(
                Color.parseColor(selected ? "#1F5FE0" : "#24344D")
        );

        holder.itemView.setOnClickListener(v -> {
            if (selectedIds.contains(area.id)) {
                selectedIds.remove(area.id);
            } else {
                selectedIds.add(area.id);
            }

            int currentPosition = holder.getBindingAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(currentPosition);
            }

            if (listener != null) {
                listener.onSelectionChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return visibleAreas.size();
    }

    static class AreaViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView card;
        TextView name;
        ImageView check;

        AreaViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.areaCard);
            name = itemView.findViewById(R.id.tvAreaName);
            check = itemView.findViewById(R.id.ivAreaCheck);
        }
    }
}
