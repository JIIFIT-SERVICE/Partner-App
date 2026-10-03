package com.company.jifiit.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;
import com.company.jifiit.model.CommissionModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommissionAdapter extends RecyclerView.Adapter<CommissionAdapter.ViewHolder> {

    private Context context;
    private List<CommissionModel> list;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();
    private OnItemSelectionListener selectionListener;

    public interface OnItemSelectionListener {
        void onSelectionChanged(int selectedCount, List<String> selectedIds);
    }

    public CommissionAdapter(Context context, List<CommissionModel> list) {
        this.context = context;
        this.list = list;
    }

    public void setOnItemSelectionListener(OnItemSelectionListener listener) {
        this.selectionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.commission_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommissionModel model = list.get(position);

        // Set data to views
        holder.bookId.setText("Booking ID: #" + model.getBookingId());
        holder.bookingType.setText(model.getBookingType());
        holder.bookingDate.setText(model.getBookingDate());
        holder.bookingTime.setText(model.getBookingTime());
        holder.customerName.setText(model.getCustomerName());
        holder.massageName.setText(model.getMassageName());
        holder.CusterPaid.setText(model.getcustomerPaidStr());
        holder.bookingAmounts.setText(model.bookingAmounts());
        holder.commission.setText(model.getCommission());
        holder.gst.setText(model.getGst());
        holder.netAmount.setText(model.getNetAmount());
        holder.product_cost.setText(model.getproduct_costStr());

        // Set payment status background
        int paymentStatus = model.getTh_payment_status();
        if (paymentStatus == 0) {
            holder.paymentLayout.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.due_background_orange)
            );
        } else {
            holder.paymentLayout.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.paid_background)
            );
        }

        // Handle selection mode with overlay
        if (isSelectionMode) {
            holder.selectionOverlay.setVisibility(View.VISIBLE);

            if (paymentStatus == 0) {
                // Unpaid - Selectable
                holder.itemView.setClickable(true);
                holder.itemView.setEnabled(true);
                holder.disabledOverlay.setVisibility(View.GONE);

                if (selectedPositions.contains(position)) {
                    // Selected state - Green overlay with checkmark
                    holder.selectionOverlay.setBackgroundColor(
                            Color.parseColor("#80A8D08D")  // 33 = 20% opacity
                    );
                    holder.selectionCheckmark.setVisibility(View.VISIBLE);
                    holder.selectionCheckmark.setImageResource(R.drawable.ic_check_circle);
                    holder.selectionCheckmark.setColorFilter(
                            ContextCompat.getColor(context, R.color.white)
                    );
                } else {
                    // Unselected state - Transparent overlay
                    holder.selectionOverlay.setBackgroundColor(
                            ContextCompat.getColor(context, R.color.selection_unselected)
                    );
                    holder.selectionCheckmark.setVisibility(View.GONE);
                }
            } else {
                // Paid - Not selectable, show disabled overlay
                holder.itemView.setClickable(false);
                holder.itemView.setEnabled(false);
                holder.disabledOverlay.setVisibility(View.VISIBLE);
                holder.selectionOverlay.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.selection_disabled)
                );
                holder.selectionCheckmark.setVisibility(View.GONE);
            }
        } else {
            // Normal mode - Hide all selection UI
            holder.selectionOverlay.setVisibility(View.GONE);
            holder.selectionCheckmark.setVisibility(View.GONE);
            holder.disabledOverlay.setVisibility(View.GONE);
            holder.itemView.setClickable(true);
            holder.itemView.setEnabled(true);
        }

        // Item click listener
        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode && model.getTh_payment_status() == 0) {
                toggleSelection(position);
            }
        });
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);

        if (selectionListener != null) {
            List<String> selectedIds = getSelectedIds();
            selectionListener.onSelectionChanged(selectedIds.size(), selectedIds);
        }
    }

    public List<String> getSelectedIds() {
        List<String> selectedIds = new ArrayList<>();
        for (Integer position : selectedPositions) {
            CommissionModel model = list.get(position);
            selectedIds.add(model.getBookingId());
        }
        return selectedIds;
    }

    // Add this method to CommissionAdapter class
    public void selectAll() {
        selectedPositions.clear();
        for (int i = 0; i < list.size(); i++) {
            CommissionModel model = list.get(i);
            if (model.getTh_payment_status() == 0) {
                selectedPositions.add(i);
            }
        }
        notifyDataSetChanged();

        if (selectionListener != null) {
            List<String> selectedIds = getSelectedIds();
            selectionListener.onSelectionChanged(selectedIds.size(), selectedIds);
        }
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();

        if (selectionListener != null) {
            selectionListener.onSelectionChanged(0, new ArrayList<>());
        }
    }

    public void setSelectionMode(boolean enabled) {
        this.isSelectionMode = enabled;
        if (!enabled) {
            selectedPositions.clear();
        }
        notifyDataSetChanged();

        if (selectionListener != null) {
            List<String> selectedIds = getSelectedIds();
            selectionListener.onSelectionChanged(selectedIds.size(), selectedIds);
        }
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }


    public int getSelectedCount() {
        return selectedPositions.size();
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void updateList(List<CommissionModel> newList) {
        list = newList;
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bookId, payment, bookingType;
        TextView bookingDate, bookingTime;
        TextView customerName, massageName;
        TextView bookingAmounts, commission, gst, netAmount, CusterPaid, product_cost;
        LinearLayout paymentLayout;
        FrameLayout selectionOverlay;
        ImageView selectionCheckmark;
        View disabledOverlay;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookId = itemView.findViewById(R.id.bookId);
            payment = itemView.findViewById(R.id.Payment);
            bookingType = itemView.findViewById(R.id.bookingType);
            bookingDate = itemView.findViewById(R.id.BookingDate);
            bookingTime = itemView.findViewById(R.id.BookingTime);
            customerName = itemView.findViewById(R.id.CusName);
            massageName = itemView.findViewById(R.id.MassageName);
            bookingAmounts = itemView.findViewById(R.id.bookingAmounts);
            commission = itemView.findViewById(R.id.commission);
            gst = itemView.findViewById(R.id.gst);
            netAmount = itemView.findViewById(R.id.netAmount);
            CusterPaid = itemView.findViewById(R.id.c_amount);
            product_cost = itemView.findViewById(R.id.prd_cst);
            paymentLayout = itemView.findViewById(R.id.paymentLayout);
            selectionOverlay = itemView.findViewById(R.id.selectionOverlay);
            selectionCheckmark = itemView.findViewById(R.id.selectionCheckmark);
            disabledOverlay = itemView.findViewById(R.id.disabledOverlay);
        }
    }
}