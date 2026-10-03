package com.company.jifiit.TrainingHub;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import android.text.TextUtils;
public class VideoPlayerActivity extends AppCompatActivity implements
        TextureView.SurfaceTextureListener,
        SeekBar.OnSeekBarChangeListener {

    private TextureView textureView;
    private MediaPlayer mediaPlayer;
    private Surface surface;
    private ProgressBar progressBar;
    private ImageButton fullscreenButton, playPauseButton;
    private boolean isFullscreen = false;
    private RelativeLayout videoContainer;
    private FrameLayout videoFrame;
    private SeekBar seekBar;
    private TextView views, VideoNo, Descriptions, title, currentTime, totalTime,Date,showMore;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;
    private LinearLayout controlsLayout;
    private boolean isControlsVisible = true;
    private static final int CONTROLS_TIMEOUT = 3000; // 3 seconds
    private int videoWidth = 0;
    private int videoHeight = 0;
    private Runnable hideControlsRunnable = this::hideControls;
    private boolean isDescriptionExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        initializeViews();
        setupClickListeners();
        setupVideoInfo();

        textureView.setSurfaceTextureListener(this);
        setupTouchListener();
    }

    private void initializeViews() {
        textureView = findViewById(R.id.textureView);
        progressBar = findViewById(R.id.progressBar);
        fullscreenButton = findViewById(R.id.fullscreenButton);
        videoContainer = findViewById(R.id.videoContainer);
        videoFrame = findViewById(R.id.videoFrame);
        playPauseButton = findViewById(R.id.playButton);
        seekBar = findViewById(R.id.seekBar);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        views = findViewById(R.id.view);
        VideoNo = findViewById(R.id.Id);
        Descriptions = findViewById(R.id.videoDescription);
        title = findViewById(R.id.description);
        Date = findViewById(R.id.Date);
        controlsLayout = findViewById(R.id.controlsLayout);
        showMore = findViewById(R.id.showMore);
        seekBar.setOnSeekBarChangeListener(this);
        Descriptions.setOnClickListener(v -> toggleDescription());
        showMore.setOnClickListener(v -> toggleDescription());
    }

    private void toggleDescription() {
        if (isDescriptionExpanded) {
            // Collapse description
            Descriptions.setMaxLines(2);
            Descriptions.setEllipsize(TextUtils.TruncateAt.END);
            showMore.setText("Show more");
        } else {
            // Expand description
            Descriptions.setMaxLines(Integer.MAX_VALUE);
            Descriptions.setEllipsize(null);
            showMore.setText("Show less");
        }
        isDescriptionExpanded = !isDescriptionExpanded;
    }

    private void setupTouchListener() {
        textureView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                toggleControls();
            }
            return true;
        });
    }

    private void toggleControls() {
        if (isControlsVisible) {
            hideControls();
        } else {
            showControls();
            resetControlsTimeout();
        }
    }

    private void showControls() {
        isControlsVisible = true;
        playPauseButton.setVisibility(View.VISIBLE);
        fullscreenButton.setVisibility(View.VISIBLE);
        controlsLayout.setVisibility(View.VISIBLE);
    }

    private void hideControls() {
        isControlsVisible = false;
        playPauseButton.setVisibility(View.INVISIBLE);
        fullscreenButton.setVisibility(View.INVISIBLE);
        controlsLayout.setVisibility(View.INVISIBLE);
    }

    private void setupVideoInfo() {
        String videoUrl = getIntent().getStringExtra("video_path");
        String vId = getIntent().getStringExtra("id");
        String Description = getIntent().getStringExtra("Content");
        String Title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("Date");
        title.setText(Title);
        Date.setText(date);

        VideoNo.setText("No." + vId + ":");

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        if (!idString.isEmpty()) {
            try {
                BigInteger id = new BigInteger(idString);
                fetchingViews(id, vId);
            } catch (NumberFormatException e) {
                Log.e("VideoPlayer", "Invalid user ID", e);
            }
        }

        setupDescription(Description);
    }

    private void setupDescription(String description) {
        Descriptions.setText(description);

        // Check if description needs "Show more" button
        Descriptions.post(() -> {
            int lineCount = Descriptions.getLineCount();
            if (lineCount > 2) {
                showMore.setVisibility(View.VISIBLE);
            } else {
                showMore.setVisibility(View.GONE);
            }
        });
    }

    private void setupMediaPlayer() {
        String videoUrl = getIntent().getStringExtra("video_path");
        progressBar.setVisibility(View.VISIBLE);

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.parse(videoUrl));
            mediaPlayer.setSurface(surface);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> {
                videoWidth = width;
                videoHeight = height;
                adjustVideoSize();
            });

            mediaPlayer.setOnPreparedListener(mp -> {
                progressBar.setVisibility(View.GONE);
                playPauseButton.setVisibility(View.VISIBLE);
                playPauseButton.setImageResource(R.drawable.ic_pause);
                mediaPlayer.start();

                int duration = mediaPlayer.getDuration();
                totalTime.setText(formatTime(duration));
                seekBar.setMax(duration);
                startProgressUpdate();

                // Auto-hide controls after timeout
                handler.postDelayed(hideControlsRunnable, CONTROLS_TIMEOUT);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                playPauseButton.setImageResource(R.drawable.ic_play_arrow);
                seekBar.setProgress(0);
                currentTime.setText(formatTime(0));
                showControls();
            });

            mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
                int secondaryProgress = (int) (mp.getDuration() * (percent / 100f));
                seekBar.setSecondaryProgress(secondaryProgress);
            });

            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("VideoPlayer", "MediaPlayer setup error", e);
            Toast.makeText(this, "Video playback error", Toast.LENGTH_SHORT).show();
        }
    }

    private void adjustVideoSize() {
        if (videoWidth == 0 || videoHeight == 0) return;

        int viewWidth = videoFrame.getWidth();
        int viewHeight = videoFrame.getHeight();

        if (viewWidth == 0 || viewHeight == 0) return;

        float videoRatio = (float) videoWidth / videoHeight;
        float viewRatio = (float) viewWidth / viewHeight;

        int newWidth, newHeight;

        if (videoRatio > viewRatio) {
            // Video is wider than view - scale to width
            newWidth = viewWidth;
            newHeight = (int) (viewWidth / videoRatio);
        } else {
            // Video is taller than view - scale to height
            newHeight = viewHeight;
            newWidth = (int) (viewHeight * videoRatio);
        }

        // Center the video
        ViewGroup.LayoutParams params = textureView.getLayoutParams();
        params.width = newWidth;
        params.height = newHeight;
        textureView.setLayoutParams(params);
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        int hours = (milliseconds / (1000 * 60 * 60)) % 24;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private void startProgressUpdate() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    currentTime.setText(formatTime(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateSeekBar);
    }

    private void stopProgressUpdate() {
        handler.removeCallbacks(updateSeekBar);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && mediaPlayer != null) {
            mediaPlayer.seekTo(progress);
            currentTime.setText(formatTime(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        stopProgressUpdate();
        showControls();
        handler.removeCallbacks(hideControlsRunnable);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            startProgressUpdate();
            resetControlsTimeout();
        }
    }

    private void setupClickListeners() {
        fullscreenButton.setOnClickListener(v -> {
            toggleFullscreen();
            resetControlsTimeout();
        });

        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playPauseButton.setImageResource(R.drawable.ic_play_arrow);
                    stopProgressUpdate();
                } else {
                    mediaPlayer.start();
                    playPauseButton.setImageResource(R.drawable.ic_pause);
                    startProgressUpdate();
                }
                resetControlsTimeout();
            }
        });
    }

    private void resetControlsTimeout() {
        handler.removeCallbacks(hideControlsRunnable);
        handler.postDelayed(hideControlsRunnable, CONTROLS_TIMEOUT);
    }

    private void toggleFullscreen() {
        if (isFullscreen) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ViewGroup.LayoutParams params = videoContainer.getLayoutParams();
            params.height = getResources().getDimensionPixelSize(R.dimen.video_height);
            videoContainer.setLayoutParams(params);
            isFullscreen = false;
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            ViewGroup.LayoutParams params = videoContainer.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            videoContainer.setLayoutParams(params);
            isFullscreen = true;
        }

        // Adjust video size after layout changes
        handler.postDelayed(() -> {
            if (videoWidth > 0 && videoHeight > 0) {
                adjustVideoSize();
            }
        }, 100);
    }

    private void fetchingViews(BigInteger id, String vId) {
        String url = Constant.fetchVideoDetails + id + "&video_id=" + vId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.has("error")) {
                            String error = response.getString("error");
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                            Log.d("fetchingViews", "Error in response: " + error);
                        } else {
                            int v_id_count = response.getInt("v_id_count");
                            views.setText("• " + v_id_count + " views");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("fetchingViews", "JSONException: " + e.getMessage());
                    }
                },
                error -> {
                    Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
                    Log.d("fetchingViews", "Volley Error: " + error.getMessage());
                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        surface = new Surface(surfaceTexture);
        setupMediaPlayer();

        videoFrame.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (videoWidth > 0 && videoHeight > 0) {
                adjustVideoSize();
            }
        });
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (videoWidth > 0 && videoHeight > 0) {
            adjustVideoSize();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.ic_play_arrow);
        }
        handler.removeCallbacks(hideControlsRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);
            startProgressUpdate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopProgressUpdate();
        handler.removeCallbacks(hideControlsRunnable);
    }
}