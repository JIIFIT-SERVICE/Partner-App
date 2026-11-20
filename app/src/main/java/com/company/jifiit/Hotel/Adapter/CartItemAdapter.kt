package com.company.jifiit.Hotel.Adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.LocalStorage.HotelDatabaseHelper
import com.company.jifiit.R

class CartItemAdapter(
    private val items: MutableList<HotelDatabaseHelper.CartItem>,
    private val dbHelper: HotelDatabaseHelper,
    private val onCartChanged: () -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvProductName: TextView =
            itemView.findViewById(R.id.productname)

        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)

        val tvOriginalPrice: TextView =
            itemView.findViewById(R.id.original_price)

        val tvDiscountPrice: TextView =
            itemView.findViewById(R.id.discounted_price)

        val tvQuantity: TextView =
            itemView.findViewById(R.id.txt_quantity)

        val btnMinus: ImageView =
            itemView.findViewById(R.id.btn_minus)

        val btnPlus: ImageView =
            itemView.findViewById(R.id.btn_plus)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.cart_category_list,
                parent,
                false
            )

        return CartViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CartViewHolder,
        position: Int
    ) {

        val item = items[position]

        // =====================================================
        // PRODUCT NAME + DURATION
        // Example:
        // Swedish Massage • 60 Minutes
        // =====================================================

        holder.tvProductName.text =
            "${item.productName}"

        // =====================================================
        // ORIGINAL PRICE
        // =====================================================

        holder.tvOriginalPrice.text =
            "₹${formatPrice(item.originalPrice)}"

        holder.tvOriginalPrice.paintFlags =
            holder.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        holder.tvOriginalPrice.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                android.R.color.holo_red_dark
            )
        )

        holder.tvDuration.text = "${item.duration} Minutes"

        // =====================================================
        // DISCOUNTED PRICE
        // =====================================================

        holder.tvDiscountPrice.text =
            "₹${formatPrice(item.discountPrice)}"

        // =====================================================
        // QUANTITY
        // =====================================================

        holder.tvQuantity.text =
            item.quantity.toString()


        // =====================================================
        // PLUS BUTTON
        // =====================================================

        holder.btnPlus.setOnClickListener {

            val adapterPosition =
                holder.bindingAdapterPosition

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }

            val currentItem =
                items[adapterPosition]

            val newQuantity =
                currentItem.quantity + 1

            // Update SQLite
            val updated =
                dbHelper.updateServiceQuantity(
                    currentItem.productId,
                    currentItem.type,
                    newQuantity
                )

            if (updated) {

                // Update RecyclerView list
                items[adapterPosition] =
                    currentItem.copy(
                        quantity = newQuantity
                    )

                // Refresh only this item
                notifyItemChanged(adapterPosition)

                // Update total in Activity
                onCartChanged()
            }
        }


        // =====================================================
        // MINUS BUTTON
        // =====================================================

        holder.btnMinus.setOnClickListener {

            val adapterPosition =
                holder.bindingAdapterPosition

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }

            val currentItem =
                items[adapterPosition]

            val newQuantity =
                currentItem.quantity - 1


            // =================================================
            // QUANTITY BECOMES 0
            // DELETE FROM CART
            // =================================================

            if (newQuantity <= 0) {

                val deleted =
                    dbHelper.deleteService(
                        currentItem.productId,
                        currentItem.type
                    )

                if (deleted) {

                    items.removeAt(adapterPosition)

                    notifyItemRemoved(adapterPosition)

                    onCartChanged()
                }

            }

            // =================================================
            // QUANTITY STILL GREATER THAN 0
            // UPDATE DATABASE
            // =================================================

            else {

                val updated =
                    dbHelper.updateServiceQuantity(
                        currentItem.productId,
                        currentItem.type,
                        newQuantity
                    )

                if (updated) {

                    items[adapterPosition] =
                        currentItem.copy(
                            quantity = newQuantity
                        )

                    notifyItemChanged(adapterPosition)

                    onCartChanged()
                }
            }
        }
    }


    // =========================================================
    // ITEM COUNT
    // =========================================================

    override fun getItemCount(): Int {
        return items.size
    }


    // =========================================================
    // PRICE FORMATTER
    // =========================================================

    private fun formatPrice(
        price: Double
    ): String {

        return if (price % 1.0 == 0.0) {

            price.toInt().toString()

        } else {

            String.format(
                "%.2f",
                price
            )
        }
    }
}