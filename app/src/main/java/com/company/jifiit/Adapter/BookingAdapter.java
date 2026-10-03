package com.company.jifiit.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.MainActivity;
import com.company.jifiit.R;
import com.company.jifiit.ViewMoreActivity;
import com.company.jifiit.model.BookingModel;

import java.util.ArrayList;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
     ArrayList<BookingModel> bookings;

    // Constructor
    public BookingAdapter(ArrayList<BookingModel> bookings) {
        this.bookings = bookings;
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView customerNameTextView, massageNameTextView, startTimeTextView, durationTextView, addressTextView, paymentStatusTextView, idTextView;
        Button ViewMore;
        public BookingViewHolder(View itemView) {
            super(itemView);
            customerNameTextView = itemView.findViewById(R.id.CusName);
            massageNameTextView = itemView.findViewById(R.id.MassageName);
            startTimeTextView = itemView.findViewById(R.id.BookingTime);
            durationTextView = itemView.findViewById(R.id.timeDuration);
            addressTextView = itemView.findViewById(R.id.CusAddress);
            paymentStatusTextView = itemView.findViewById(R.id.bookingStatus);
            idTextView = itemView.findViewById(R.id.CusId);
            ViewMore = itemView.findViewById(R.id.viewMoreButton);
        }
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingModel booking  = bookings.get(position);
        holder.customerNameTextView.setText(booking.getCustomer_name());
        holder.massageNameTextView.setText(booking.getMassage_name());
        holder.startTimeTextView.setText(booking.getStart_time());
        holder.durationTextView.setText(booking.getDuration() + "min");
        holder.addressTextView.setText(booking.getAddress());
        holder.idTextView.setText("Booking Id: " +booking.getId());

        // Set text color based on payment status
        String paymentStatus = booking.getPayment_status();
        holder.paymentStatusTextView.setText(paymentStatus);

        switch (paymentStatus) {
            case "Completed":
                holder.paymentStatusTextView.setTextColor(Color.WHITE);
                holder.paymentStatusTextView.setBackgroundResource(R.drawable.bg_status_completed);
                break;
            case "Pending":
                holder.paymentStatusTextView.setTextColor(Color.WHITE);
                holder.paymentStatusTextView.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "Canceled":
                holder.paymentStatusTextView.setTextColor(Color.WHITE);
                holder.paymentStatusTextView.setBackgroundResource(R.drawable.bg_status_rejected);
                holder.ViewMore.setVisibility(View.GONE);
                break;
            default:
                holder.paymentStatusTextView.setTextColor(Color.WHITE);
//                holder.paymentStatusTextView.setBackgroundResource(R.drawable.bg_status_unknown);
                break;
        }

        holder.ViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, ViewMoreActivity.class);
                intent.putExtra("customer_name", booking.getCustomer_name());
                intent.putExtra("massage_name", booking.getMassage_name());
//                intent.putExtra("start_time", booking.getStart_time());
                intent.putExtra("duration", booking.getDuration());
                intent.putExtra("landmark", booking.getAddress());
                intent.putExtra("payment_status", booking.getPayment_status());
                intent.putExtra("id", booking.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }
}
