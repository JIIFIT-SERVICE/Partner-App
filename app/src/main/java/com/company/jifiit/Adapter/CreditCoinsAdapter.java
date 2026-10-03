package com.company.jifiit.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;
import com.company.jifiit.model.CreditCoinsModel;

import java.util.ArrayList;

public class CreditCoinsAdapter extends RecyclerView.Adapter<CreditCoinsAdapter.ViewHolder> {

    private ArrayList<CreditCoinsModel> creditCoinList;
    private Context context;

    public CreditCoinsAdapter(ArrayList<CreditCoinsModel> creditCoinList, Context context) {
        this.creditCoinList = creditCoinList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_add_coins, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CreditCoinsModel creditCoin = creditCoinList.get(position);
        holder.amountTextView.setText(creditCoin.getWallet());
        holder.dateTextView.setText(creditCoin.getDate());
        holder.timeTextView.setText(creditCoin.getTime());
        holder.coinsTextView.setText(creditCoin.getBalance());
    }

    @Override
    public int getItemCount() {
        return creditCoinList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView amountTextView;
        public TextView dateTextView, timeTextView, coinsTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.creditCoins);
            dateTextView = itemView.findViewById(R.id.creditDate);
            timeTextView = itemView.findViewById(R.id.creditTime);
            coinsTextView = itemView.findViewById(R.id.creditBalance);
        }
    }
}
