package com.company.jifiit.Hotel.Adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.company.jifiit.Hotel.Model.ExpertModel
import com.company.jifiit.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton

class ExpertAdapter(
    private val experts: List<ExpertModel>,
    private val onSelectClick: (ExpertModel) -> Unit
) : RecyclerView.Adapter<ExpertAdapter.ExpertViewHolder>() {

    // =====================================================
    // SELECTED EXPERT ID
    // =====================================================

    private var selectedExpertId: String? = null

    // =====================================================
    // VIEW HOLDER
    // =====================================================

    inner class ExpertViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmerLayout)
        val ivExpert: ImageView =
            itemView.findViewById(R.id.ivExpert)

        val tvName: TextView =
            itemView.findViewById(R.id.tvExpertName)

        val tvEmployeeId: TextView =
            itemView.findViewById(R.id.tvEmployeeId)

        val tvExperience: TextView =
            itemView.findViewById(R.id.tvExperience)

        val btnSelect: MaterialButton =
            itemView.findViewById(R.id.btnSelectExpert)
    }

    // =====================================================
    // CREATE VIEW HOLDER
    // =====================================================

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpertViewHolder {

        val view = LayoutInflater.from(
            parent.context
        ).inflate(
            R.layout.item_expert,
            parent,
            false
        )

        return ExpertViewHolder(view)
    }

    // =====================================================
    // BIND VIEW HOLDER
    // =====================================================

    override fun onBindViewHolder(
        holder: ExpertViewHolder,
        position: Int
    ) {

        val expert = experts[position]

        // =================================================
        // EXPERT INFORMATION
        // =================================================

        holder.tvName.text = expert.name

        holder.tvEmployeeId.text =
            "Employee ID: ${expert.employeeId}"

        holder.tvExperience.text =
            "Experience: ${expert.experience}"

        // =================================================
        // EXPERT IMAGE
        // =================================================
        holder.shimmerLayout.visibility = View.VISIBLE
        holder.shimmerLayout.startShimmer()
        if (expert.image.isNotEmpty()) {

            Glide.with(holder.itemView.context)
                .load(expert.image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.shimmerLayout.stopShimmer()
                        holder.shimmerLayout.visibility = View.GONE
                        holder.ivExpert.visibility = View.VISIBLE
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
                        holder.ivExpert.visibility = View.VISIBLE
                        return false
                    }
                })
                .error(R.mipmap.hotel_body_massage)
                .into(holder.ivExpert)
        }

        // =================================================
        // CHECK SELECTED STATE
        // =================================================

        if (expert.id == selectedExpertId) {

            // -------------------------------------------------
            // SELECTED
            // -------------------------------------------------

            holder.btnSelect.text = "Selected"

            holder.btnSelect.setBackgroundColor(
                Color.parseColor("#1976D2")
            )

            holder.btnSelect.setTextColor(
                Color.WHITE
            )

        } else {

            // -------------------------------------------------
            // NOT SELECTED
            // -------------------------------------------------

            holder.btnSelect.text = "Select"

            holder.btnSelect.setBackgroundColor(
                Color.parseColor("#E3F2FD")
            )

            holder.btnSelect.setTextColor(
                Color.parseColor("#1976D2")
            )
        }

        // =================================================
        // SELECT BUTTON CLICK
        // =================================================

        holder.btnSelect.setOnClickListener {

            // Store selected expert ID
            selectedExpertId = expert.id

            // Refresh all cards
            notifyDataSetChanged()

            // Send selected expert to Activity
            onSelectClick(expert)
        }
    }

    // =====================================================
    // ITEM COUNT
    // =====================================================

    override fun getItemCount(): Int {

        return experts.size
    }
}