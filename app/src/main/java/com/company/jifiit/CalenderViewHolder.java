package com.company.jifiit;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalenderViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener
{    public TextView dayMonth;
    public CalenderAdapter.OnItemListener onItemListener;
    public CalenderViewHolder(@NonNull View itemView)
    {
        super(itemView);
        dayMonth=itemView.findViewById(R.id.cellDayText);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAdapterPosition(),(String) dayMonth.getText());
    }
}
