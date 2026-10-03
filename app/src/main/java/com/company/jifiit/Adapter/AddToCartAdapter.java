package com.company.jifiit.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.LocalStorage.DatabaseHelper;
import com.company.jifiit.R;
import com.company.jifiit.ViewMoreMenu.OnTotalOriginalPriceChangeListener;
import com.company.jifiit.ViewMoreMenu.OnTotalPriceChangeListener;
import com.company.jifiit.model.AddToCartModel;

import java.util.List;

public class AddToCartAdapter extends RecyclerView.Adapter<AddToCartAdapter.CartViewHolder> {

    private Context context;
    private List<AddToCartModel> cartList;
    DatabaseHelper dbHelper;
    private OnTotalPriceChangeListener listener;
    private OnTotalOriginalPriceChangeListener onTotalOriginalPriceChangeListener;
    // Constructor for the adapter
    public AddToCartAdapter(Context context, List<AddToCartModel> cartList, OnTotalPriceChangeListener listener,OnTotalOriginalPriceChangeListener onTotalOriginalPriceChangeListener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
        this.onTotalOriginalPriceChangeListener = onTotalOriginalPriceChangeListener;
        dbHelper = new DatabaseHelper(context);
    }

    // Inner ViewHolder class that holds item views
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView productName,discountprice,quantity,originalPrice;
        Button minus , pluse;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the views by their IDs in the item layout
            productName = itemView.findViewById(R.id.productname);
            discountprice = itemView.findViewById(R.id.discounted_price);
            originalPrice = itemView.findViewById(R.id.original_price);
            quantity = itemView.findViewById(R.id.txt_quantity);
            minus = itemView.findViewById(R.id.btn_minus);
            pluse = itemView.findViewById(R.id.btn_plus);



        }
    }

    // Inflate the item layout and create the ViewHolder
    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.add_to_cart_category_list, parent, false);
        return new CartViewHolder(view);
    }

    // Bind the data to the views
    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        AddToCartModel model = cartList.get(position);

        // Bind data to the TextViews
        holder.productName.setText(model.getProductName() + " (" + model.getDuration() + " minutes)"+model.getType());
        holder.originalPrice.setText("₹" + model.getOriginalPrice());
        holder.discountprice.setText("₹" + model.getDiscountPrice());
        holder.quantity.setText(String.valueOf(model.getQuantity()));  // Ensures the quantity is in the correct format
        holder.originalPrice.setPaintFlags(holder.originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        holder.pluse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Increment the quantity
                int quantity = Integer.parseInt(holder.quantity.getText().toString()) + 1;
                holder.quantity.setText(String.valueOf(quantity));

                // Calculate the original price based on the original single product price
                double originalPrice = Double.parseDouble(model.getOriginalPrice());
                double totalOriginalPrice = originalPrice * quantity;
                holder.originalPrice.setText("₹" + String.format("%.2f", totalOriginalPrice));

                double discountPrice = Double.parseDouble(model.getDiscountPrice());
                double totalDiscountPrice = discountPrice * quantity;
                holder.discountprice.setText("₹" + String.format("%.2f", totalDiscountPrice));

                // Update the quantity in the database
                storeDataInSQLite(model.getProductId(), quantity, model.getType(), model.getOrderId());

                // Update total price after incrementing the quantity
                double updatedTotalPrice = dbHelper.getTotalPriceByOrderId(model.getOrderId());
                listener.onTotalPriceChanged(updatedTotalPrice);

                double updatatedOriginalPrice = dbHelper.getTotalOriginalPriceByOrderId(model.getOrderId());
                onTotalOriginalPriceChangeListener.onTotalOriginalPriceChanged(updatatedOriginalPrice);

            }
        });

        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current quantity
                int quantity = Integer.parseInt(holder.quantity.getText().toString());

                if (quantity > 1) {
                    // Decrement the quantity if it's greater than 1
                    quantity--;
                    holder.quantity.setText(String.valueOf(quantity));

                    // Calculate the original price based on the new quantity
                    double originalPrice = Double.parseDouble(model.getOriginalPrice());
                    double totalOriginalPrice = originalPrice * quantity;
                    holder.originalPrice.setText("₹" + String.format("%.2f", totalOriginalPrice));

                    double discountPrice = Double.parseDouble(model.getDiscountPrice());
                    double totalDiscountPrice = discountPrice * quantity;
                    holder.discountprice.setText("₹" + String.format("%.2f", totalDiscountPrice));

                    // Update the quantity in the database
                    storeDataInSQLite(model.getProductId(), quantity, model.getType(), model.getOrderId());

                    // Update total price after decrementing the quantity
                    double updatedTotalPrice = dbHelper.getTotalPriceByOrderId(model.getOrderId());
                    listener.onTotalPriceChanged(updatedTotalPrice);

                    double updatatedOriginalPrice = dbHelper.getTotalOriginalPriceByOrderId(model.getOrderId());
                    onTotalOriginalPriceChangeListener.onTotalOriginalPriceChanged(updatatedOriginalPrice);

                } else if (quantity == 1) {
                    // If the quantity is 1 and the minus button is clicked, delete the service
                    dbHelper.deleteService(model.getProductId(), model.getOrderId());

                    // Remove the item from the list and notify the adapter
                    removeItem(holder.getAdapterPosition());

                    // Update total price after deleting the service
                    double updatedTotalPrice = dbHelper.getTotalPriceByOrderId(model.getOrderId());
                    listener.onTotalPriceChanged(updatedTotalPrice);

                    double updatatedOriginalPrice = dbHelper.getTotalOriginalPriceByOrderId(model.getOrderId());
                    onTotalOriginalPriceChangeListener.onTotalOriginalPriceChanged(updatatedOriginalPrice);
                }
            }
        });

    }

    // Method to remove an item from the list and notify the adapter
    public void removeItem(int position) {
        // Remove the item from the data list
        cartList.remove(position);
        // Notify the adapter that the item is removed
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,cartList.size());  // Optional: update the position of remaining items
    }


    private void storeDataInSQLite(String id, int quantity, String type,String orderId) {

        // Check if the service already exists in the database
        if (dbHelper.serviceExists(id, orderId,type)) {
            // If it exists, update the quantity
            dbHelper.updateServiceQuantity(id, orderId, quantity);
        }else{

        }
    }



    // Return the total number of items in the cart
    @Override
    public int getItemCount() {
        return cartList.size();
    }

    // Method to update the cart list (if needed)
    public void updateCartList(List<AddToCartModel> newCartList) {
        this.cartList = newCartList;
        notifyDataSetChanged();
    }
}
