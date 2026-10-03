package com.company.jifiit.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.jifiit.LocalStorage.DatabaseHelper;
import com.company.jifiit.R;
import com.company.jifiit.ViewMoreMenu.OnTotalPriceChangeListener;
import com.company.jifiit.model.SelectMassageModel;


import java.util.List;

public class SelectMassageAdapter extends RecyclerView.Adapter<SelectMassageAdapter.ViewHolder> {

    private Context context;
    private List<SelectMassageModel> massageList;
    String orderId, ProductName;
    private OnTotalPriceChangeListener listener;
    DatabaseHelper dbHelper;

    // Constructor
    public SelectMassageAdapter(Context context, List<SelectMassageModel> massageList, String orderId, String productName, OnTotalPriceChangeListener listener) {
        this.context = context;
        this.massageList = massageList;
        this.orderId = orderId;
        this.ProductName = productName;
        this.listener = listener;
        dbHelper = new DatabaseHelper(context);

    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Clear any existing views to avoid duplication

        holder.serviceContainer.removeAllViews();

        for (int i = 0; i < massageList.size(); i++) {
            SelectMassageModel model = massageList.get(i);

            // Inflate the layout for individual service inside the card
            View serviceView = LayoutInflater.from(context).inflate(R.layout.item_service, holder.serviceContainer, false);

            // Find the views in the inflated layout
            TextView txtDuration = serviceView.findViewById(R.id.duration);
            TextView txtOriginalPrice = serviceView.findViewById(R.id.originalPrice);
            TextView txtDiscountPrice = serviceView.findViewById(R.id.discount_price);
            TextView txtDiscounts = serviceView.findViewById(R.id.discounts);
            Button addServiceButton = serviceView.findViewById(R.id.add_service_button);
            LinearLayout quantityManagementLayout = serviceView.findViewById(R.id.quantity_management_layout);
            Button btnMinus = serviceView.findViewById(R.id.btn_minus);
            Button btnPlus = serviceView.findViewById(R.id.btn_plus);
            TextView txtQuantity = serviceView.findViewById(R.id.txt_quantity);
            View divider = serviceView.findViewById(R.id.divider);

            // Set the data for the views
            txtDuration.setText(model.getDuration() + " minutes");
            txtOriginalPrice.setText("₹" + model.getOriginalPrice());
            txtOriginalPrice.setPaintFlags(txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            txtDiscountPrice.setText("₹" + model.getDiscountPrice());
            txtDiscounts.setText(model.getDiscounts() + "% OFF");

//        dbHelper = new DatabaseHelper(context);

            if (i == massageList.size() - 1) {
                divider.setVisibility(View.GONE); // Hide the divider for the last item
            }

            // Add the inflated view (individual service) to the serviceContainer
            holder.serviceContainer.addView(serviceView);

// Adding Service Logic
            // Add Service Button Logic
            addServiceButton.setOnClickListener(v -> {
                boolean isAlreadyAdded = dbHelper.serviceExists(model.getProductId(), orderId,model.getType());

                if (isAlreadyAdded) {
                    addServiceButton.setVisibility(View.GONE);
                    quantityManagementLayout.setVisibility(View.VISIBLE);
                    // Service already exists, increment the quantity
                    int currentQuantity = dbHelper.getQuantityByProductIdAndOrderId(model.getProductId(), orderId,model.getType());
                    int newQuantity = currentQuantity + 1;

                    // Set the updated quantity in the TextView (ensure the quantity is converted to String)
                    txtQuantity.setText(String.valueOf(newQuantity));

                    // Update the quantity in the database
                    dbHelper.updateServiceQuantity(model.getProductId(), orderId, newQuantity);

                    // Update the total price and notify the listener
                    double updatedTotalPrice = dbHelper.getTotalPriceByOrderId(orderId);
                    listener.onTotalPriceChanged(updatedTotalPrice);

//                    Toast.makeText(context, "Service quantity updated", Toast.LENGTH_SHORT).show();
                } else {
                    // Service is not added yet, so add it
                    addServiceButton.setVisibility(View.GONE);
                    quantityManagementLayout.setVisibility(View.VISIBLE);

                    // Set initial quantity to 1
                    txtQuantity.setText("1");

                    // Insert the new service into the database
                    storeDataInSQLite(model.getProductId(), ProductName, model.getOriginalPrice(),
                            model.getDiscountPrice(), 1, model.getDuration(), model.getType(), model.getDiscounts(), orderId);

                    // Update total price
                    double updatedTotalPrice = dbHelper.getTotalPriceByOrderId(orderId);
                    listener.onTotalPriceChanged(updatedTotalPrice);

//                    Toast.makeText(context, "Service added", Toast.LENGTH_SHORT).show();
                }
            });

// Increment Quantity Logic
            btnPlus.setOnClickListener(v -> {
                // Get the current quantity from the text field
                int quantity = Integer.parseInt(txtQuantity.getText().toString()) + 1;
                txtQuantity.setText(String.valueOf(quantity));

                // Update the quantity in the database
                storeDataInSQLite(model.getProductId(), ProductName, model.getOriginalPrice(),
                        model.getDiscountPrice(), quantity, model.getDuration(), model.getType(), model.getDiscounts(), orderId);

                // Update total price
                double updatedTotalPrice = dbHelper.getTotalPriceByOrderId(orderId);
                listener.onTotalPriceChanged(updatedTotalPrice);
            });

// Decrement Quantity Logic
            btnMinus.setOnClickListener(v -> {
                // Get the current quantity from the text field
                int quantity = Integer.parseInt(txtQuantity.getText().toString());

                if (quantity > 1) {
                    // Decrease quantity if it's greater than 1
                    quantity--;
                    txtQuantity.setText(String.valueOf(quantity));

                    // Update the quantity in the database
                    storeDataInSQLite(model.getProductId(), ProductName, model.getOriginalPrice(),
                            model.getDiscountPrice(), quantity, model.getDuration(), model.getType(), model.getDiscounts(), orderId);

                    // Update total price after decreasing the quantity
                    double updatedTotalPrice = dbHelper.getTotalPriceByOrderId(orderId);
                    listener.onTotalPriceChanged(updatedTotalPrice);

                } else if (quantity == 1) {
                    // If the quantity is 1 and the minus button is clicked, delete the service
                    dbHelper.deleteService(model.getProductId(), orderId);

                    // Hide the quantity management and show the "Add" button
                    quantityManagementLayout.setVisibility(View.GONE);
                    addServiceButton.setVisibility(View.VISIBLE);

                    // Update total price after deleting the service
                    double updatedTotalPrice = dbHelper.getTotalPriceByOrderId(orderId);
                    listener.onTotalPriceChanged(updatedTotalPrice);

//                    Toast.makeText(context, "Service removed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
// Store data in SQLite, either add or update service quantity
            private void storeDataInSQLite(String id, String productName, String originalPrice, String discountPrice, int quantity,
            String duration, String type, String discounts, String orderId) {

                // Check if the service already exists in the database
                if (dbHelper.serviceExists(id, orderId,type)) {
                    // If it exists, update the quantity
                    dbHelper.updateServiceQuantity(id, orderId, quantity);
                } else {
                    // If it doesn't exist, insert the new service
                    dbHelper.insertService(id, productName, originalPrice, discountPrice, quantity,
                            duration, type, discounts, orderId);
                }
            }



    public int getItemCount() {
        // We return 1 because we want all items inside a single card
        return 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout serviceContainer; // Assuming you have a container for services

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceContainer = itemView.findViewById(R.id.serviceContainer);
        }
    }





}
