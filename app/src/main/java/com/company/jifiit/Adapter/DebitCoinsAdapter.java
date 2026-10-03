package com.company.jifiit.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;
import com.company.jifiit.model.DebitCoinsModel;

import java.util.ArrayList;

public class DebitCoinsAdapter extends RecyclerView.Adapter<DebitCoinsAdapter.ViewHolder> {

    private ArrayList<DebitCoinsModel> debitCoinsList;
    private Context context;

    public DebitCoinsAdapter(ArrayList<DebitCoinsModel> debitCoinsList, Context context) {
        this.debitCoinsList = debitCoinsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_debit_coins, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DebitCoinsModel debitCoin = debitCoinsList.get(position);
        holder.BookingId.setText(debitCoin.getOrder_id());
        holder.FareType.setText(debitCoin.getFareType());
        holder.Fare.setText(debitCoin.getFareValue());
        holder.balance.setText(debitCoin.getFarebalance());
        holder.date.setText(debitCoin.getDate());
        holder.time.setText(debitCoin.getTime());

    }

    @Override
    public int getItemCount() {
        return debitCoinsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
     TextView date,time,BookingId,FareType,Fare,balance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.debitDate);
            time = itemView.findViewById(R.id.debitTime);
            BookingId = itemView.findViewById(R.id.bookingId);
            FareType = itemView.findViewById(R.id.fareType);
            Fare = itemView.findViewById(R.id.debitCoins);
            balance = itemView.findViewById(R.id.debitBalance);
        }
    }
}
