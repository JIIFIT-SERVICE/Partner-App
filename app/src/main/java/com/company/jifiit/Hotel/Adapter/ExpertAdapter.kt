package com.company.jifiit.Hotel.Adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat

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

        val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmerLayout)

        val ivExpert: ImageView =
            itemView.findViewById(R.id.ivExpert)

        val tvName: TextView =
            itemView.findViewById(R.id.tvExpertName)

        val tvRole: TextView =
            itemView.findViewById(R.id.tvRole)

        val tvHometown: TextView =
            itemView.findViewById(R.id.tvHometown)

        val tvExperience: TextView =
            itemView.findViewById(R.id.tvExperience)

        val tvLifetimeExp: TextView =
            itemView.findViewById(R.id.tvLifetimeExp)

        val tvAvailability: TextView =
            itemView.findViewById(R.id.tvAvailability)

        val tvRating: TextView =
            itemView.findViewById(R.id.tvRating)

        val btnDetails: MaterialButton =
            itemView.findViewById(R.id.btnDetails)

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
        // BASIC TEXT FIELDS
        // =================================================

        holder.tvName.text = expert.name

        holder.tvRole.text = expert.role

        holder.tvHometown.text = expert.hometown

        holder.tvExperience.text = expert.experience

        holder.tvLifetimeExp.text = expert.lifetimeExp

        holder.tvAvailability.text = expert.availability

        holder.tvRating.text =
            "★ ${expert.rating} (${expert.ratingCount})"

        // =================================================
        // EXPERT IMAGE
        // =================================================

        holder.shimmerLayout.visibility = View.VISIBLE
        holder.shimmerLayout.startShimmer()
        holder.ivExpert.visibility = View.INVISIBLE

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

        } else {

            holder.shimmerLayout.stopShimmer()
            holder.shimmerLayout.visibility = View.GONE
            holder.ivExpert.visibility = View.VISIBLE
            holder.ivExpert.setImageResource(
                R.mipmap.hotel_body_massage
            )
        }

        // =================================================
        // SELECTED / UNSELECTED STATE
        // =================================================

        if (expert.id == selectedExpertId) {

            // ---------------------------------------------
            // SELECTED
            // ---------------------------------------------
            val context = holder.itemView.context
            holder.btnSelect.text = "✓ Selected"

            holder.btnSelect.setBackgroundColor(
                holder.itemView.context.getColor(R.color.BackgroundBlue)
            )

            holder.btnSelect.setTextColor(
                ContextCompat.getColor(context, R.color.Text_Blue)
            )

            holder.btnSelect.strokeColor =
                ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.Button_Blue)
                )

        } else {

            // ---------------------------------------------
            // NOT SELECTED
            // ---------------------------------------------

            holder.btnSelect.text = "Select"

            holder.btnSelect.setBackgroundColor(
                Color.TRANSPARENT
            )

            holder.btnSelect.setTextColor(
                Color.parseColor("#1976D2")
            )

            holder.btnSelect.strokeColor =
                ColorStateList.valueOf(Color.parseColor("#1976D2"))
        }

        // =================================================
        // SELECT BUTTON CLICK
        // =================================================

        holder.btnSelect.setOnClickListener {

            selectedExpertId = expert.id

            notifyDataSetChanged()

            onSelectClick(expert)
        }

        // =================================================
        // DETAILS BUTTON CLICK
        // =================================================

        holder.btnDetails.setOnClickListener {

            // TODO: open details bottom sheet
        }
    }

    // =====================================================
    // ITEM COUNT
    // =====================================================

    override fun getItemCount(): Int {

        return experts.size
    }
}