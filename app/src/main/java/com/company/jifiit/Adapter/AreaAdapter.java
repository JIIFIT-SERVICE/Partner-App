package com.company.jifiit.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.TypedValue;
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
import java.util.List;
import java.util.Set;

public class AreaAdapter extends RecyclerView.Adapter<AreaAdapter.AreaViewHolder> {

    public static class Area {
        public final String id;
        public final String name;
        public final String description;

        public Area(String id, String name) {
            this(id, name, "High demand residential & hotel hub");
        }

        public Area(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = (description != null && !description.trim().isEmpty())
                    ? description
                    : "High demand residential & hotel hub";
        }
    }

    public interface SelectionListener {
        void onSelectionChanged();
    }

    private final ArrayList<Area> areas = new ArrayList<>();
    private final Set<String> selectedIds;
    private final SelectionListener listener;

    public AreaAdapter(
            Set<String> selectedIds,
            SelectionListener listener
    ) {
        this.selectedIds = selectedIds;
        this.listener = listener;
    }

    public AreaAdapter(
            List<Area> initialAreas,
            Set<String> selectedIds,
            SelectionListener listener
    ) {
        if (initialAreas != null) {
            this.areas.addAll(initialAreas);
        }
        this.selectedIds = selectedIds;
        this.listener = listener;
    }

    public void setAreas(List<Area> newAreas) {
        areas.clear();
        if (newAreas != null) {
            areas.addAll(newAreas);
        }
        notifyDataSetChanged();
    }

    public boolean addArea(Area area) {
        if (area == null) return false;
        if (!selectedIds.contains(area.id)) {
            selectedIds.add(area.id);
            int idx = areas.indexOf(area);
            if (idx != -1) {
                notifyItemChanged(idx);
            } else {
                notifyDataSetChanged();
            }
            if (listener != null) {
                listener.onSelectionChanged();
            }
            return true;
        }
        return false;
    }

    public void removeArea(Area area) {
        if (area == null) return;
        selectedIds.remove(area.id);
        int idx = areas.indexOf(area);
        if (idx != -1) {
            notifyItemChanged(idx);
        } else {
            notifyDataSetChanged();
        }
        if (listener != null) {
            listener.onSelectionChanged();
        }
    }

    public void toggleSelection(int position) {
        if (position < 0 || position >= areas.size()) return;
        Area area = areas.get(position);
        if (selectedIds.contains(area.id)) {
            selectedIds.remove(area.id);
        } else {
            selectedIds.add(area.id);
        }
        notifyItemChanged(position);
        if (listener != null) {
            listener.onSelectionChanged();
        }
    }

    public void syncSelected(ArrayList<Area> allAvailableAreas) {
        areas.clear();
        if (allAvailableAreas != null) {
            areas.addAll(allAvailableAreas);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged();
        }
    }

    public void clear() {
        areas.clear();
        selectedIds.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged();
        }
    }

    public ArrayList<Area> getSelectedAreas() {
        ArrayList<Area> selectedList = new ArrayList<>();
        for (Area a : areas) {
            if (selectedIds.contains(a.id)) {
                selectedList.add(a);
            }
        }
        return selectedList;
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    @NonNull
    @Override
    public AreaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_area_hub, parent, false);

        return new AreaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AreaViewHolder holder,
            int position
    ) {
        Area area = areas.get(position);
        Context context = holder.itemView.getContext();
        boolean isSelected = selectedIds.contains(area.id);

        holder.tvHubTitle.setText(area.name);
        holder.tvHubSubtitle.setText(area.description);

        if (isSelected) {
            // Selected state: Light blue background, bold blue stroke, checked box
            holder.card.setCardBackgroundColor(Color.parseColor("#F4F8FE"));
            holder.card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#1A56DB")));
            holder.card.setStrokeWidth(dpToPx(context, 1.5f));
            holder.ivCheckbox.setImageResource(R.drawable.ic_hub_checkbox_checked);
        } else {
            // Unselected state: Clean white background, subtle grey border, unchecked box
            holder.card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E2E8F0")));
            holder.card.setStrokeWidth(dpToPx(context, 1.0f));
            holder.ivCheckbox.setImageResource(R.drawable.ic_hub_checkbox_unchecked);
        }

        holder.card.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                toggleSelection(currentPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return areas.size();
    }

    private int dpToPx(Context context, float dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        ));
    }

    static class AreaViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final ImageView ivCheckbox;
        final TextView tvHubTitle;
        final TextView tvHubSubtitle;

        AreaViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardAreaHub);
            ivCheckbox = itemView.findViewById(R.id.ivCheckbox);
            tvHubTitle = itemView.findViewById(R.id.tvHubTitle);
            tvHubSubtitle = itemView.findViewById(R.id.tvHubSubtitle);
        }
    }
}
