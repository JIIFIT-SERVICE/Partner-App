package com.company.jifiit.Hotel.Adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource                              // ✅ Glide's DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target                        // ✅ Glide's Target
import com.company.jifiit.Hotel.Model.ServiceItemModel
import com.company.jifiit.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton

class ServiceCardAdapter(
    private var items: List<ServiceItemModel>,
    private val onAddClick: (ServiceItemModel) -> Unit
) : RecyclerView.Adapter<ServiceCardAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val shimmerLayout: ShimmerFrameLayout = view.findViewById(R.id.shimmerLayout)
        val ivService: ImageView = view.findViewById(R.id.ivService)
        val tvSaveBadge: TextView = view.findViewById(R.id.tvSaveBadge)
        val tvOptionsBadge: TextView = view.findViewById(R.id.tvOptionsBadge)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvOriginalPrice: TextView = view.findViewById(R.id.tvOriginalPrice)
        val tvDiscount: TextView = view.findViewById(R.id.tvDiscount)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val btnAdd: MaterialButton = view.findViewById(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.tvSaveBadge.text = item.saveBadge
        holder.tvOptionsBadge.text = "${item.optionsCount} Options"
        holder.tvTitle.text = item.title
        holder.tvPrice.text = item.price
        holder.tvOriginalPrice.text = item.originalPrice
        holder.tvDiscount.text = item.discountPercent
        holder.tvDuration.text = item.duration

        // ✅ Shimmer start
        holder.shimmerLayout.visibility = View.VISIBLE
        holder.shimmerLayout.startShimmer()
        holder.ivService.visibility = View.INVISIBLE

        Glide.with(holder.ivService.context)
            .load(item.image)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.shimmerLayout.stopShimmer()
                    holder.shimmerLayout.visibility = View.GONE
                    holder.ivService.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.shimmerLayout.stopShimmer()
                    holder.shimmerLayout.visibility = View.GONE
                    holder.ivService.visibility = View.VISIBLE
                    return false
                }
            })
            .error(R.mipmap.hotel_body_massage)
            .into(holder.ivService)

        holder.tvOriginalPrice.apply {
            paintFlags = paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            setTextColor(android.graphics.Color.RED)
        }

        holder.btnAdd.setOnClickListener { onAddClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<ServiceItemModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}