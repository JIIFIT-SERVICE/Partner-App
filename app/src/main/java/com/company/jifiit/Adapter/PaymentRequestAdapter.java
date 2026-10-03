package com.company.jifiit.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;
import com.company.jifiit.model.PaymentRequestModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentRequestAdapter extends RecyclerView.Adapter<PaymentRequestAdapter.ViewHolder> {

    private Context context;
    private List<PaymentRequestModel> paymentList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(PaymentRequestModel payment);
        void onViewReceiptClick(PaymentRequestModel payment);
    }

    public PaymentRequestAdapter(Context context, List<PaymentRequestModel> paymentList) {
        this.context = context;
        this.paymentList = paymentList != null ? paymentList : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.payment_histrory_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentRequestModel payment = paymentList.get(position);

        if (payment == null) {
            return;
        }

        // Set date range
        holder.StartDate.setText(payment.getStartDate());
        holder.EndDaTe.setText(payment.getEndDate());

        // Set booking count
        holder.TotalBooking.setText(payment.getTotalBooking());
        holder.tvTotalPaid.setText(formatCurrency(payment.getTotalPaid()));
        holder.tvRequestedDate.setText(payment.getPaymentRequestDate());
        holder.tvReceiveDate.setText(payment.getUpdatedDate());
        holder.tvPlartFormFee.setText(payment.getPlarfromFee());
        // Set earnings
        holder.requestEarning.setText(formatCurrency(payment.getRequestedEarning()));

        // Set deductions (only show if they exist)
        String bedDeduction = payment.getDeductionForBed();
        if (bedDeduction != null && !bedDeduction.isEmpty() && !bedDeduction.equals("0") && !bedDeduction.equals("0.00")) {
            holder.deductionBed.setText(formatCurrency(bedDeduction));
            holder.bedLayout.setVisibility(View.VISIBLE);
        } else {
            holder.bedLayout.setVisibility(View.GONE);
        }

        String fineDeduction = payment.getDeductionForLateFine();
        if (fineDeduction != null && !fineDeduction.isEmpty() && !fineDeduction.equals("0") && !fineDeduction.equals("0.00")) {
            holder.deductionFine.setText(formatCurrency(fineDeduction));
            holder.fineLayout.setVisibility(View.VISIBLE);
        } else {
            holder.fineLayout.setVisibility(View.GONE);
        }

        String cancelDeduction = payment.getDeductionForCanceledBooking();
        if (cancelDeduction != null && !cancelDeduction.isEmpty() && !cancelDeduction.equals("0") && !cancelDeduction.equals("0.00")) {
            holder.deductionCanceled.setText(formatCurrency(cancelDeduction));
            holder.canceledLayout.setVisibility(View.VISIBLE);
        } else {
            holder.canceledLayout.setVisibility(View.GONE);
        }

        String othersfine = payment.getDeductionForOthers();
        if (othersfine != null && !othersfine.isEmpty() && !othersfine.equals("0") && !othersfine.equals("0.00")) {
            holder.deductionOth.setText(formatCurrency(othersfine));
            holder.othersLayout.setVisibility(View.VISIBLE);
        } else {
            holder.othersLayout.setVisibility(View.GONE);
        }

        String plartformfees = payment.getPlarfromFee();
        if (plartformfees != null && !plartformfees.isEmpty() && !plartformfees.equals("0") && !plartformfees.equals("0.00")) {
            holder.tvPlartFormFee.setText(formatCurrency(plartformfees));
            holder.PlartformLayout.setVisibility(View.VISIBLE);
        } else {
            holder.PlartformLayout.setVisibility(View.GONE);
        }

        // Set total paid with status
        String status = payment.getStatus();
        String statusText = "";
        int statusColor = context.getResources().getColor(R.color.orange);

        if (status != null) {
            switch (status) {
                case "0":
                    statusText = "Pending";
                    holder.tvTotalPaidLabel.setText("Pending Amount");
                    holder.tvViewReceipt.setVisibility(View.GONE);
                    holder.paymenteUpdatedLayer.setVisibility(View.GONE);
                    statusColor = context.getResources().getColor(R.color.orange);
                    break;
                case "1":
                    statusText = "Completed";
                    holder.tvTotalPaidLabel.setText("Total Paid");
                    holder.tvViewReceipt.setVisibility(View.VISIBLE);
                    holder.paymenteUpdatedLayer.setVisibility(View.VISIBLE);
                    statusColor = context.getResources().getColor(R.color.Green);
                    break;
                case "2":
                    statusText = "Pending";
                    holder.tvTotalPaidLabel.setText("Pending Amount");
                    holder.tvViewReceipt.setVisibility(View.GONE);
                    holder.paymenteUpdatedLayer.setVisibility(View.GONE);
                    statusColor = context.getResources().getColor(R.color.orange);
                    break;
                default:
                    statusText = "Unknown";
                    break;
            }
        }


        holder.tvTotalPaidLabel.setTextColor(statusColor);
        holder.tvTotalPaid.setTextColor(statusColor);

        // Handle view receipt click
        holder.tvViewReceipt.setOnClickListener(v -> {
            String imageUrl = payment.getReceipt();

            // Open in browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
            v.getContext().startActivity(intent);
        });

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(payment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentList != null ? paymentList.size() : 0;
    }

    private String formatCurrency(String amount) {
        try {
            if (amount == null || amount.isEmpty()) {
                return "₹ 0";
            }
            double value = Double.parseDouble(amount);
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            return currencyFormat.format(value);
        } catch (NumberFormatException e) {
            return "₹ 0";
        }
    }

    public void updateData(List<PaymentRequestModel> newList) {
        this.paymentList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addData(List<PaymentRequestModel> newList) {
        if (newList != null && !newList.isEmpty()) {
            int startPosition = paymentList.size();
            paymentList.addAll(newList);
            notifyItemRangeInserted(startPosition, newList.size());
        }
    }

    public void clearData() {
        paymentList.clear();
        notifyDataSetChanged();
    }

    public List<PaymentRequestModel> getPaymentList() {
        return paymentList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView StartDate, EndDaTe, TotalBooking, requestEarning, deductionBed,
                deductionFine, deductionCanceled, deductionOth, tvTotalPaidLabel, tvViewReceipt,tvTotalPaid,tvRequestedDate,tvReceiveDate,
                tvPlartFormFee;
        LinearLayout bedLayout, fineLayout, canceledLayout, othersLayout,paymentRquestLayer,paymenteUpdatedLayer,
                PlartformLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            StartDate = itemView.findViewById(R.id.tvStartDate);
            EndDaTe = itemView.findViewById(R.id.tvEndDate);
            TotalBooking = itemView.findViewById(R.id.tvTotalBooking);
            requestEarning = itemView.findViewById(R.id.tvTotalEarning);
            deductionBed = itemView.findViewById(R.id.tvBedDeduction);
            deductionFine = itemView.findViewById(R.id.tvFineDeduction);
            deductionCanceled = itemView.findViewById(R.id.tvCancelDeduction);
            deductionOth = itemView.findViewById(R.id.tvOthersDeduction);
            tvTotalPaidLabel = itemView.findViewById(R.id.tvTotalPaidLabel);
            tvViewReceipt = itemView.findViewById(R.id.tvViewReceipt);
            tvTotalPaid = itemView.findViewById(R.id.tvTotalPaid);
            tvRequestedDate = itemView.findViewById(R.id.tvRequestedDate);
            tvReceiveDate = itemView.findViewById(R.id.tvReceiveDate);
            tvPlartFormFee = itemView.findViewById(R.id.tvPlartFormFee);

            bedLayout = itemView.findViewById(R.id.bedLayout);
            fineLayout = itemView.findViewById(R.id.fineLayout);
            canceledLayout = itemView.findViewById(R.id.canceledLayout);
            othersLayout = itemView.findViewById(R.id.othersLayout);
            paymenteUpdatedLayer = itemView.findViewById(R.id.paymenteUpdatedLayer);
            paymentRquestLayer = itemView.findViewById(R.id.paymentRquestLayer);
            PlartformLayout = itemView.findViewById(R.id.PlartformLayout);
        }
    }
}