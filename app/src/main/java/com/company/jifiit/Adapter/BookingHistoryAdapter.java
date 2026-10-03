package com.company.jifiit.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;
import com.company.jifiit.model.BookingHistoryModel;

import java.util.List;
import android.text.Html;


public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {
    private Context context;
    private List<BookingHistoryModel> bookingList;

    public BookingHistoryAdapter(Context context, List<BookingHistoryModel> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingHistoryModel booking = bookingList.get(position);
        holder.bookingId.setText("Booking Id: " + booking.getId());
        holder.customerName.setText(booking.getCustomer_name());
        String formattedMassageName = booking.getMassage_name().replace("<br>", "<br/>");
        holder.massageName.setText(Html.fromHtml(formattedMassageName));
        holder.startTime.setText(booking.getStart_time());
        holder.duration.setText(booking.getDuration() + "Min");
        holder.orderDate.setText(booking.getOrder_date());
        holder.paymentType.setText(booking.getPayment_type());
//        holder.fare.setText("Fare: ₹" +booking.getFare());
        holder.CusId.setText("Customer Id: "+booking.getCustomers_id());
        holder.MassageType.setText(booking.getBookingType());
        holder.Amount.setText("Total (incl. 5% GST): ₹" +booking.getAmount());
        holder.rettings.setText(Html.fromHtml(booking.getRatings()));

//        String paymentStatus = booking.getPayment_status();
//        holder.paymentStatus.setText(paymentStatus);
//        if ("Completed".equals(paymentStatus)) {
//            holder.paymentStatus.setTextColor(Color.GREEN);
//        } else if ("Canceled".equals(paymentStatus)) {
//            holder.paymentStatus.setTextColor(Color.RED);
//        } else {
//            // Default color if status is unknown
//            holder.paymentStatus.setTextColor(Color.BLACK);
//        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bookingId, customerName, massageName, startTime, duration, orderDate, paymentType, fare,rettings,Amount,CusId,MassageType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookingId = itemView.findViewById(R.id.bookId);
            customerName = itemView.findViewById(R.id.CusName);
            massageName = itemView.findViewById(R.id.MassageName);
            startTime = itemView.findViewById(R.id.BookingTime);
            duration = itemView.findViewById(R.id.timeDuration);
            orderDate = itemView.findViewById(R.id.BookingDate);
            paymentType = itemView.findViewById(R.id.Payment);
//            fare = itemView.findViewById(R.id.Fare);
            rettings= itemView.findViewById(R.id.review);
            Amount= itemView.findViewById(R.id.amount);
            CusId= itemView.findViewById(R.id.CustomerId);
            MassageType= itemView.findViewById(R.id.bookingType);
        }
    }
}
