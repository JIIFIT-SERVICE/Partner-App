package com.company.jifiit.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.company.jifiit.R;

public class RegisterAdapter extends PagerAdapter {

    private Context context;
    private String[] slideTexts;

    public RegisterAdapter(Context context, String[] slideTexts) {
        this.context = context;
        this.slideTexts = slideTexts;
    }

    @Override
    public int getCount() {
        return slideTexts.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.register_layout, null);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView textView = view.findViewById(R.id.slideTextView);
        textView.setText(slideTexts[position]);
        textView.setTextColor(context.getResources().getColor(R.color.black));

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}