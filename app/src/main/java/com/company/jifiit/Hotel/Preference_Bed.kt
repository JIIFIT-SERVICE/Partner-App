package com.company.jifiit.Hotel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Preference_Bed : AppCompatActivity() {

    // =====================================================
    // VIEWS
    // =====================================================

    private lateinit var btnBack: ImageView

    // Guest data
    private lateinit var guestName: String
    private lateinit var guestMobile: String
    private lateinit var country_Code: String
    private lateinit var Mobile: String

    // Room data
    private lateinit var roomId: String
    private lateinit var roomNumber: String
    private lateinit var floorWing: String

    // Therapist
    private lateinit var cardMaleTherapist: MaterialCardView
    private lateinit var cardFemaleTherapist: MaterialCardView
    private var expertType: String = ""

    // Checkbox-style cards
    private lateinit var cbDoorbell: MaterialCardView
    private lateinit var cbDoNotCall: MaterialCardView
    private lateinit var cbCallConcierge: MaterialCardView

    private lateinit var ivDoorbellCheck: ImageView
    private lateinit var ivDoNotCallCheck: ImageView
    private lateinit var ivCallConciergeCheck: ImageView

    // State flags
    private var ringDoorbell: Boolean = false
    private var doNotCallBeforeRoom: Boolean = false
    private var callConcierge: Boolean = false

    // Special note
    private lateinit var etSpecialNote: EditText
    private lateinit var ivVoiceNote: ImageView

    // Continue
    private lateinit var btnContinue: MaterialButton

    private lateinit var service: String


    // =====================================================
    // VOICE RECORDING UI
    // =====================================================

    private lateinit var layoutRecording: LinearLayout
    private lateinit var layoutVoiceAttached: LinearLayout
    private lateinit var viewRecDot: View
    private lateinit var tvRecordingTimer: TextView
    private lateinit var ivStopRecording: ImageView
    private lateinit var ivPlayVoice: ImageView
    private lateinit var ivDeleteVoice: ImageView
    private lateinit var tvVoiceDuration: TextView


    // =====================================================
    // VOICE RECORDING STATE
    // =====================================================

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    private var voiceNoteFile: File? = null

    // Playback
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying: Boolean = false


    // =====================================================
    // HANDLERS
    // =====================================================

    private val timerHandler = Handler(Looper.getMainLooper())
    private val blinkHandler = Handler(Looper.getMainLooper())
    private val timerTickHandler = Handler(Looper.getMainLooper())

    private val maxRecordDurationMs = 60_000L


    // =====================================================
    // BLINKING DOT
    // =====================================================

    private var blinkOn = true
    private val blinkRunnable = object : Runnable {
        override fun run() {
            blinkOn = !blinkOn
            viewRecDot.alpha = if (blinkOn) 1f else 0.2f
            blinkHandler.postDelayed(this, 500)
        }
    }

    private fun startBlinkingDot() {
        blinkOn = true
        viewRecDot.alpha = 1f
        blinkHandler.post(blinkRunnable)
    }

    private fun stopBlinkingDot() {
        blinkHandler.removeCallbacks(blinkRunnable)
        viewRecDot.alpha = 1f
    }


    // =====================================================
    // RECORDING TIMER
    // =====================================================

    private var recordingStartTime: Long = 0
    private var recordingDurationMs: Long = 0

    private val timerTickRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - recordingStartTime
            tvRecordingTimer.text = formatDuration(elapsed)
            timerTickHandler.postDelayed(this, 250)
        }
    }

    private fun startRecordingTimer() {
        recordingStartTime = System.currentTimeMillis()
        timerTickHandler.post(timerTickRunnable)
    }

    private fun stopRecordingTimer() {
        timerTickHandler.removeCallbacks(timerTickRunnable)
        recordingDurationMs = System.currentTimeMillis() - recordingStartTime
    }

    private fun formatDuration(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format(Locale.getDefault(), "%02d:%02d", min, sec)
    }


    // =====================================================
    // AUTO STOP RUNNABLE
    // =====================================================

    private val stopRecordingRunnable = Runnable {
        if (isRecording) {
            stopRecordingAndSave()
            Toast.makeText(
                this,
                "Maximum 60 seconds reached — recording stopped.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContentView(R.layout.activity_preference_bed)

        // -------------------------------------------------
        // Insets
        // -------------------------------------------------
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime =
                insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, ime.bottom)
            )

            insets
        }

        service = intent.getStringExtra("SELECTED_SERVICE") ?: ""

        // =====================================================
        // VIEW INITIALIZATION
        // =====================================================

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        cardMaleTherapist = findViewById(R.id.cardMaleTherapist)
        cardFemaleTherapist = findViewById(R.id.cardFemaleTherapist)

        cbDoorbell = findViewById(R.id.cbDoorbell)
        cbDoNotCall = findViewById(R.id.cbDoNotCall)
        cbCallConcierge = findViewById(R.id.cbCallConcierge)

        ivDoorbellCheck = findViewById(R.id.ivDoorbellCheck)
        ivDoNotCallCheck = findViewById(R.id.ivDoNotCallCheck)
        ivCallConciergeCheck = findViewById(R.id.ivCallConciergeCheck)

        etSpecialNote = findViewById(R.id.etSpecialNote)
        ivVoiceNote = findViewById(R.id.ivVoiceNote)

        btnContinue = findViewById(R.id.btnContinue)


        // Recording UI

        layoutRecording     = findViewById(R.id.layoutRecording)
        layoutVoiceAttached = findViewById(R.id.layoutVoiceAttached)
        viewRecDot          = findViewById(R.id.viewRecDot)
        tvRecordingTimer    = findViewById(R.id.tvRecordingTimer)
        ivStopRecording     = findViewById(R.id.ivStopRecording)
        ivPlayVoice         = findViewById(R.id.ivPlayVoice)
        ivDeleteVoice       = findViewById(R.id.ivDeleteVoice)
        tvVoiceDuration     = findViewById(R.id.tvVoiceDuration)


        // =====================================================
        // GET DATA FROM PREVIOUS ACTIVITY
        // =====================================================

        guestName = intent.getStringExtra("guest_name").orEmpty()
        guestMobile = intent.getStringExtra("guest_mobile_full").orEmpty()
        country_Code  = intent.getStringExtra("country_code").orEmpty()
        Mobile = intent.getStringExtra("guest_mobile").orEmpty()

        roomId = intent.getStringExtra("room_id").orEmpty()
        roomNumber = intent.getStringExtra("room_number").orEmpty()
        floorWing = intent.getStringExtra("floor_wing").orEmpty()


        // =====================================================
        // APPLY INITIAL STATE TO UI
        // =====================================================

        updateTherapistSelection()

        updateCheckbox(cbDoorbell, ivDoorbellCheck, ringDoorbell)
        updateCheckbox(cbDoNotCall, ivDoNotCallCheck, doNotCallBeforeRoom)
        updateCheckbox(cbCallConcierge, ivCallConciergeCheck, callConcierge)


        // =====================================================
        // THERAPIST CARD CLICK
        // =====================================================

        cardMaleTherapist.setOnClickListener {
            expertType = "male"
            updateTherapistSelection()
        }

        cardFemaleTherapist.setOnClickListener {
            expertType = "female"
            updateTherapistSelection()
        }


        // =====================================================
        // CHECKBOX CARD CLICK
        // =====================================================

        cbDoorbell.setOnClickListener {
            ringDoorbell = !ringDoorbell
            updateCheckbox(cbDoorbell, ivDoorbellCheck, ringDoorbell)
        }

        cbDoNotCall.setOnClickListener {
            doNotCallBeforeRoom = !doNotCallBeforeRoom
            updateCheckbox(cbDoNotCall, ivDoNotCallCheck, doNotCallBeforeRoom)
        }

        cbCallConcierge.setOnClickListener {
            callConcierge = !callConcierge
            updateCheckbox(cbCallConcierge, ivCallConciergeCheck, callConcierge)
        }


        // =====================================================
        // VOICE RECORD BUTTON
        // =====================================================

        ivVoiceNote.setOnClickListener {
            if (isRecording) {
                stopRecordingAndSave()
            } else {
                checkMicrophonePermissionAndStart()
            }
        }

        ivStopRecording.setOnClickListener {
            stopRecordingAndSave()
        }

        ivDeleteVoice.setOnClickListener {
            deleteVoiceNote()
        }

        ivPlayVoice.setOnClickListener {
            togglePlayback()
        }


        // =====================================================
        // CONTINUE
        // =====================================================

        btnContinue.setOnClickListener {
            validateAndContinue()
        }
    }


    // =====================================================
    // UPDATE CHECKBOX UI
    // =====================================================

    private fun updateCheckbox(
        card: MaterialCardView,
        checkIcon: ImageView,
        checked: Boolean
    ) {
        if (checked) {
            card.setCardBackgroundColor(getColor(R.color.Button_Blue))
            card.strokeColor = getColor(R.color.Button_Blue)
            card.strokeWidth = 1
            checkIcon.visibility = View.VISIBLE
        } else {
            card.setCardBackgroundColor(getColor(R.color.white))
            card.strokeColor = getColor(R.color.Gray)
            card.strokeWidth = 1
            checkIcon.visibility = View.GONE
        }
    }


    // =====================================================
    // UPDATE THERAPIST CARD UI
    // =====================================================

    private fun updateTherapistSelection() {

        when (expertType) {

            "male" -> {
                cardMaleTherapist.setCardBackgroundColor(
                    getColor(R.color.BackgroundBlue)
                )
                cardMaleTherapist.strokeColor = getColor(R.color.Button_Blue)
                cardMaleTherapist.strokeWidth = 2

                cardFemaleTherapist.setCardBackgroundColor(
                    getColor(android.R.color.white)
                )
                cardFemaleTherapist.strokeColor = getColor(R.color.light_Gray)
                cardFemaleTherapist.strokeWidth = 2
            }

            "female" -> {
                cardFemaleTherapist.setCardBackgroundColor(
                    getColor(R.color.BackgroundBlue)
                )
                cardFemaleTherapist.strokeColor = getColor(R.color.Button_Blue)
                cardFemaleTherapist.strokeWidth = 2

                cardMaleTherapist.setCardBackgroundColor(
                    getColor(android.R.color.white)
                )
                cardMaleTherapist.strokeColor = getColor(R.color.light_Gray)
                cardMaleTherapist.strokeWidth = 2
            }

            else -> {
                cardMaleTherapist.setCardBackgroundColor(
                    getColor(android.R.color.white)
                )
                cardMaleTherapist.strokeColor = getColor(R.color.light_Gray)
                cardMaleTherapist.strokeWidth = 1

                cardFemaleTherapist.setCardBackgroundColor(
                    getColor(android.R.color.white)
                )
                cardFemaleTherapist.strokeColor = getColor(R.color.light_Gray)
                cardFemaleTherapist.strokeWidth = 1
            }
        }
    }


    // =====================================================
    // VALIDATION + CONTINUE
    // =====================================================

    private fun validateAndContinue() {

        if (expertType.isEmpty()) {
            Toast.makeText(
                this,
                "Please select therapist preference",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // If currently recording, stop first
        if (isRecording) {
            stopRecordingAndSave()
        }

        val specialNote = etSpecialNote.text.toString().trim()
        val voiceNotePath = voiceNoteFile?.absolutePath ?: ""

        val intent = Intent(this, Slot_activity::class.java)

        intent.putExtra("guest_name", guestName)
        intent.putExtra("guest_mobile_full", guestMobile)
        intent.putExtra("country_code", country_Code)
        intent.putExtra("guest_mobile", Mobile)


        intent.putExtra("room_id", roomId)
        intent.putExtra("room_number", roomNumber)
        intent.putExtra("floor_wing", floorWing)

        intent.putExtra("expert_type", expertType)

        intent.putExtra("ring_doorbell", ringDoorbell)
        intent.putExtra("do_not_call", doNotCallBeforeRoom)
        intent.putExtra("call_concierge", callConcierge)

        intent.putExtra("special_note", specialNote)
        intent.putExtra("voice_note_path", voiceNotePath)
        intent.putExtra("SELECTED_SERVICE", service)

        startActivity(intent)
    }


    // =====================================================
    // MICROPHONE PERMISSION
    // =====================================================

    private fun checkMicrophonePermissionAndStart() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startRecording()
        } else {
            recordAudioPermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }


    private val recordAudioPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startRecording()
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission is required for voice note",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    // =====================================================
    // START RECORDING
    // =====================================================

    private fun startRecording() {

        try {

            // Prepare file
            val timeStamp =
                SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()
                ).format(Date())

            val fileName = "voice_note_$timeStamp.m4a"

            val file = File(getExternalFilesDir(null), fileName)

            voiceNoteFile = file


            // MediaRecorder
            mediaRecorder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(this)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }

            mediaRecorder?.apply {

                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44_100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            isRecording = true

            // Icon color feedback
            ivVoiceNote.setColorFilter(
                ContextCompat.getColor(this, R.color.Button_Blue)
            )

            // Show recording panel, hide attached panel
            layoutRecording.visibility = View.VISIBLE
            layoutVoiceAttached.visibility = View.GONE

            // Start blinking dot + timer
            startBlinkingDot()
            startRecordingTimer()

            Toast.makeText(
                this,
                "Recording... Tap ⏹ to stop",
                Toast.LENGTH_SHORT
            ).show()


            // Auto-stop after max duration
            timerHandler.postDelayed(
                stopRecordingRunnable,
                maxRecordDurationMs
            )

        } catch (e: Exception) {

            e.printStackTrace()

            isRecording = false

            mediaRecorder?.release()
            mediaRecorder = null

            voiceNoteFile = null

            layoutRecording.visibility = View.GONE
            stopBlinkingDot()
            stopRecordingTimer()

            Toast.makeText(
                this,
                "Failed to start recording: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // =====================================================
    // STOP RECORDING AND SAVE
    // =====================================================

    private fun stopRecordingAndSave() {

        if (!isRecording) return

        // Stop timers first
        stopRecordingTimer()
        stopBlinkingDot()

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
            voiceNoteFile?.delete()
            voiceNoteFile = null
        } finally {
            mediaRecorder = null
            isRecording = false
            ivVoiceNote.clearColorFilter()
            timerHandler.removeCallbacks(stopRecordingRunnable)
        }

        // Hide recording panel
        layoutRecording.visibility = View.GONE

        if (voiceNoteFile != null && voiceNoteFile!!.exists()) {

            layoutVoiceAttached.visibility = View.VISIBLE
            tvVoiceDuration.text = formatDuration(recordingDurationMs)

            Toast.makeText(
                this,
                "Voice note attached ✓",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            layoutVoiceAttached.visibility = View.GONE

            Toast.makeText(
                this,
                "Recording failed — no file saved",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // =====================================================
    // DELETE VOICE NOTE
    // =====================================================

    private fun deleteVoiceNote() {

        stopPlayback()

        voiceNoteFile?.delete()
        voiceNoteFile = null

        layoutVoiceAttached.visibility = View.GONE

        Toast.makeText(
            this,
            "Voice note removed",
            Toast.LENGTH_SHORT
        ).show()
    }


    // =====================================================
    // PLAYBACK
    // =====================================================

    private fun togglePlayback() {

        if (isPlaying) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }


    private fun startPlayback() {

        val file = voiceNoteFile ?: return

        try {

            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
            }

            isPlaying = true
            ivPlayVoice.setImageResource(R.drawable.ic_pause)

            mediaPlayer?.setOnCompletionListener {
                stopPlayback()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Cannot play voice note",
                Toast.LENGTH_SHORT
            ).show()
            stopPlayback()
        }
    }


    private fun stopPlayback() {

        try {
            mediaPlayer?.stop()
        } catch (_: Exception) { }

        mediaPlayer?.release()
        mediaPlayer = null

        isPlaying = false

        ivPlayVoice.setImageResource(R.drawable.ic_play_arrow)
    }


    // =====================================================
    // LIFECYCLE
    // =====================================================

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }


    override fun onStop() {

        if (isRecording) {
            stopRecordingAndSave()
        }

        stopPlayback()

        super.onStop()
    }


    override fun onDestroy() {

        timerHandler.removeCallbacks(stopRecordingRunnable)
        blinkHandler.removeCallbacks(blinkRunnable)
        timerTickHandler.removeCallbacks(timerTickRunnable)

        stopPlayback()

        mediaRecorder?.release()
        mediaRecorder = null

        super.onDestroy()
    }
}