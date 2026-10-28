package com.company.jifiit.Influencer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.company.jifiit.Constant
import com.company.jifiit.LoginActivity
import com.company.jifiit.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.hbb20.CountryCodePicker
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class InfluencerRegistartion : AppCompatActivity() {

    private lateinit var headerContainer: LinearLayout
    private lateinit var btnClose: ImageView
    private lateinit var fixedFooterContainer: LinearLayout
    private lateinit var layoutFaqContainer: LinearLayout
    private lateinit var btnSupportEmail: View

    // Categories
    private lateinit var cardGymPartner: LinearLayout
    private lateinit var cardYoutuber: LinearLayout
    private lateinit var cardInstagram: LinearLayout
    private lateinit var cardFacebook: LinearLayout
    private lateinit var cardNormalPerson: LinearLayout
    private lateinit var cardBrandOrg: LinearLayout

    private lateinit var tvGymTitle: TextView
    private lateinit var tvYoutuberTitle: TextView
    private lateinit var tvInstagramTitle: TextView
    private lateinit var tvFacebookTitle: TextView
    private lateinit var tvNormalTitle: TextView
    private lateinit var tvBrandTitle: TextView

    // Profile photo upload views
    private lateinit var layoutPhotoUpload: LinearLayout
    private lateinit var ivProfilePhoto: CircleImageView
    private lateinit var tvPhotoTitle: TextView
    private lateinit var tvPhotoSubtitle: TextView
    private lateinit var btnUploadPhoto: MaterialButton

    private var selectedPhotoUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onPhotoSelected(it) }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            cameraImageUri?.let { onPhotoSelected(it) }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            takePhotoWithCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
        }
    }

    // Form fields
    private lateinit var etFullName: EditText
    private lateinit var countryCodePicker: CountryCodePicker
    private lateinit var etMobileNumber: EditText
    private lateinit var etEmail: EditText
    private lateinit var etChannelUrl: EditText
    private lateinit var btnContinue: MaterialButton
    private lateinit var cardTermsConditionsLink: LinearLayout
    private lateinit var tvTermsConditionsApply: TextView

    private var selectedCategory: String = "Gym Partner"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_influencer_registartion)

        // Configure Status Bar for dark gradient header
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.isAppearanceLightNavigationBars = true

        initializeViews()
        applyWindowInsets()
        setupCategorySelection()
        setupActionListeners()
    }

    private fun initializeViews() {
        headerContainer = findViewById(R.id.headerContainer)
        btnClose = findViewById(R.id.btnClose)

        cardGymPartner = findViewById(R.id.cardGymPartner)
        cardYoutuber = findViewById(R.id.cardYoutuber)
        cardInstagram = findViewById(R.id.cardInstagram)
        cardFacebook = findViewById(R.id.cardFacebook)
        cardNormalPerson = findViewById(R.id.cardNormalPerson)
        cardBrandOrg = findViewById(R.id.cardBrandOrg)

        tvGymTitle = findViewById(R.id.tvGymTitle)
        tvYoutuberTitle = findViewById(R.id.tvYoutuberTitle)
        tvInstagramTitle = findViewById(R.id.tvInstagramTitle)
        tvFacebookTitle = findViewById(R.id.tvFacebookTitle)
        tvNormalTitle = findViewById(R.id.tvNormalTitle)
        tvBrandTitle = findViewById(R.id.tvBrandTitle)

        layoutPhotoUpload = findViewById(R.id.layoutPhotoUpload)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)
        tvPhotoTitle = findViewById(R.id.tvPhotoTitle)
        tvPhotoSubtitle = findViewById(R.id.tvPhotoSubtitle)
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto)

        etFullName = findViewById(R.id.etFullName)
        countryCodePicker = findViewById(R.id.countryCodePicker)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etEmail = findViewById(R.id.etEmail)
        etChannelUrl = findViewById(R.id.etChannelUrl)
        btnContinue = findViewById(R.id.btnContinue)
        fixedFooterContainer = findViewById(R.id.fixedFooterContainer)
        layoutFaqContainer = findViewById(R.id.layoutFaqContainer)
        btnSupportEmail = findViewById(R.id.btnSupportEmail)
        cardTermsConditionsLink = findViewById(R.id.cardTermsConditionsLink)
        tvTermsConditionsApply = findViewById(R.id.tvTermsConditionsApply)

        setupPhotoUpload()
        setupCountryCodePicker()
        setupFaqSection()
        setupSupportEmail()
    }

    private fun setupCountryCodePicker() {
        countryCodePicker.setDialogBackgroundColor(Color.WHITE)
        countryCodePicker.setDialogTextColor(Color.BLACK)
        countryCodePicker.setDefaultCountryUsingNameCode("IN")
        countryCodePicker.resetToDefaultCountry()
        countryCodePicker.registerCarrierNumberEditText(etMobileNumber)

        countryCodePicker.setOnCountryChangeListener {
            etMobileNumber.error = null
        }
    }

    private fun setupPhotoUpload() {
        val openPickerListener = View.OnClickListener {
            showImagePickerBottomSheet()
        }
        layoutPhotoUpload.setOnClickListener(openPickerListener)
        ivProfilePhoto.setOnClickListener(openPickerListener)
        btnUploadPhoto.setOnClickListener(openPickerListener)
    }

    private fun showImagePickerBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_influencer_profile_picker, null)
        bottomSheetDialog.setContentView(sheetView)

        val optCamera = sheetView.findViewById<View>(R.id.layoutOptionCamera)
        val optGallery = sheetView.findViewById<View>(R.id.layoutOptionGallery)
        val optRemove = sheetView.findViewById<View>(R.id.layoutOptionRemove)
        val btnClose = sheetView.findViewById<ImageView>(R.id.btnCloseSheet)

        optRemove?.visibility = if (selectedPhotoUri != null) View.VISIBLE else View.GONE

        optCamera?.setOnClickListener {
            bottomSheetDialog.dismiss()
            checkCameraPermissionAndLaunch()
        }

        optGallery?.setOnClickListener {
            bottomSheetDialog.dismiss()
            galleryLauncher.launch("image/*")
        }

        optRemove?.setOnClickListener {
            bottomSheetDialog.dismiss()
            removePhoto()
        }

        btnClose?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            takePhotoWithCamera()
        }
    }

    private fun takePhotoWithCamera() {
        try {
            val photoFile = File(cacheDir, "influencer_avatar_${System.currentTimeMillis()}.jpg")
            val photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
            cameraImageUri = photoUri
            cameraLauncher.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onPhotoSelected(uri: Uri) {
        selectedPhotoUri = uri
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_avatar_placeholder)
            .error(R.drawable.ic_avatar_placeholder)
            .into(ivProfilePhoto)

        tvPhotoTitle.text = "Profile Photo Added"
        tvPhotoSubtitle.text = "Tap to change or remove photo"
        btnUploadPhoto.text = "Change"
        Toast.makeText(this, "Photo selected successfully", Toast.LENGTH_SHORT).show()
    }

    private fun removePhoto() {
        selectedPhotoUri = null
        ivProfilePhoto.setImageResource(R.drawable.ic_avatar_placeholder)
        tvPhotoTitle.text = "Upload Profile Photo"
        tvPhotoSubtitle.text = "Tap to take a photo or choose from gallery"
        btnUploadPhoto.text = "Upload"
        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show()
    }

    private fun setupSupportEmail() {
        btnSupportEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:jiifitteam@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Support Request — Influencer & Partner Program")
            }
            try {
                startActivity(Intent.createChooser(intent, "Send Email via..."))
            } catch (e: Exception) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFaqSection() {
        val faqs = listOf(
            "What is the Influencer & Partner Program?" to
                    "It is a referral program that allows you to share our wellness services with others and earn real cash for successful referrals.",
            "How can I become a partner?" to
                    "Register for the Influencer & Partner Program. Once registered, you will receive your unique Referral ID and referral link.",
            "How does the referral program work?" to
                    "Share your referral link and Referral ID with friends, family, and your audience. When a new user registers using your Referral ID and completes their first eligible booking, you earn ₹50 in real cash.",
            "What will my referred friend receive?" to
                    "Your referred friend will receive ₹4,999 in wallet balance after successfully registering with your valid Referral ID, subject to program verification.",
            "Is the ₹4,999 wallet balance real cash?" to
                    "No. The ₹4,999 is promotional wallet balance that can be used for eligible wellness services. It cannot be withdrawn as cash.",
            "Does the wallet balance expire?" to
                    "No. The ₹4,999 wallet balance does not expire, subject to the applicable wallet terms and conditions.",
            "When will I earn ₹50?" to
                    "You will earn ₹50 in real cash after a referred user completes their first eligible booking and the booking is verified. Registration or downloading the app alone does not qualify.",
            "How will I receive my earnings?" to
                    "Approved referral earnings will be paid to your verified bank account or UPI ID, subject to the program’s payout and verification process.",
            "Can I use my own Referral ID to refer myself?" to
                    "No. Self-referrals are not allowed. Referrals must be genuine and comply with the program’s terms and conditions.",
            "What should I do if I do not receive my referral reward?" to
                    "Please contact our support team using the email address provided in the Help section of the app. Include your registered account details and relevant referral information so our team can review the issue.",
            "Where can I get help with my partner account?" to
                    "For assistance with your account, Referral ID, wallet balance, or referral earnings, email our support team using the contact details listed in the app footer."
        )

        layoutFaqContainer.removeAllViews()

        faqs.forEachIndexed { index, (question, answer) ->
            val itemView = layoutInflater.inflate(R.layout.item_faq, layoutFaqContainer, false)
            val faqHeader = itemView.findViewById<View>(R.id.faqHeader)
            val tvQuestion = itemView.findViewById<TextView>(R.id.tvQuestion)
            val tvAnswer = itemView.findViewById<TextView>(R.id.tvAnswer)
            val ivArrow = itemView.findViewById<ImageView>(R.id.ivArrow)

            tvQuestion.text = "Q${index + 1}. $question"
            tvQuestion.textSize = 14.5f
            tvQuestion.setTypeface(null, android.graphics.Typeface.BOLD)

            tvAnswer.text = "Answer: $answer"
            tvAnswer.textSize = 13f
            tvAnswer.setTextColor(Color.parseColor("#475569"))
            tvAnswer.visibility = View.GONE

            faqHeader.setOnClickListener {
                val isExpanded = tvAnswer.visibility == View.VISIBLE
                if (layoutFaqContainer.parent is android.view.ViewGroup) {
                    android.transition.TransitionManager.beginDelayedTransition(layoutFaqContainer)
                }
                tvAnswer.visibility = if (isExpanded) View.GONE else View.VISIBLE
                ivArrow.animate().rotation(if (isExpanded) 0f else 180f).setDuration(200).start()
            }

            layoutFaqContainer.addView(itemView)
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerContainer.setPadding(
                headerContainer.paddingStart,
                systemBars.top + 16,
                headerContainer.paddingEnd,
                headerContainer.paddingBottom
            )
            fixedFooterContainer.setPadding(
                fixedFooterContainer.paddingStart,
                fixedFooterContainer.paddingTop,
                fixedFooterContainer.paddingEnd,
                systemBars.bottom + 12
            )
            insets
        }
    }

    private fun setupCategorySelection() {
        val selectedBg = R.drawable.bg_partner_category_selected
        val unselectedBg = R.drawable.bg_partner_category_unselected

        val selectedColor = Color.parseColor("#0D62FE")
        val unselectedColor = Color.parseColor("#1E293B")

        fun selectCategory(category: String) {
            selectedCategory = category

            // Reset all
            cardGymPartner.setBackgroundResource(unselectedBg)
            cardYoutuber.setBackgroundResource(unselectedBg)
            cardInstagram.setBackgroundResource(unselectedBg)
            cardFacebook.setBackgroundResource(unselectedBg)
            cardNormalPerson.setBackgroundResource(unselectedBg)
            cardBrandOrg.setBackgroundResource(unselectedBg)

            tvGymTitle.setTextColor(unselectedColor)
            tvYoutuberTitle.setTextColor(unselectedColor)
            tvInstagramTitle.setTextColor(unselectedColor)
            tvFacebookTitle.setTextColor(unselectedColor)
            tvNormalTitle.setTextColor(unselectedColor)
            tvBrandTitle.setTextColor(unselectedColor)

            // Activate selected
            when (category) {
                "Gym Partner" -> {
                    cardGymPartner.setBackgroundResource(selectedBg)
                    tvGymTitle.setTextColor(selectedColor)
                }
                "YouTuber" -> {
                    cardYoutuber.setBackgroundResource(selectedBg)
                    tvYoutuberTitle.setTextColor(selectedColor)
                }
                "Instagram Creator" -> {
                    cardInstagram.setBackgroundResource(selectedBg)
                    tvInstagramTitle.setTextColor(selectedColor)
                }
                "Facebook" -> {
                    cardFacebook.setBackgroundResource(selectedBg)
                    tvFacebookTitle.setTextColor(selectedColor)
                }
                "Normal Person" -> {
                    cardNormalPerson.setBackgroundResource(selectedBg)
                    tvNormalTitle.setTextColor(selectedColor)
                }
                "Brand / Organization" -> {
                    cardBrandOrg.setBackgroundResource(selectedBg)
                    tvBrandTitle.setTextColor(selectedColor)
                }
            }
        }

        cardGymPartner.setOnClickListener { selectCategory("Gym Partner") }
        cardYoutuber.setOnClickListener { selectCategory("YouTuber") }
        cardInstagram.setOnClickListener { selectCategory("Instagram Creator") }
        cardFacebook.setOnClickListener { selectCategory("Facebook") }
        cardNormalPerson.setOnClickListener { selectCategory("Normal Person") }
        cardBrandOrg.setOnClickListener { selectCategory("Brand / Organization") }
    }

    private var loadingDialog: androidx.appcompat.app.AlertDialog? = null
    private var tvLoadingMessage: TextView? = null

    private fun showLoadingDialog(message: String) {
        if (loadingDialog == null) {
            val linearLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(60, 45, 60, 45)
                gravity = android.view.Gravity.CENTER_VERTICAL
                setBackgroundColor(Color.WHITE)
                val progressBar = android.widget.ProgressBar(this@InfluencerRegistartion).apply {
                    isIndeterminate = true
                }
                val tv = TextView(this@InfluencerRegistartion).apply {
                    text = message
                    textSize = 15f
                    setTextColor(Color.parseColor("#1E293B"))
                    setPadding(40, 0, 0, 0)
                }
                tvLoadingMessage = tv
                addView(progressBar)
                addView(tv)
            }
            loadingDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(linearLayout)
                .setCancelable(false)
                .create()
        }
        tvLoadingMessage?.text = message
        if (!isFinishing && loadingDialog?.isShowing == false) {
            loadingDialog?.show()
        }
    }

    private fun dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val extension = when (contentResolver.getType(uri)) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val tempFile = File(cacheDir, "temp_profile_${System.currentTimeMillis()}.$extension")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Submits partner registration data to the backend API.
     * Required parameters:
     * - partner_category
     * - full_name
     * - country_code
     * - mobile_number
     * - email
     * - channel_url
     * - profile_image_url
     */
    fun submitInfluencerData(
        partnerCategory: String = selectedCategory,
        fullName: String = etFullName.text.toString().trim(),
        countryCode: String = "+" + countryCodePicker.selectedCountryCode,
        mobileNumber: String = etMobileNumber.text.toString().trim(),
        email: String = etEmail.text.toString().trim(),
        channelUrl: String = etChannelUrl.text.toString().trim(),
        profileImageUrl: String = selectedPhotoUri?.toString() ?: ""
    ) {
        showLoadingDialog("Submitting registration...")
        btnContinue.isEnabled = false

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("partner_category", partnerCategory)
            .addFormDataPart("full_name", fullName)
            .addFormDataPart("country_code", countryCode)
            .addFormDataPart("mobile_number", mobileNumber)
            .addFormDataPart("email", email)
            .addFormDataPart("channel_url", channelUrl)
            .addFormDataPart("profile_image_url", profileImageUrl)

        // Attach local photo file if selected
        selectedPhotoUri?.let { uri ->
            val file = getFileFromUri(uri)
            if (file != null && file.exists()) {
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val mediaType = mimeType.toMediaTypeOrNull()
                builder.addFormDataPart(
                    "profile_image",
                    file.name,
                    file.asRequestBody(mediaType)
                )
            }
        }

        val requestBody = builder.build()
        val url = Constant.InfluencerRegisterUrl

        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .post(requestBody)
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    dismissLoadingDialog()
                    btnContinue.isEnabled = true
                    Toast.makeText(
                        this@InfluencerRegistartion,
                        "Failed to submit: ${e.message ?: "Network error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string().orEmpty()
                runOnUiThread {
                    dismissLoadingDialog()
                    btnContinue.isEnabled = true

                    try {
                        val json = JSONObject(body)
                        val isSuccess = json.optBoolean("status", false) ||
                                json.optString("status").equals("success", ignoreCase = true) ||
                                json.optString("status").equals("true", ignoreCase = true) ||
                                (response.isSuccessful && json.optBoolean("success", false))

                        val message = json.optString(
                            "message",
                            if (isSuccess) "Influencer registration submitted successfully" else "Registration failed"
                        )

                        if (isSuccess) {
                            Toast.makeText(this@InfluencerRegistartion, message, Toast.LENGTH_LONG).show()
                            val intent = Intent(this@InfluencerRegistartion, LoginActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            startActivity(intent)
                            finish()
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        } else {
                            Toast.makeText(this@InfluencerRegistartion, message, Toast.LENGTH_LONG).show()
                            if (message.contains("mobile", ignoreCase = true)) {
                                etMobileNumber.error = message
                                etMobileNumber.requestFocus()
                            } else if (message.contains("email", ignoreCase = true)) {
                                etEmail.error = message
                                etEmail.requestFocus()
                            }
                        }
                    } catch (e: Exception) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@InfluencerRegistartion, "Influencer registration submitted successfully", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@InfluencerRegistartion, LoginActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            startActivity(intent)
                            finish()
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        } else {
                            val errMsg = if (body.isNotBlank() && body.length < 150) body else "Server error (${response.code})"
                            Toast.makeText(this@InfluencerRegistartion, errMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }

    private fun setupActionListeners() {
        val openTerms = {
            val intent = Intent(this, InfluencerTermsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        val openPrivacy = {
            val intent = Intent(this, InfluencerPrivacyPolicyActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        cardTermsConditionsLink.setOnClickListener { openTerms() }

        // Configure clickable Terms & Conditions and Privacy Policy links
        val termsFullText = tvTermsConditionsApply.text.toString()
        val spannable = SpannableString(termsFullText)

        // 1. "Terms & Conditions" Clickable Span
        val termsTarget = "Terms & Conditions"
        val termsStart = termsFullText.indexOf(termsTarget)
        if (termsStart != -1) {
            val termsEnd = termsStart + termsTarget.length
            val termsClickable = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openTerms()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = Color.parseColor("#0D62FE")
                    ds.isUnderlineText = true
                    ds.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            }
            spannable.setSpan(termsClickable, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // 2. "Privacy Policy" Clickable Span
        val privacyTarget = "Privacy Policy"
        val privacyStart = termsFullText.indexOf(privacyTarget)
        if (privacyStart != -1) {
            val privacyEnd = privacyStart + privacyTarget.length
            val privacyClickable = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openPrivacy()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = Color.parseColor("#0D62FE")
                    ds.isUnderlineText = true
                    ds.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            }
            spannable.setSpan(privacyClickable, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        tvTermsConditionsApply.text = spannable
        tvTermsConditionsApply.movementMethod = LinkMovementMethod.getInstance()
        tvTermsConditionsApply.highlightColor = Color.TRANSPARENT

        btnClose.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        btnContinue.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val mobileNumber = etMobileNumber.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val channelUrl = etChannelUrl.text.toString().trim()

            if (fullName.isEmpty()) {
                etFullName.error = "Please enter your name or legal entity name"
                etFullName.requestFocus()
                return@setOnClickListener
            }

            if (mobileNumber.isEmpty()) {
                etMobileNumber.error = "Please enter your mobile number"
                etMobileNumber.requestFocus()
                return@setOnClickListener
            }

            if (!countryCodePicker.isValidFullNumber) {
                etMobileNumber.error = "Please enter a valid mobile number for ${countryCodePicker.selectedCountryName}"
                etMobileNumber.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Please enter your email ID"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Please enter a valid email address"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            val countryCode = "+" + countryCodePicker.selectedCountryCode
            val profileImageUrl = selectedPhotoUri?.toString() ?: ""

            // Submit data to backend
            submitInfluencerData(
                partnerCategory = selectedCategory,
                fullName = fullName,
                countryCode = countryCode,
                mobileNumber = mobileNumber,
                email = email,
                channelUrl = channelUrl,
                profileImageUrl = profileImageUrl
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}