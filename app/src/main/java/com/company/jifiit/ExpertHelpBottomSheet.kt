package com.company.jifiit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

object ExpertHelpBottomSheet {

    fun show(context: Context) {
        val bottomSheetDialog = BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_expert_help, null)
        bottomSheetDialog.setContentView(view)

        bottomSheetDialog.setOnShowListener { dialogInterface ->
            val dialog = dialogInterface as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        // Close buttons
        view.findViewById<View>(R.id.btnCloseSheet)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        view.findViewById<View>(R.id.btnCloseHelpBottom)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Setup FAQ items
        setupFaqItem(view, R.id.llFaq1, R.id.tvFaqAnswer1, R.id.ivFaqChevron1)
        setupFaqItem(view, R.id.llFaq2, R.id.tvFaqAnswer2, R.id.ivFaqChevron2)
        setupFaqItem(view, R.id.llFaq3, R.id.tvFaqAnswer3, R.id.ivFaqChevron3)
        setupFaqItem(view, R.id.llFaq4, R.id.tvFaqAnswer4, R.id.ivFaqChevron4)
        setupFaqItem(view, R.id.llFaq5, R.id.tvFaqAnswer5, R.id.ivFaqChevron5)

        // Call Helpline
        view.findViewById<View>(R.id.btnCallHelpline)?.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919717550414"))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show()
            }
        }

        // Email Support
        view.findViewById<View>(R.id.btnEmailSupport)?.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:jiifitteam@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Jiifit Expert Onboarding Support")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }

    private fun setupFaqItem(root: View, headerId: Int, answerId: Int, chevronId: Int) {
        val header = root.findViewById<View>(headerId) ?: return
        val answer = root.findViewById<TextView>(answerId) ?: return
        val chevron = root.findViewById<ImageView>(chevronId) ?: return

        header.setOnClickListener {
            val isExpanded = answer.visibility == View.VISIBLE
            if (isExpanded) {
                answer.visibility = View.GONE
                chevron.animate().rotation(0f).setDuration(200).start()
            } else {
                answer.visibility = View.VISIBLE
                chevron.animate().rotation(180f).setDuration(200).start()
            }
        }
    }
}

