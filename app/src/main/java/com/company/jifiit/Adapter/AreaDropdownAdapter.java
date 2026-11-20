package com.company.jifiit.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.company.jifiit.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AreaDropdownAdapter extends BaseAdapter implements Filterable {

    private final Context context;
    private final List<AreaAdapter.Area> originalList = new ArrayList<>();
    private final List<AreaAdapter.Area> filteredList = new ArrayList<>();
    private final LayoutInflater inflater;

    public AreaDropdownAdapter(Context context, List<AreaAdapter.Area> areas) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        setAreas(areas);
    }

    public void setAreas(List<AreaAdapter.Area> areas) {
        originalList.clear();
        filteredList.clear();
        if (areas != null) {
            for (AreaAdapter.Area a : areas) {
                if (a != null && a.name != null && !a.name.trim().isEmpty()) {
                    originalList.add(a);
                }
            }
            // Sort ascending A-Z
            Collections.sort(originalList, (a, b) ->
                    a.name.trim().compareToIgnoreCase(b.name.trim())
            );
            filteredList.addAll(originalList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public AreaAdapter.Area getItem(int position) {
        if (position >= 0 && position < filteredList.size()) {
            return filteredList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_area_dropdown, parent, false);
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tvDropdownAreaName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AreaAdapter.Area item = getItem(position);
        if (item != null) {
            holder.tvName.setText(item.name.trim());
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvName;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<AreaAdapter.Area> suggestions = new ArrayList<>();

                if (constraint == null || constraint.toString().trim().isEmpty()) {
                    suggestions.addAll(originalList);
                } else {
                    String query = constraint.toString().trim().toLowerCase(Locale.ROOT);
                    List<AreaAdapter.Area> startsWith = new ArrayList<>();
                    List<AreaAdapter.Area> contains = new ArrayList<>();

                    for (AreaAdapter.Area item : originalList) {
                        String name = item.name.trim().toLowerCase(Locale.ROOT);
                        if (name.startsWith(query)) {
                            startsWith.add(item);
                        } else if (name.contains(query)) {
                            contains.add(item);
                        }
                    }

                    // Sort both in alphabetical ascending order
                    Collections.sort(startsWith, (a, b) ->
                            a.name.trim().compareToIgnoreCase(b.name.trim())
                    );
                    Collections.sort(contains, (a, b) ->
                            a.name.trim().compareToIgnoreCase(b.name.trim())
                    );

                    suggestions.addAll(startsWith);
                    suggestions.addAll(contains);
                }

                results.values = suggestions;
                results.count = suggestions.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                if (results != null && results.values != null) {
                    filteredList.addAll((List<AreaAdapter.Area>) results.values);
                }
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof AreaAdapter.Area) {
                    return ((AreaAdapter.Area) resultValue).name.trim();
                }
                return super.convertResultToString(resultValue);
            }
        };
    }
}
