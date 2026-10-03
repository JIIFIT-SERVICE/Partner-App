package com.company.jifiit.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.model.LeaveModel;
import com.company.jifiit.R;

import java.util.List;

public class LeaveAdapter extends RecyclerView.Adapter<LeaveAdapter.LeaveViewHolder> {
    private List<LeaveModel> leaveList;
    private Context context;

    public LeaveAdapter(List<LeaveModel> leaveList, Context context) {
        this.leaveList = leaveList;
        this.context = context;
    }

    @NonNull
    @Override
    public LeaveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_leave, parent, false);
        return new LeaveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaveViewHolder holder, int position) {
        LeaveModel leaveModel = leaveList.get(position);
//        holder.leaveId.setText("Leave id: " + leaveModel.getId());
        holder.dayName.setText(leaveModel.getDayName());
        holder.dateTextView.setText(leaveModel.getDate());
        holder.dayQuantitiesTextView.setText(leaveModel.getDayQuantitis());
        holder.FromTime.setText(leaveModel.getFromTime());
        holder.ToTime.setText(leaveModel.getToTime());

        String Leave_Type = leaveModel.getLeave_type();
        if ("CASUAL LEAVE".equals(Leave_Type)) {
            holder.FromTime.setVisibility(View.VISIBLE);
            holder.ToTime.setVisibility(View.VISIBLE);
            holder.dayName.setVisibility(View.GONE);
            holder.dayQuantitiesTextView.setVisibility(View.GONE);
        } else {
            holder.FromTime.setVisibility(View.GONE);
            holder.ToTime.setVisibility(View.GONE);
            holder.dayName.setVisibility(View.VISIBLE);
            holder.dayQuantitiesTextView.setVisibility(View.VISIBLE);
        }

        String Approve = leaveModel.getApproval();
        holder.approvalTextView.setText(Approve);
        if ("approved".equals(Approve)) {
            holder.approvalTextView.setTextColor(Color.GREEN);
        } else if ("pending".equals(Approve)) {
            holder.approvalTextView.setTextColor(Color.parseColor("#FFA500"));
        } else if ("rejected".equals(Approve)) {
            holder.approvalTextView.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    public class LeaveViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, dayQuantitiesTextView, approvalTextView,dayName,ToTime,FromTime;

        public LeaveViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date);
            dayQuantitiesTextView = itemView.findViewById(R.id.dayNumber);
            approvalTextView = itemView.findViewById(R.id.leaveStatus);
//            leaveId = itemView.findViewById(R.id.LeaveId);
            dayName= itemView.findViewById(R.id.dayName);
            FromTime= itemView.findViewById(R.id.fromTime);
            ToTime= itemView.findViewById(R.id.toTime);

        }
    }
}
