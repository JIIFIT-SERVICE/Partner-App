package com.company.jifiit.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.ViewMoreActivity;
import com.company.jifiit.ViewMoreMenu.BookUpgradeActivity;
import com.company.jifiit.model.CartModel;

import java.nio.Buffer;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartModel> cartItemList;
    private final Context context;
    String orderId,bookingType;

    // Constructor for the adapter
    public CartAdapter(Context context, List<CartModel> cartItemList,String orderId,String bookingType) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.orderId = orderId;
        this.bookingType = bookingType;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each cart item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categorys_list, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        // Get the data for the current position
        CartModel cartItem = cartItemList.get(position);

        // Bind data to the UI components in the cart item layout
        holder.discountPrice.setText("₹"+cartItem.getDiscountPrice());
        holder.originalPrice.setText("₹"+cartItem.getOriginalPrice());
        holder.productName.setText(cartItem.getProduct_name()); // Updated to camelCase
        holder.saveAmount.setText("Save ₹"+cartItem.getSave());
        holder.duration.setText(cartItem.getDuration());
        holder.options.setText(cartItem.getNameCount()+" options");
        holder.discounts.setText(cartItem.getDiscounts()+"% OFF");
        holder.originalPrice.setPaintFlags(holder.originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Initialize imagePath
        String imagePath = ""; // Default value

        // Check the table name and set the image path accordingly
        if ("packages".equals(cartItem.getTable_name())) {
            // Change the image path if table_name is "packages"
            imagePath = Constant.packages_ImgPath + cartItem.getImages();
            holder.options.setVisibility(View.GONE);
        } else if ("add_on".equals(cartItem.getTable_name())) {
            imagePath = Constant.add_onsImgPath + cartItem.getImages();
            holder.options.setVisibility(View.VISIBLE);
            Animation slideAnimation = AnimationUtils.loadAnimation(context, R.anim.text_slide_in_out);
            holder.options.startAnimation(slideAnimation);
        } else if ("oil_massage".equals(cartItem.getTable_name())) {
            imagePath = Constant.home_massageImgPath + cartItem.getImages();
            holder.options.setVisibility(View.VISIBLE);
            Animation slideAnimation = AnimationUtils.loadAnimation(context, R.anim.text_slide_in_out);
            holder.options.startAnimation(slideAnimation);
        } else if ("dry_massage".equals(cartItem.getTable_name())) {
            imagePath = Constant.home_massageImgPath + cartItem.getImages();
            holder.options.setVisibility(View.VISIBLE);
            Animation slideAnimation = AnimationUtils.loadAnimation(context, R.anim.text_slide_in_out);
            holder.options.startAnimation(slideAnimation);
        }
        // Load image using Glide
        Glide.with(context)
                .load(imagePath)
                .placeholder(R.drawable.image_24) // Default image while loading
                .error(R.drawable.image_24) // Default image on error
                .override(300, 300) // Resize the image to a smaller resolution (adjust size as needed)
                .centerCrop() // Crop the image to fill the ImageView
                .into(holder.images);

        // Set the image in the ImageView

        holder.addBtn.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, BookUpgradeActivity.class);
            intent.putExtra("product_name", cartItem.getProduct_name());
            intent.putExtra("table_name", cartItem.getTable_name());
            intent.putExtra("id", orderId); // Pass the Id to the next activity
            intent.putExtra("bookingType", bookingType); // Pass the Id to the next activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    // ViewHolder class for holding the views for each item in the cart
    static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView discountPrice, originalPrice, productName, saveAmount, duration, options, discounts;
        ImageView images;
        Button addBtn;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the views from the cart item layout
            discountPrice = itemView.findViewById(R.id.discountprice);
            originalPrice = itemView.findViewById(R.id.originalprice);
            productName = itemView.findViewById(R.id.title);
            saveAmount = itemView.findViewById(R.id.save_amount);
            duration = itemView.findViewById(R.id.time);
            options = itemView.findViewById(R.id.get_details);
            discounts = itemView.findViewById(R.id.discounts);
            images = itemView.findViewById(R.id.thumbnail);
            addBtn = itemView.findViewById(R.id.add_service_button);


        }
    }
}
