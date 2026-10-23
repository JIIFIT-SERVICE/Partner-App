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

    private final ArrayList<Area> selectedAreas = new ArrayList<>();
    private final Set<String> selectedIds;
    private final SelectionListener listener;

    public AreaAdapter(
            Set<String> selectedIds,
            SelectionListener listener
    ) {
        this.selectedIds = selectedIds;
        this.listener = listener;
    }

    public boolean addArea(Area area) {
        if (area == null) return false;
        if (!selectedIds.contains(area.id)) {
            if (selectedIds.size() >= 15) {
                return false;
            }
            selectedIds.add(area.id);
            selectedAreas.add(area);
            notifyItemInserted(selectedAreas.size() - 1);
            if (listener != null) {
                listener.onSelectionChanged();
            }
            return true;
        }
        return false;
    }

    public void removeArea(Area area) {
        if (area == null) return;
        int index = -1;
        for (int i = 0; i < selectedAreas.size(); i++) {
            if (selectedAreas.get(i).id.equals(area.id)) {
                index = i;
                break;
            }
        }
        selectedIds.remove(area.id);
        if (index != -1) {
            selectedAreas.remove(index);
            notifyItemRemoved(index);
        } else {
            notifyDataSetChanged();
        }
        if (listener != null) {
            listener.onSelectionChanged();
        }
    }

    public void syncSelected(ArrayList<Area> allAvailableAreas) {
        selectedAreas.clear();
        if (allAvailableAreas != null) {
            for (Area a : allAvailableAreas) {
                if (selectedIds.contains(a.id)) {
                    selectedAreas.add(a);
                }
            }
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged();
        }
    }

    public void clear() {
        selectedAreas.clear();
        selectedIds.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged();
        }
    }

    public ArrayList<Area> getSelectedAreas() {
        return selectedAreas;
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
        Area area = selectedAreas.get(position);

        holder.name.setText(area.name);

        holder.card.setStrokeColor(ColorStateList.valueOf(
                Color.parseColor("#BFDBFE")
        ));

        holder.card.setCardBackgroundColor(
                Color.parseColor("#EFF6FF")
        );

        holder.check.setImageResource(R.drawable.ic_chip_close);
        holder.check.setImageTintList(ColorStateList.valueOf(Color.parseColor("#EF4444")));

        holder.name.setTextColor(
                Color.parseColor("#1E3A8A")
        );

        holder.itemView.setOnClickListener(v -> removeArea(area));
        holder.check.setOnClickListener(v -> removeArea(area));
    }

    @Override
    public int getItemCount() {
        return selectedAreas.size();
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
