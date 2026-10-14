package com.company.jifiit.Hotel

import android.app.Activity
import android.content.Intent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.company.jifiit.R
import com.google.android.material.navigation.NavigationView

object HotelHeaderHelper {

    fun setupHeader(
        activity: Activity,
        drawerLayout: DrawerLayout? = null,
        navView: NavigationView? = null,
        currentNavId: Int = 0
    ) {
        // Set blue status bar with white icons matching brand blue header
        try {
            activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.hotel_primary)
            WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = true
            }
        } catch (_: Exception) {}

        val branch = HotelBranchManager.getInstance(activity).getSelectedBranch()
        val tvName = activity.findViewById<TextView>(R.id.hotelName)
        val tvSubtitle = activity.findViewById<TextView>(R.id.hotelSubtitle)
        val btnMenu = activity.findViewById<ImageView>(R.id.btnMenu)
        val btnNotification = activity.findViewById<FrameLayout>(R.id.btnNotification)

        tvName?.text = branch.name
        tvSubtitle?.text = "${branch.locality} • ${branch.branchTag}"

        btnMenu?.setOnClickListener {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                activity.finish()
            }
        }

        btnNotification?.setOnClickListener {
            Toast.makeText(
                activity,
                "No new unread notifications for ${branch.name}",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (drawerLayout != null && navView != null) {
            val headerView = navView.getHeaderView(0)
            headerView?.findViewById<TextView>(R.id.tvHotelName)?.text = branch.name
            headerView?.findViewById<TextView>(R.id.tvHotelPartnerId)?.text = branch.partnerId
            headerView?.findViewById<TextView>(R.id.tvSelectedBranchBadge)?.text =
                "${branch.locality} (${branch.branchTag})"
            headerView?.findViewById<TextView>(R.id.tvHotelProfileInitials)?.text = branch.initials

            if (currentNavId != 0) {
                navView.setCheckedItem(currentNavId)
            }

            navView.setNavigationItemSelectedListener { item ->
                drawerLayout.closeDrawer(GravityCompat.START)
                if (item.itemId == currentNavId) {
                    return@setNavigationItemSelectedListener true
                }

                when (item.itemId) {
                    R.id.nav_overview -> {
                        val intent = Intent(activity, HotelMainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        activity.startActivity(intent)
                    }
                    R.id.nav_rooms -> {
                        if (activity !is ManageRoomsActivity) {
                            activity.startActivity(Intent(activity, ManageRoomsActivity::class.java))
                        }
                    }
                    R.id.nav_history -> {
                        if (activity !is HotelBookingHistoryActivity) {
                            activity.startActivity(Intent(activity, HotelBookingHistoryActivity::class.java))
                        }
                    }
                    R.id.nav_earnings -> {
                        if (activity !is HotelEarningsActivity) {
                            activity.startActivity(Intent(activity, HotelEarningsActivity::class.java))
                        }
                    }
                    R.id.nav_reports -> {
                        if (activity !is HotelReportsActivity) {
                            activity.startActivity(Intent(activity, HotelReportsActivity::class.java))
                        }
                    }
                    R.id.nav_help -> {
                        Toast.makeText(activity, "Help & Support", Toast.LENGTH_SHORT).show()
                    }
                    R.id.nav_terms -> {
                        Toast.makeText(activity, "Terms & Conditions", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
        }
    }
}
