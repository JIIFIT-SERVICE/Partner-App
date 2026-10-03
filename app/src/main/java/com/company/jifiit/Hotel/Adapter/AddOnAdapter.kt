package com.company.jifiit.Hotel.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.company.jifiit.Hotel.Model.AddOnModel
import com.company.jifiit.Hotel.ServiceDetailsActivity
import com.company.jifiit.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class AddOnAdapter(
    private val context: Context,
    private val items: List<AddOnModel>,
    private val service: String,
    private val onAddClick: (AddOnModel) -> Unit
) : RecyclerView.Adapter<AddOnAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {

        val ivService: ImageView =
            v.findViewById(R.id.ivService)

        val tvTitle: TextView =
            v.findViewById(R.id.tvTitle)

        val tvSaveBadge: TextView =
            v.findViewById(R.id.tvSaveBadge)

        val tvOptionsBadge: TextView =
            v.findViewById(R.id.tvOptionsBadge)

        val tvPrice: TextView =
            v.findViewById(R.id.tvPrice)

        val tvOriginalPrice: TextView =
            v.findViewById(R.id.tvOriginalPrice)

        val tvDiscount: TextView =
            v.findViewById(R.id.tvDiscount)

        val tvDuration: TextView =
            v.findViewById(R.id.tvDuration)

        val tvHotelTag: TextView =
            v.findViewById(R.id.tvHotelTag)

        val btnAdd: MaterialButton =
            v.findViewById(R.id.btnAdd)

        val shimmerLayout: ShimmerFrameLayout =
            v.findViewById(R.id.shimmerLayout)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_addon_service,
                parent,
                false
            )

        return VH(view)
    }

    override fun onBindViewHolder(
        h: VH,
        position: Int
    ) {

        val item = items[position]

        // =====================================================
        // TEXT
        // =====================================================

        h.tvTitle.text = item.title

        h.tvPrice.text = item.price

        h.tvOriginalPrice.text = item.originalPrice

        h.tvDiscount.text = item.discountPercent

        h.tvDuration.text =
            if (item.duration.isNotEmpty()) {
                "${item.duration}m"
            } else {
                ""
            }


        // =====================================================
        // SAVE BADGE
        // =====================================================

        if (item.saveBadge.isNotEmpty()) {

            h.tvSaveBadge.visibility = View.VISIBLE

            h.tvSaveBadge.text = item.saveBadge

        } else {

            h.tvSaveBadge.visibility = View.GONE
        }


        // =====================================================
        // OPTIONS BADGE
        // =====================================================

        if (item.optionsCount > 1) {

            h.tvOptionsBadge.visibility = View.VISIBLE

            h.tvOptionsBadge.text =
                "${item.optionsCount} Options"

        } else {

            h.tvOptionsBadge.visibility = View.GONE
        }


        // =====================================================
        // HOTEL TAG
        // =====================================================

        if (item.type.equals("hotel", ignoreCase = true)) {

            h.tvHotelTag.visibility = View.VISIBLE

            h.tvHotelTag.text = "Hotel"

        } else {

            h.tvHotelTag.visibility = View.GONE
        }


        // =====================================================
        // IMAGE
        // =====================================================

        // =====================================================
// IMAGE + SHIMMER
// =====================================================

// Reset view state because RecyclerView reuses ViewHolder
        h.ivService.visibility = View.INVISIBLE

        h.shimmerLayout.visibility = View.VISIBLE
        h.shimmerLayout.startShimmer()

        if (item.image.isNotEmpty()) {

            Glide.with(h.ivService.context)
                .load(item.image)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {

                        h.shimmerLayout.stopShimmer()
                        h.shimmerLayout.visibility = View.GONE

                        h.ivService.visibility = View.VISIBLE

                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {

                        h.shimmerLayout.stopShimmer()
                        h.shimmerLayout.visibility = View.GONE

                        h.ivService.visibility = View.VISIBLE

                        return false
                    }
                })
                .error(R.mipmap.hotel_body_massage)
                .into(h.ivService)

        } else {

            // No image URL → show fallback immediately

            h.shimmerLayout.stopShimmer()
            h.shimmerLayout.visibility = View.GONE

            h.ivService.visibility = View.VISIBLE

            h.ivService.setImageResource(
                R.mipmap.hotel_body_massage
            )
        }


        // =====================================================
        // ADD BUTTON
        // =====================================================

        h.btnAdd.setOnClickListener {

            // First execute callback
            onAddClick(item)

            // Then open ServiceDetailsActivity
            val intent = Intent(
                context,
                ServiceDetailsActivity::class.java
            ).apply {

                putExtra(
                    "SERVICE_ID",
                    item.id
                )

                putExtra(
                    "SERVICE_TYPE",
                    item.type
                )

                putExtra(
                    "SERVICE_TITLE",
                    item.title
                )

                putExtra(
                    "SELECTED_SERVICE",
                    service
                )
            }

            context.startActivity(intent)

            // Slide animation
            if (context is Activity) {

                context.overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}