package com.company.jifiit.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.R;

import java.util.List;

public class CustomImageAdapter extends RecyclerView.Adapter<CustomImageAdapter.SliderViewHolder> {
    private List<String> imageUrls;
    private RequestQueue requestQueue;
    private LruBitmapCache imageCache; // Declare the image cache

    // Pass context to the adapter
    public CustomImageAdapter(Context context, List<String> imageUrls) {
        this.imageUrls = imageUrls;
        // Initialize Volley RequestQueue
        this.requestQueue = Volley.newRequestQueue(context); // Use the passed context

        // Initialize the image cache with a size of 1/8th of the available memory
        this.imageCache = new LruBitmapCache((int) (Runtime.getRuntime().maxMemory() / 1024) / 8);
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        loadImage(holder.imageView, imageUrl); // Load the image from the URL
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    private void loadImage(ImageView imageView, String url) {
        ImageLoader imageLoader = new ImageLoader(requestQueue, imageCache); // Use the image cache

        imageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    imageView.setImageBitmap(response.getBitmap());
                } else {
                    imageView.setImageResource(R.mipmap.image_4); // Use your error image here
                }
            }

            @Override
            public void onErrorResponse(com.android.volley.VolleyError error) {
                // Handle error, set a placeholder or error image
                imageView.setImageResource(R.mipmap.image_8); // Use your error image here
            }
        });
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        SliderViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
