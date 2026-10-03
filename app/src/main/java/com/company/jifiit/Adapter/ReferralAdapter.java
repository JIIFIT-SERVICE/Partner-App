package com.company.jifiit.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.R;
import com.company.jifiit.model.ReferralModel;

import java.util.List;

public class ReferralAdapter extends RecyclerView.Adapter<ReferralAdapter.ReferralViewHolder> {

    private Context context;
    private List<ReferralModel> referralList;

    public ReferralAdapter(Context context, List<ReferralModel> referralList) {
        this.context = context;
        this.referralList = referralList;
    }

    @NonNull
    @Override
    public ReferralViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_referral, parent, false);
        return new ReferralViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReferralViewHolder holder, int position) {
        ReferralModel referral = referralList.get(position);
        holder.nameTextView.setText(referral.getName());
        holder.createdAtTextView.setText(referral.getCreated_at());
        holder.monthsTextView.setText(referral.getMonths());
        holder.paymentTextView.setText(referral.getReferral_payment());
    }

    @Override
    public int getItemCount() {
        return referralList.size();
    }

    public class ReferralViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, createdAtTextView, monthsTextView, paymentTextView;

        public ReferralViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            createdAtTextView = itemView.findViewById(R.id.JoinDate);
            monthsTextView = itemView.findViewById(R.id.months);
            paymentTextView = itemView.findViewById(R.id.payment);
        }
    }
}
