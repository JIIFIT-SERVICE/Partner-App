package com.company.jifiit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.net.Uri;
import android.text.InputType;

import android.database.Cursor;
import android.provider.OpenableColumns;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.view.inputmethod.InputMethodManager;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.AreaAdapter;
import com.company.jifiit.Adapter.AreaDropdownAdapter;
import com.company.jifiit.Adapter.RegisterPagerAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.webkit.MimeTypeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";
    private static final int TOTAL_PAGES = 5;
    private static final int MIN_REQUIRED_AREAS = 50;

    private ViewPager2 viewPager;
    private Button btnNext, btnPrevious;
    private TextView tvStep;
    private ProgressBar progressBar;
    private RegisterPagerAdapter adapter;
    private int currentPage = 0;

    // ============================================================
    // PAGE 1 - PERSONAL DETAILS
    // ============================================================

    private EditText regName, regAadhaar, regMobile, regEmail,
            regDob, regAddress;

    private AutoCompleteTextView regCity;
    private ProgressBar pbCityLoading;
    private String selectedLivingCityName = "";
    private final Handler citySearchHandler = new Handler(Looper.getMainLooper());
    private Runnable citySearchRunnable = null;
    private static final String TAG_CITY_SEARCH = "tag_city_search";

    private TextView tvAge;
    private RadioGroup rgGender;
    private CountryCodePicker ccp;
    private CircleImageView imgPhoto;
    private Button btnUploadPhoto;

    private String selectedDob = null;
    private String encodedPhoto = null;

    // Single RequestQueue & tags
    private RequestQueue mRequestQueue;
    private static final String TAG_PREFETCH = "tag_prefetch";
    private static final String TAG_REGISTRATION = "tag_registration";

    // Multi-step registration state: stores expert ID returned from Step 1 INSERT
    private String registeredExpertId = null;

    // Duplicate submission guard
    private boolean isSubmitting = false;

    // Persistent form state (decoupled from view recycling)
    private String selectedGenderString = "";
    private String formName = "";
    private String formAadhaar = "";
    private String formMobile = "";
    private String formEmail = "";
    private String formAddress = "";
    private String formCityName = "";
    private String existingProfilePhotoUrl = null;

    private void capturePage1FormState() {
        if (regName != null && regName.getText() != null && regName.getText().length() > 0) {
            formName = regName.getText().toString().trim();
        }
        if (regAadhaar != null && regAadhaar.getText() != null && regAadhaar.getText().length() > 0) {
            formAadhaar = regAadhaar.getText().toString().replaceAll("\\s+", "").trim();
        }
        if (regMobile != null && regMobile.getText() != null && regMobile.getText().length() > 0) {
            formMobile = (ccp != null) ? ccp.getFullNumberWithPlus() : regMobile.getText().toString().trim();
        }
        if (regEmail != null && regEmail.getText() != null && regEmail.getText().length() > 0) {
            formEmail = regEmail.getText().toString().trim();
        }
        if (regAddress != null && regAddress.getText() != null && regAddress.getText().length() > 0) {
            formAddress = regAddress.getText().toString().trim();
        }
        if (regCity != null && regCity.getText() != null && regCity.getText().length() > 0) {
            formCityName = regCity.getText().toString().trim();
        }
        if (rgGender != null && rgGender.getCheckedRadioButtonId() != -1) {
            RadioButton selectedRb = rgGender.findViewById(rgGender.getCheckedRadioButtonId());
            if (selectedRb != null) {
                selectedGenderString = selectedRb.getText().toString().trim();
            }
        }
    }

    private void restorePage1Views() {
        if (regName != null && !TextUtils.isEmpty(formName)) {
            if (!formName.equals(regName.getText().toString().trim())) {
                regName.setText(formName);
            }
        }
        if (regEmail != null && !TextUtils.isEmpty(formEmail)) {
            if (!formEmail.equals(regEmail.getText().toString().trim())) {
                regEmail.setText(formEmail);
            }
        }
        if (regAddress != null && !TextUtils.isEmpty(formAddress)) {
            if (!formAddress.equals(regAddress.getText().toString().trim())) {
                regAddress.setText(formAddress);
            }
        }
        if (regCity != null && !TextUtils.isEmpty(selectedLivingCityName)) {
            regCity.setText(selectedLivingCityName, false);
            formCityName = selectedLivingCityName;
        } else if (regCity != null && !TextUtils.isEmpty(formCityName)) {
            regCity.setText(formCityName, false);
            selectedLivingCityName = formCityName;
        }
        if (regMobile != null && !TextUtils.isEmpty(formMobile)) {
            if (ccp != null) {
                try {
                    ccp.setFullNumber(formMobile);
                } catch (Exception e) {
                    regMobile.setText(formMobile);
                }
            } else {
                regMobile.setText(formMobile);
            }
        }
        if (regAadhaar != null && !TextUtils.isEmpty(formAadhaar)) {
            String clean = formAadhaar.replaceAll("\\s+", "");
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < clean.length(); i++) {
                formatted.append(clean.charAt(i));
                if (i == 3 || i == 7) formatted.append(" ");
            }
            if (!formatted.toString().equals(regAadhaar.getText().toString().trim())) {
                regAadhaar.setText(formatted.toString());
            }
        }
        if (!TextUtils.isEmpty(selectedDob) && regDob != null) {
            try {
                SimpleDateFormat dbFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date d = dbFmt.parse(selectedDob);
                if (d != null) {
                    SimpleDateFormat dispFmt = new SimpleDateFormat("MM / dd / yyyy", Locale.getDefault());
                    regDob.setText(dispFmt.format(d));
                    Calendar dobCal = Calendar.getInstance();
                    dobCal.setTime(d);
                    Calendar today = Calendar.getInstance();
                    int years = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
                    int months = today.get(Calendar.MONTH) - dobCal.get(Calendar.MONTH);
                    if (today.get(Calendar.DAY_OF_MONTH) < dobCal.get(Calendar.DAY_OF_MONTH)) {
                        months--;
                    }
                    if (months < 0) {
                        years--;
                        months += 12;
                    }
                    if (tvAge != null) {
                        if (years < 18) {
                            tvAge.setText(R.string.age_warning);
                            tvAge.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        } else {
                            tvAge.setText("Your current age is : " + years + " years and " + months + " months");
                            tvAge.setTextColor(getResources().getColor(R.color.green_success));
                        }
                    }
                }
            } catch (Exception e) {
                regDob.setText(selectedDob);
            }
        }
        if (rgGender != null && !TextUtils.isEmpty(selectedGenderString)) {
            for (int i = 0; i < rgGender.getChildCount(); i++) {
                View child = rgGender.getChildAt(i);
                if (child instanceof RadioButton) {
                    RadioButton rb = (RadioButton) child;
                    if (selectedGenderString.equalsIgnoreCase(rb.getText().toString().trim())) {
                        rgGender.check(rb.getId());
                        rb.setBackgroundResource(R.drawable.bg_chip_selected);
                        rb.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        rb.setBackgroundResource(R.drawable.bg_chip_unselected);
                        rb.setTextColor(getResources().getColor(R.color.Text_Blue));
                    }
                }
            }
        }
        if (imgPhoto != null && !TextUtils.isEmpty(existingProfilePhotoUrl)) {
            try {
                com.bumptech.glide.Glide.with(this)
                        .load(existingProfilePhotoUrl)
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .into(imgPhoto);
            } catch (Exception e) {
                Log.w(TAG, "Could not load existing profile photo with Glide", e);
            }
        }
    }

    // TextWatchers to prevent duplicate listeners
    private TextWatcher aadhaarFormatterWatcher = null;
    private final TextWatcher pageFieldWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            capturePage1FormState();
            validateCurrentPage();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    // Shared background executor for file and image processing
    private static final ExecutorService BACKGROUND_EXECUTOR = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private final AtomicInteger threadCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "jifiit-worker-" + threadCount.getAndIncrement());
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        }
    });

    private RequestQueue getAppRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    // City state (Page 3 working city preferences)
    private String selectedLocalityId = "";
    private String selectedHubId = "";

    // Page 3 (status = 1)
    private final ArrayList<String> cityNamesP3 = new ArrayList<>();
    private final ArrayList<String> cityIdsP3 = new ArrayList<>();
    private boolean cityLoadedP3 = false;
    private boolean cityLoadingP3 = false;
    private boolean isCityPickerPendingP3 = false;

    private final ArrayList<String> localityNames = new ArrayList<>();
    private final ArrayList<String> localityIds = new ArrayList<>();
    private final ArrayList<String> localityHubIds = new ArrayList<>();

    // ============================================================
    // PAGE 2 - PROFESSIONAL DETAILS
    // ============================================================

    private RadioGroup rgCategory;
    private Spinner spExperience;
    private View flExperienceDropdown;
    private EditText etExpDesc;
    private View llServiceSection;
    private View dividerServiceSectionBottom;
    private TextView tvSelectedServicesCount;
    private TextView tvServiceCategoryHeader;
    private LinearLayout llServiceItemsContainer;

    public static class ExperienceOption {
        private final String label;
        private final String value;

        public ExperienceOption(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        @NonNull
        @Override
        public String toString() {
            return label;
        }
    }

    private final List<ExperienceOption> experienceOptions = new ArrayList<>();

    public static class CategoryItem {
        public final String id;
        public final String name;
        public final String subtitle;

        public CategoryItem(String id, String name, String subtitle) {
            this.id = id;
            this.name = name;
            this.subtitle = subtitle;
        }

        public CategoryItem(String id, String name) {
            this(id, name, "");
        }
    }

    public static final String[][] DEFAULT_CATEGORIES = {
            { "1", "Massage Therapist", "Deep tissue, Swedish, Aroma & therapeutic bodywork" },
            { "2", "Ayurveda Therapist", "Traditional Abhyanga, Shirodhara & holistic treatments" },
            { "3", "Personal Trainer", "Strength, weight loss, functional & athletic conditioning" },
            { "4", "Yoga Instructor", "Hatha, Vinyasa, Ashtanga & personalized wellness flows" },
            { "5", "Zumba Dancer", "High energy cardio dance, rhythm aerobic conditioning" },
            { "6", "Physiotherapist", "Clinical rehabilitation, post-surgery, joint & spine relief" },
            { "7", "Chiropractor", "Spinal adjustment, subluxation alignment & musculoskeletal health" },
            { "8", "Nutritionist", "Clinical diet planning, metabolic wellness & weight coaching" }
    };

    private final List<CategoryItem> dynamicCategories = new ArrayList<>();
    private final Set<String> selectedCategoryIds = new LinkedHashSet<>();
    private final Set<String> selectedCategories = new LinkedHashSet<>();
    private String selectedCategoryId = "";
    private final Map<String, List<String>> dynamicCategoryServicesCache = new HashMap<>();
    private boolean isCategoriesLoading = false;

    private void initDefaultCategories() {
        if (dynamicCategories.isEmpty()) {
            for (String[] cat : DEFAULT_CATEGORIES) {
                dynamicCategories.add(new CategoryItem(cat[0], cat[1], cat[2]));
            }
        }
    }

    private String selectedCategory = "";
    private String selectedExperience = "";
    private String lastBuiltCategory = "";

    private final Set<String> selectedServices = new LinkedHashSet<>();
    private final Map<String, Set<String>> categorySelectedServices = new LinkedHashMap<>();

    private final Set<String> loadingCategories = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> failedCategories = Collections.synchronizedSet(new HashSet<>());

    private boolean categoryUiBuilt = false;

    private static final Map<String, List<String>> CATEGORY_SERVICES = new LinkedHashMap<>();
    static {
        // Other Categories (Massage and Ayurveda MUST be fetched dynamically from database APIs)
        CATEGORY_SERVICES.put("Personal Trainer", Arrays.asList(
                "Strength & Muscle Building",
                "Weight Loss & Fat Burn",
                "Functional & HIIT Training",
                "Core & Mobility Conditioning",
                "Endurance & Cardio Training",
                "Posture & Flexibility Training"));

        CATEGORY_SERVICES.put("Yoga Instructor", Arrays.asList(
                "Hatha Yoga",
                "Vinyasa Flow Yoga",
                "Pranayama & Meditation",
                "Ashtanga Yoga",
                "Pre & Post Natal Yoga",
                "Therapeutic & Restorative Yoga"));

        CATEGORY_SERVICES.put("Zumba Dancer", Arrays.asList(
                "Zumba Classic Cardio",
                "Aqua Zumba",
                "Zumba Toning (Light Weights)",
                "Zumba Kids & Family",
                "Aerobic Dance Fitness",
                "Latin Rhythm Choreography"));

        CATEGORY_SERVICES.put("Physiotherapist", Arrays.asList(
                "Orthopedic Rehabilitation",
                "Sports Injury Therapy",
                "Back & Neck Pain Relief",
                "Neuro Rehabilitation",
                "Post-Surgery Physical Therapy",
                "Joint Mobilization Therapy"));

        CATEGORY_SERVICES.put("Chiropractor", Arrays.asList(
                "Spinal Alignment & Adjustment",
                "Cervical & Lumbar Decompression",
                "Posture Correction Therapy",
                "Sciatica Nerve Relief",
                "Joint Mobilization",
                "Musculoskeletal Pain Therapy"));

        CATEGORY_SERVICES.put("Nutritionist", Arrays.asList(
                "Weight Management Diet Plan",
                "Clinical Nutrition Consultation",
                "Sports & Athlete Nutrition",
                "Diabetes & Lifestyle Diet",
                "PCOS / Hormonal Diet Plan",
                "Detox & Gut Health Plan"));

        // Database Table / Backend Name Aliases (strictly no static entries for Massage or Ayurveda)
        CATEGORY_SERVICES.put("Personal trainer at home", CATEGORY_SERVICES.get("Personal Trainer"));
        CATEGORY_SERVICES.put("Yoga at home", CATEGORY_SERVICES.get("Yoga Instructor"));
        CATEGORY_SERVICES.put("Yoga Teacher", CATEGORY_SERVICES.get("Yoga Instructor"));
        CATEGORY_SERVICES.put("Zumba dance at home", CATEGORY_SERVICES.get("Zumba Dancer"));
        CATEGORY_SERVICES.put("Zumba Dance Teacher", CATEGORY_SERVICES.get("Zumba Dancer"));
        CATEGORY_SERVICES.put("Physiotherapist at home", CATEGORY_SERVICES.get("Physiotherapist"));
        CATEGORY_SERVICES.put("Chiropractor at home", CATEGORY_SERVICES.get("Chiropractor"));
        CATEGORY_SERVICES.put("Nutrionist at home", CATEGORY_SERVICES.get("Nutritionist"));
    }



    // ============================================================
    // PAGE 3 - PREFERENCES / SERVICE AREAS
    // ============================================================

    private MaterialAutoCompleteTextView actvCityDropdown;
    private RecyclerView rvAreas;
    private TextView tvTotalAreas;
    private TextView tvMinAreasHint;
    private View llNoAreasSelected;
    private View llAreasLoading;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;

    private AreaAdapter areaAdapter;

    private final ArrayList<AreaAdapter.Area> allAreas = new ArrayList<>();
    private final LinkedHashSet<String> selectedAreaIds = new LinkedHashSet<>();

    private String selectedCityIdP3 = "";
    private boolean areasLoaded = false;
    private boolean isLoadingAreas = false;
    private String areasLoadedCityId = "";

    // ============================================================
    // PAGE 4 - DOCUMENTS / VERIFICATION & COMPLIANCE
    // ============================================================

    private TextView tvResumeFileName, tvBtnUploadResume;
    private View btnUploadResume;

    private TextView tvSchool10thFileName, tvBtnUploadSchool10th;
    private View btnUploadSchool10th;
    private TextView tvSchool12thFileName, tvBtnUploadSchool12th;
    private View btnUploadSchool12th;
    private TextView tvSchoolGradFileName, tvBtnUploadSchoolGrad;
    private View btnUploadSchoolGrad;

    private View btnAddVocationalCert, layoutVocationalItem;
    private TextView tvVocationalTitle, tvVocationalSubtitle;
    private ImageView ivDeleteVocational;

    private TextView tvAadhaarFrontName, tvBtnUploadAadhaarFront;
    private View btnUploadAadhaarFront;
    private TextView tvAadhaarBackName, tvBtnUploadAadhaarBack;
    private View btnUploadAadhaarBack;

    private int activeDocUploadType = 0;
    private String resumeFileName = "";
    private String resumeDocBase64 = "";
    private String cert10thFileName = "";
    private String cert10thDocBase64 = "";
    private String cert12thFileName = "";
    private String cert12thDocBase64 = "";
    private String certGradFileName = "";
    private String certGradDocBase64 = "";
    private String aadhaarFrontFileName = "";
    private String aadhaarFrontDocBase64 = "";
    private String aadhaarBackFileName = "";
    private String aadhaarBackDocBase64 = "";
    private String vocationalCertFileName = "";
    private String vocationalCertDocBase64 = "";

    // ============================================================
    // PAGE 5 - REFERRAL
    // ============================================================

    private EditText etReferral;

    // ============================================================
    // IMAGE & DOCUMENT PICKERS
    // ============================================================

    private final ActivityResultLauncher<Intent> docPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri == null)
                        return;
                    processDocumentUri(activeDocUploadType, uri);
                }
            });

    private void openDocumentPicker(int type) {
        activeDocUploadType = type;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/pdf",
                "image/*",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        docPickerLauncher.launch(intent);
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private long getFileSize(Uri uri) {
        long size = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception ignored) {
        }
        if (size <= 0) {
            try (AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(uri, "r")) {
                if (afd != null) {
                    size = afd.getLength();
                }
            } catch (Exception ignored) {
            }
        }
        return size;
    }

    private String guessMimeType(String fileName) {
        if (fileName != null) {
            String lower = fileName.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".pdf"))
                return "application/pdf";
            if (lower.endsWith(".png"))
                return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
                return "image/jpeg";
            if (lower.endsWith(".doc"))
                return "application/msword";
            if (lower.endsWith(".docx"))
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "application/octet-stream";
    }

    private String decodeAndCompressImageUri(Uri uri, String mimeType, int maxDim, int targetMaxBytes)
            throws Exception {
        ContentResolver cr = getContentResolver();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream isBounds = cr.openInputStream(uri)) {
            BitmapFactory.decodeStream(isBounds, null, options);
        }

        int sampleSize = 1;
        while ((options.outWidth / sampleSize) > maxDim * 2 || (options.outHeight / sampleSize) > maxDim * 2) {
            sampleSize *= 2;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = sampleSize;
        Bitmap sampled;
        try (InputStream isActual = cr.openInputStream(uri)) {
            sampled = BitmapFactory.decodeStream(isActual, null, decodeOptions);
        }

        if (sampled == null) {
            throw new IOException("Failed to decode image");
        }

        // Correct EXIF rotation if present
        try (InputStream exifIs = cr.openInputStream(uri)) {
            if (exifIs != null) {
                ExifInterface exif = new ExifInterface(exifIs);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int rotationAngle = 0;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    rotationAngle = 90;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    rotationAngle = 180;
                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    rotationAngle = 270;
                }

                if (rotationAngle != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotationAngle);
                    Bitmap rotated = Bitmap.createBitmap(sampled, 0, 0, sampled.getWidth(), sampled.getHeight(), matrix,
                            true);
                    if (rotated != sampled) {
                        sampled.recycle();
                        sampled = rotated;
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        // Scale down to exact maxDim if either width or height exceeds maxDim
        int curWidth = sampled.getWidth();
        int curHeight = sampled.getHeight();
        if (curWidth > maxDim || curHeight > maxDim) {
            float ratio = Math.min((float) maxDim / curWidth, (float) maxDim / curHeight);
            int newW = Math.max(1, Math.round(curWidth * ratio));
            int newH = Math.max(1, Math.round(curHeight * ratio));
            Bitmap scaled = Bitmap.createScaledBitmap(sampled, newW, newH, true);
            if (scaled != sampled) {
                sampled.recycle();
                sampled = scaled;
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 65;
        sampled.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        while (baos.size() > targetMaxBytes && quality > 35) {
            baos.reset();
            quality -= 10;
            sampled.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }
        sampled.recycle();

        String rawB64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        return "data:image/jpeg;base64," + rawB64;
    }

    private String decodeAndCompressImageUri(Uri uri, String mimeType) throws Exception {
        return decodeAndCompressImageUri(uri, mimeType, 800, 65 * 1024);
    }

    private void processDocumentUri(final int type, final Uri uri) {
        if (uri == null)
            return;

        Toast.makeText(this, "Reading document...", Toast.LENGTH_SHORT).show();

        BACKGROUND_EXECUTOR.execute(() -> {
            try {
                String fileName = getFileNameFromUri(uri);
                if (TextUtils.isEmpty(fileName)) {
                    fileName = "document_" + System.currentTimeMillis();
                }

                ContentResolver cr = getContentResolver();
                String mimeType = cr.getType(uri);
                if (mimeType == null) {
                    mimeType = guessMimeType(fileName);
                }

                long fileSize = getFileSize(uri);
                // Clamping non-image documents (PDF, DOC) to 350KB keeps total POST body
                // comfortably under 1MB
                long MAX_NON_IMAGE_DOC_SIZE = 350 * 1024; // 350 KB
                if (mimeType != null && !mimeType.startsWith("image/") && fileSize > MAX_NON_IMAGE_DOC_SIZE) {
                    final String name = fileName;
                    final long sizeKb = fileSize / 1024;
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(Register.this)
                                .setTitle("Document Exceeds Size Limit")
                                .setMessage("'" + name + "' is " + sizeKb
                                        + " KB.\n\nTo ensure your registration uploads successfully without server rejection (HTTP 413), documents must be under 350 KB.\n\nTips:\n• Take a clear camera photo of your document (photos are automatically optimized to ~60 KB).\n• Or compress your PDF using an online PDF compressor before selecting it.")
                                .setPositiveButton("OK", null)
                                .show();
                    });
                    return;
                }

                String base64Payload;
                if (mimeType != null && mimeType.startsWith("image/")) {
                    base64Payload = decodeAndCompressImageUri(uri, mimeType, 800, 65 * 1024);
                } else {
                    InputStream is = cr.openInputStream(uri);
                    if (is == null) {
                        runOnUiThread(() -> Toast
                                .makeText(Register.this, "Unable to read selected document", Toast.LENGTH_SHORT)
                                .show());
                        return;
                    }
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[8192];
                    int nRead;
                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    byte[] fileBytes = buffer.toByteArray();
                    is.close();

                    String rawB64 = Base64.encodeToString(fileBytes, Base64.NO_WRAP);
                    base64Payload = "data:" + mimeType + ";base64," + rawB64;
                }

                final String finalFileName = fileName;
                final String finalPayload = base64Payload;
                runOnUiThread(() -> {
                    handleDocPickedWithData(type, finalFileName, finalPayload);
                });

            } catch (Exception e) {
                Log.e(TAG, "Document processing failed", e);
                runOnUiThread(() -> Toast.makeText(
                        Register.this,
                        "Failed to process document: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void handleDocPickedWithData(int type, String name, String base64Payload) {
        switch (type) {
            case 1:
                resumeFileName = name;
                resumeDocBase64 = base64Payload;
                if (tvResumeFileName != null)
                    tvResumeFileName.setText(name);
                if (tvBtnUploadResume != null)
                    tvBtnUploadResume.setText("Change Resume");
                break;
            case 2:
                cert10thFileName = name;
                cert10thDocBase64 = base64Payload;
                if (tvSchool10thFileName != null)
                    tvSchool10thFileName.setText(name);
                if (tvBtnUploadSchool10th != null)
                    tvBtnUploadSchool10th.setText("Replace 10th");
                break;
            case 3:
                cert12thFileName = name;
                cert12thDocBase64 = base64Payload;
                if (tvSchool12thFileName != null)
                    tvSchool12thFileName.setText(name);
                if (tvBtnUploadSchool12th != null)
                    tvBtnUploadSchool12th.setText("Replace 12th");
                break;
            case 4:
                certGradFileName = name;
                certGradDocBase64 = base64Payload;
                if (tvSchoolGradFileName != null)
                    tvSchoolGradFileName.setText(name);
                if (tvBtnUploadSchoolGrad != null)
                    tvBtnUploadSchoolGrad.setText("Replace Degree");
                break;
            case 5:
                aadhaarFrontFileName = name;
                aadhaarFrontDocBase64 = base64Payload;
                if (tvAadhaarFrontName != null)
                    tvAadhaarFrontName.setText(name);
                if (tvBtnUploadAadhaarFront != null)
                    tvBtnUploadAadhaarFront.setText("Replace Front");
                break;
            case 6:
                aadhaarBackFileName = name;
                aadhaarBackDocBase64 = base64Payload;
                if (tvAadhaarBackName != null)
                    tvAadhaarBackName.setText(name);
                if (tvBtnUploadAadhaarBack != null)
                    tvBtnUploadAadhaarBack.setText("Replace Back");
                break;
            case 7:
                vocationalCertFileName = name;
                vocationalCertDocBase64 = base64Payload;
                if (layoutVocationalItem != null) {
                    layoutVocationalItem.setVisibility(View.VISIBLE);
                }
                if (tvVocationalTitle != null) {
                    tvVocationalTitle.setText("Vocational Certificate");
                }
                if (tvVocationalSubtitle != null) {
                    tvVocationalSubtitle.setText("Uploaded · " + name);
                }
                break;
        }
        validateCurrentPage();
    }

    private void handleDocPicked(int type, String name) {
        handleDocPickedWithData(type, name, "");
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();

                    if (uri == null) {
                        return;
                    }

                    if (imgPhoto != null) {
                        imgPhoto.setImageURI(uri);
                    }

                    processProfilePhotoUri(uri);
                }
            });

    private void processProfilePhotoUri(Uri uri) {
        Toast.makeText(this, "Processing photo...", Toast.LENGTH_SHORT).show();
        BACKGROUND_EXECUTOR.execute(() -> {
            try {
                // Profile photo max 600px, 45KB binary target (~60KB Base64)
                String b64 = decodeAndCompressImageUri(uri, "image/jpeg", 600, 45 * 1024);
                runOnUiThread(() -> {
                    encodedPhoto = b64;
                    validateCurrentPage();
                });
            } catch (Exception e) {
                Log.e(TAG, "Image loading failed", e);
                runOnUiThread(() -> Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ============================================================
    // ACTIVITY LIFECYCLE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());

        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        setContentView(R.layout.activity_register);

        View root = findViewById(R.id.registerRootLayout);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

                int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);

                if (ime.bottom > systemBars.bottom) {
                    v.postDelayed(() -> {
                        View focused = getCurrentFocus();
                        if (focused instanceof EditText) {
                            ScrollView sv = findScrollView(getCurrentPageView());
                            if (sv != null) {
                                View target = focused;
                                if (focused.getParent() != null && focused.getParent().getParent() instanceof com.google.android.material.textfield.TextInputLayout) {
                                    target = (View) focused.getParent().getParent();
                                }
                                scrollToTarget(sv, target);
                            }
                        }
                    }, 120);
                }

                return windowInsets;
            });
            ViewCompat.requestApplyInsets(root);
        }

        initDefaultCategories();
        initViews();
        setupViewPager();
        setupButtons();
        loadCategoriesFromApi();
        fetchHomeMassageServicesFromApi();
        fetchAyurvedaServicesFromApi();
        checkSavedRegistrationProgress();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        View root = findViewById(R.id.registerRootLayout);
        if (root != null) {
            ViewCompat.requestApplyInsets(root);
            root.postDelayed(() -> {
                View focused = getCurrentFocus();
                if (focused instanceof EditText) {
                    ScrollView sv = findScrollView(getCurrentPageView());
                    if (sv != null) {
                        View target = focused;
                        if (focused.getParent() != null && focused.getParent().getParent() instanceof com.google.android.material.textfield.TextInputLayout) {
                            target = (View) focused.getParent().getParent();
                        }
                        scrollToTarget(sv, target);
                    }
                }
            }, 150);
        }
    }

    private void checkSavedRegistrationProgress() {
        Intent intent = getIntent();
        String intentExpertId = intent != null ? intent.getStringExtra("EXTRA_EXPERT_ID") : null;
        android.content.SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String storedId = !TextUtils.isEmpty(intentExpertId) ? intentExpertId : sp.getString("Id", "");

        if (!TextUtils.isEmpty(storedId)) {
            registeredExpertId = storedId;
        }

        if (intent != null && intent.hasExtra("EXTRA_PAGE_INDEX")) {
            int targetPage = intent.getIntExtra("EXTRA_PAGE_INDEX", 0);
            viewPager.setCurrentItem(targetPage, false);
            return;
        }

        if (TextUtils.isEmpty(storedId)) {
            Log.d(TAG, "No stored expert ID in credentials. Starting registration at Page 1.");
            return;
        }

        trackRegistrationStatus(storedId);
    }

    private void saveExpertIdToPrefs(String expertId) {
        if (TextUtils.isEmpty(expertId)) {
            return;
        }
        registeredExpertId = expertId;
        getSharedPreferences("credentials", MODE_PRIVATE)
                .edit()
                .putString("Id", expertId)
                .apply();
        Log.d(TAG, "Registered expert id saved to prefs: " + registeredExpertId);
    }

    private void trackRegistrationStatus(String expertId) {
        if (TextUtils.isEmpty(expertId)) {
            return;
        }

        Log.d(TAG, "Tracking registration status for ID: " + expertId);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking registration progress...");
        progressDialog.setCancelable(false);
        if (!isFinishing() && !isDestroyed()) {
            progressDialog.show();
        }
        Runnable safeDismiss = createSafeDismiss(progressDialog);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constant.TrackRegistrationStatusUrl,
                response -> {
                    safeDismiss.run();
                    Log.d(TAG, "Track status response: " + response);
                    String cleanResponse = response != null ? response.trim() : "";
                    int jsonStart = cleanResponse.indexOf('{');
                    int jsonEnd = cleanResponse.lastIndexOf('}');
                    if (jsonStart >= 0 && jsonEnd > jsonStart) {
                        cleanResponse = cleanResponse.substring(jsonStart, jsonEnd + 1);
                    }
                    if (!cleanResponse.startsWith("{")) {
                        Log.w(TAG, "Non-JSON response from track status API: " + response);
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(cleanResponse);
                        if (!json.optBoolean("status", false)) {
                            Log.w(TAG, "Status check returned false: " + json.optString("message"));
                            String msg = json.optString("message", "");
                            if (msg.toLowerCase().contains("not found")) {
                                getSharedPreferences("credentials", MODE_PRIVATE).edit().remove("Id").apply();
                                registeredExpertId = null;
                            }
                            return;
                        }

                        // Recover Page 1 personal fields
                        String rName = json.optString("name", "");
                        String rPhone = json.optString("phone", "");
                        String rEmail = json.optString("email", "");
                        String rAadhaar = json.optString("aadhar_card_no", "");
                        String rGender = json.optString("gender", "");
                        String rDob = json.optString("date_of_birth", "");
                        String rAddress = json.optString("address", "");
                        String rCityId = json.optString("city_id", "");
                        String rCityName = json.optString("city_name", "");
                        String rPhotoUrl = json.optString("profile_photo_url", "");

                        if (!TextUtils.isEmpty(rName)) formName = rName;
                        if (!TextUtils.isEmpty(rPhone)) formMobile = rPhone;
                        if (!TextUtils.isEmpty(rEmail)) formEmail = rEmail;
                        if (!TextUtils.isEmpty(rAadhaar)) formAadhaar = rAadhaar;
                        if (!TextUtils.isEmpty(rGender)) selectedGenderString = rGender;
                        if (!TextUtils.isEmpty(rDob)) selectedDob = rDob;
                        if (!TextUtils.isEmpty(rAddress)) formAddress = rAddress;
                        String rLivingCity = json.optString("living_city_name", "");
                        if (!TextUtils.isEmpty(rLivingCity)) {
                            selectedLivingCityName = rLivingCity;
                            formCityName = rLivingCity;
                        } else if (!TextUtils.isEmpty(rCityName)) {
                            selectedLivingCityName = rCityName;
                            formCityName = rCityName;
                        }
                        if (!TextUtils.isEmpty(rPhotoUrl)) existingProfilePhotoUrl = rPhotoUrl;
                        String rExperience = json.optString("experience", "");
                        if (!TextUtils.isEmpty(rExperience)) {
                            selectedExperience = rExperience;
                        }

                        if (currentPage == 0) {
                            restorePage1Views();
                            validateCurrentPage();
                        }

                        int l1 = json.optInt("1st_layout", 0);
                        int l2 = json.optInt("2nd_layout", 0);
                        int l3 = json.optInt("3rd_layout", 0);
                        int l4 = json.optInt("4th_layout", 0);
                        int l5 = json.optInt("5th_layout", 0);
                        String verification = json.optString("verification", "UNVERIFIED").trim();
                        String kitPayment = json.optString("kit_payment", "").trim();

                        boolean allLayoutsComplete = (l1 == 1 && l2 == 1 && l3 == 1 && l4 == 1 && l5 == 1);

                        if (allLayoutsComplete) {
                            if ("VERIFIED".equalsIgnoreCase(verification)) {
                                if ("pending".equalsIgnoreCase(kitPayment)) {
                                    Intent intent = new Intent(Register.this, TrainingRegistrationKitActivity.class);
                                    intent.putExtra("EXTRA_EXPERT_ID", expertId);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(Register.this, VerificationCenterActivity.class);
                                    intent.putExtra("EXTRA_EXPERT_ID", expertId);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                Intent intent = new Intent(Register.this, ApplicationSubmittedActivity.class);
                                intent.putExtra("EXTRA_EXPERT_ID", expertId);
                                startActivity(intent);
                                finish();
                            }
                        } else if (l4 == 1 && l5 == 0) {
                            viewPager.setCurrentItem(4, false);
                        } else if (l3 == 1 && l4 == 0) {
                            viewPager.setCurrentItem(3, false);
                        } else if (l2 == 1 && l3 == 0) {
                            viewPager.setCurrentItem(2, false);
                        } else if (l1 == 1 && l2 == 0) {
                            viewPager.setCurrentItem(1, false);
                        } else {
                            viewPager.setCurrentItem(0, false);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing track status JSON", e);
                    }
                },
                error -> {
                    safeDismiss.run();
                    Log.e(TAG, "Failed to track registration progress: " + error.getMessage());
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", expertId);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 1.0f));
        getAppRequestQueue().add(request.setTag(TAG_REGISTRATION));
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);

        btnPrevious = findViewById(R.id.btnPrevious);
        tvStep = findViewById(R.id.tvSte);
        progressBar = findViewById(R.id.progressBar);

        if (btnNext != null) {
            btnNext.setEnabled(false);
            btnNext.setBackgroundColor(Color.parseColor("#BDBDBD"));
        }

        View backButton = findViewById(R.id.backbtn);
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupViewPager() {
        adapter = new RegisterPagerAdapter();

        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(1);

        viewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        hideKeyboard();
                        currentPage = position;
                        updateUI();

                        // Safety net: if user somehow lands on page 2 (index 1),
                        // make sure page-3 city list starts loading.
                        if (position == 1 && !cityLoadedP3 && !cityLoadingP3
                                && cityNamesP3.isEmpty()) {
                            loadCities(1);
                        }

                        viewPager.post(() -> {
                            initPageViews(position);
                            setupTextWatchers(position);
                            validateCurrentPage();
                        });
                    }
                });

        viewPager.post(() -> {
            initPageViews(0);
            setupTextWatchers(0);
            validateCurrentPage();
        });
    }

    private View getCurrentPageView() {
        if (viewPager == null || adapter == null) {
            return null;
        }

        RecyclerView rv = (RecyclerView) viewPager.getChildAt(0);
        if (rv == null) {
            return null;
        }

        RecyclerView.ViewHolder holder = rv.findViewHolderForAdapterPosition(currentPage);
        return holder != null ? holder.itemView : null;
    }

    // ============================================================
    // KEYBOARD HANDLING & AUTO-SCROLL HELPERS
    // ============================================================

    private void hideKeyboard() {
        try {
            View view = getCurrentFocus();
            if (view == null) {
                view = findViewById(R.id.registerRootLayout);
            }
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                view.clearFocus();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding keyboard", e);
        }
    }

    private void setupFocusAutoScroll(View pageView) {
        if (pageView == null) return;
        ScrollView sv = findScrollView(pageView);
        if (sv != null) {
            attachFocusAutoScroll(pageView, sv);
        }
    }

    private ScrollView findScrollView(View view) {
        if (view == null) return null;
        if (view instanceof ScrollView) return (ScrollView) view;
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                ScrollView sv = findScrollView(vg.getChildAt(i));
                if (sv != null) return sv;
            }
        }
        return null;
    }

    private void attachFocusAutoScroll(View root, ScrollView sv) {
        if (root == null || sv == null) return;
        if (root instanceof EditText) {
            final View editView = root;
            final View scrollTarget;
            if (editView.getParent() != null && editView.getParent().getParent() instanceof com.google.android.material.textfield.TextInputLayout) {
                scrollTarget = (View) editView.getParent().getParent();
            } else {
                scrollTarget = editView;
            }

            View.OnFocusChangeListener existingListener = editView.getOnFocusChangeListener();
            editView.setOnFocusChangeListener((v, hasFocus) -> {
                if (existingListener != null) {
                    existingListener.onFocusChange(v, hasFocus);
                }
                if (hasFocus) {
                    sv.postDelayed(() -> scrollToTarget(sv, scrollTarget), 200);
                }
            });
        } else if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++) {
                attachFocusAutoScroll(vg.getChildAt(i), sv);
            }
        }
    }

    private void scrollToTarget(final ScrollView scrollView, final View targetView) {
        if (scrollView == null || targetView == null) {
            return;
        }
        scrollView.post(() -> {
            try {
                Rect rect = new Rect();
                targetView.getDrawingRect(rect);
                scrollView.offsetDescendantRectToMyCoords(targetView, rect);

                int extraPadding = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

                int scrollY = scrollView.getScrollY();
                int scrollViewHeight = scrollView.getHeight();
                if (scrollViewHeight <= 0) return;

                int targetTop = rect.top;
                int targetBottom = rect.bottom;
                int targetHeight = targetBottom - targetTop;

                if (targetHeight + extraPadding >= scrollViewHeight) {
                    int desiredScrollY = Math.max(0, targetTop - (extraPadding / 2));
                    scrollView.smoothScrollTo(0, desiredScrollY);
                } else if (targetBottom + extraPadding > scrollY + scrollViewHeight) {
                    int scrollAmount = (targetBottom + extraPadding) - (scrollY + scrollViewHeight);
                    scrollView.smoothScrollBy(0, scrollAmount);
                } else if (targetTop - extraPadding < scrollY) {
                    int scrollAmount = Math.max(0, targetTop - extraPadding) - scrollY;
                    scrollView.smoothScrollBy(0, scrollAmount);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error scrolling to target view", e);
            }
        });
    }

    // ============================================================
    // INITIALIZE PAGE VIEWS
    // ============================================================

    private void initPageViews(int position) {
        View pageView = getCurrentPageView();

        if (pageView == null) {
            viewPager.postDelayed(() -> initPageViews(position), 50);
            return;
        }

        setupFocusAutoScroll(pageView);

        switch (position) {
            // ----------------------------------------------------
            // PAGE 1 - PERSONAL
            // ----------------------------------------------------
            case 0:
                regName = pageView.findViewById(R.id.reg_name);
                regAadhaar = pageView.findViewById(R.id.reg_aadhaar);
                regMobile = pageView.findViewById(R.id.reg_mobile);
                regEmail = pageView.findViewById(R.id.reg_email);
                regDob = pageView.findViewById(R.id.reg_age);
                regCity = pageView.findViewById(R.id.etCity);
                pbCityLoading = pageView.findViewById(R.id.pbCityLoading);
                regAddress = pageView.findViewById(R.id.reg_address);
                tvAge = pageView.findViewById(R.id.current_age);
                rgGender = pageView.findViewById(R.id.radioGroup);
                ccp = pageView.findViewById(R.id.VerifyCountryCode);
                imgPhoto = pageView.findViewById(R.id.upLivePhoto);
                btnUploadPhoto = pageView.findViewById(R.id.uploadLivePhotoButton);

                if (ccp != null && regMobile != null) {
                    ccp.setDefaultCountryUsingNameCode("IN");
                    ccp.resetToDefaultCountry();
                    ccp.registerCarrierNumberEditText(regMobile);
                }

                setupAadhaarFormatter();
                setupSearchableCityPage1();
                setupGenderChips();
                setupDobPicker();
                setupPhotoUpload();
                restorePage1Views();
                break;

            // ----------------------------------------------------
            // PAGE 2 - PROFESSIONAL
            // ----------------------------------------------------
            case 1:
                rgCategory = pageView.findViewById(R.id.rgCategory);
                spExperience = pageView.findViewById(R.id.spExperience);
                flExperienceDropdown = pageView.findViewById(R.id.flExperienceDropdown);
                etExpDesc = pageView.findViewById(R.id.reg_exp_desc);
                llServiceSection = pageView.findViewById(R.id.llServiceSection);
                dividerServiceSectionBottom = pageView.findViewById(R.id.dividerServiceSectionBottom);
                tvSelectedServicesCount = pageView.findViewById(R.id.tvSelectedServicesCount);
                tvServiceCategoryHeader = pageView.findViewById(R.id.tvServiceCategoryHeader);
                llServiceItemsContainer = pageView.findViewById(R.id.llServiceItemsContainer);

                if (rgCategory != null) {
                    initDefaultCategories();
                    if (rgCategory.getChildCount() == 0) {
                        buildCategoryRows();
                    }
                    loadCategoriesFromApi();
                    fetchHomeMassageServicesFromApi();
                    fetchAyurvedaServicesFromApi();
                }

                if (spExperience != null) {
                    setupExperienceDropdown();
                }

                if (!selectedCategories.isEmpty() || !TextUtils.isEmpty(selectedCategory)) {
                    highlightCategoryRows();
                    renderServicesForSelectedCategories(false);
                } else if (llServiceSection != null) {
                    llServiceSection.setVisibility(View.GONE);
                    if (dividerServiceSectionBottom != null) {
                        dividerServiceSectionBottom.setVisibility(View.GONE);
                    }
                }
                break;

            // ----------------------------------------------------
            // PAGE 3 - PREFERENCES
            // ----------------------------------------------------
            case 2:
                actvCityDropdown = pageView.findViewById(R.id.actvCityDropdown);
                rvAreas = pageView.findViewById(R.id.rvAreas);
                tvTotalAreas = pageView.findViewById(R.id.tvTotalAreas);
                tvMinAreasHint = pageView.findViewById(R.id.tvMinAreasHint);
                llNoAreasSelected = pageView.findViewById(R.id.llNoAreasSelected);
                llAreasLoading = pageView.findViewById(R.id.llAreasLoading);
                tvEmptyTitle = pageView.findViewById(R.id.tvEmptyTitle);
                tvEmptySubtitle = pageView.findViewById(R.id.tvEmptySubtitle);

                View cityContainer = pageView.findViewById(R.id.cityContainer);
                if (cityContainer != null) {
                    cityContainer.setOnClickListener(v -> openCityPickerPage3());
                }

                View ivCityDropdownArrow = pageView.findViewById(R.id.ivCityDropdownArrow);
                if (ivCityDropdownArrow != null) {
                    ivCityDropdownArrow.setOnClickListener(v -> openCityPickerPage3());
                }

                setupCityDropdownPage3();
                setupAreaPreferences();
                break;

            // ----------------------------------------------------
            // PAGE 4 - DOCUMENTS / VERIFICATION
            // ----------------------------------------------------
            case 3:
                initDocumentViews(pageView);
                break;

            // ----------------------------------------------------
            // PAGE 5 - REFERRAL
            // ----------------------------------------------------
            case 4:
                etReferral = pageView.findViewById(R.id.reg_referral);
                TextView signIn = pageView.findViewById(R.id.do_you);

                if (signIn != null) {
                    signIn.setOnClickListener(v -> startActivity(new Intent(Register.this, LoginActivity.class)));
                }
                break;
        }
    }

    // ============================================================
    // PAGE 4 - DOCUMENTS / VERIFICATION METHODS
    // ============================================================

    private void initDocumentViews(View pageView) {
        if (pageView == null)
            return;

        tvResumeFileName = pageView.findViewById(R.id.tvResumeFileName);
        btnUploadResume = pageView.findViewById(R.id.btnUploadResume);
        tvBtnUploadResume = pageView.findViewById(R.id.tvBtnUploadResume);

        tvSchool10thFileName = pageView.findViewById(R.id.tvSchool10thFileName);
        btnUploadSchool10th = pageView.findViewById(R.id.btnUploadSchool10th);
        tvBtnUploadSchool10th = pageView.findViewById(R.id.tvBtnUploadSchool10th);

        tvSchool12thFileName = pageView.findViewById(R.id.tvSchool12thFileName);
        btnUploadSchool12th = pageView.findViewById(R.id.btnUploadSchool12th);
        tvBtnUploadSchool12th = pageView.findViewById(R.id.tvBtnUploadSchool12th);

        tvSchoolGradFileName = pageView.findViewById(R.id.tvSchoolGradFileName);
        btnUploadSchoolGrad = pageView.findViewById(R.id.btnUploadSchoolGrad);
        tvBtnUploadSchoolGrad = pageView.findViewById(R.id.tvBtnUploadSchoolGrad);

        btnAddVocationalCert = pageView.findViewById(R.id.btnAddVocationalCert);
        layoutVocationalItem = pageView.findViewById(R.id.layoutVocationalItem);
        tvVocationalTitle = pageView.findViewById(R.id.tvVocationalTitle);
        tvVocationalSubtitle = pageView.findViewById(R.id.tvVocationalSubtitle);
        ivDeleteVocational = pageView.findViewById(R.id.ivDeleteVocational);

        tvAadhaarFrontName = pageView.findViewById(R.id.tvAadhaarFrontName);
        btnUploadAadhaarFront = pageView.findViewById(R.id.btnUploadAadhaarFront);
        tvBtnUploadAadhaarFront = pageView.findViewById(R.id.tvBtnUploadAadhaarFront);
        tvAadhaarBackName = pageView.findViewById(R.id.tvAadhaarBackName);
        btnUploadAadhaarBack = pageView.findViewById(R.id.btnUploadAadhaarBack);
        tvBtnUploadAadhaarBack = pageView.findViewById(R.id.tvBtnUploadAadhaarBack);

        if (btnUploadResume != null) {
            btnUploadResume.setOnClickListener(v -> openDocumentPicker(1));
        }
        if (btnUploadSchool10th != null) {
            btnUploadSchool10th.setOnClickListener(v -> openDocumentPicker(2));
        }
        if (btnUploadSchool12th != null) {
            btnUploadSchool12th.setOnClickListener(v -> openDocumentPicker(3));
        }
        if (btnUploadSchoolGrad != null) {
            btnUploadSchoolGrad.setOnClickListener(v -> openDocumentPicker(4));
        }
        if (btnUploadAadhaarFront != null) {
            btnUploadAadhaarFront.setOnClickListener(v -> openDocumentPicker(5));
        }
        if (btnUploadAadhaarBack != null) {
            btnUploadAadhaarBack.setOnClickListener(v -> openDocumentPicker(6));
        }

        // Restore file names if already selected
        if (tvResumeFileName != null && !TextUtils.isEmpty(resumeFileName)) {
            tvResumeFileName.setText(resumeFileName);
            if (tvBtnUploadResume != null)
                tvBtnUploadResume.setText("Change Resume");
        }
        if (tvAadhaarFrontName != null && !TextUtils.isEmpty(aadhaarFrontFileName)) {
            tvAadhaarFrontName.setText(aadhaarFrontFileName);
            if (tvBtnUploadAadhaarFront != null)
                tvBtnUploadAadhaarFront.setText("Replace Front");
        }
        if (tvAadhaarBackName != null && !TextUtils.isEmpty(aadhaarBackFileName)) {
            tvAadhaarBackName.setText(aadhaarBackFileName);
            if (tvBtnUploadAadhaarBack != null)
                tvBtnUploadAadhaarBack.setText("Replace Back");
        }
        if (tvSchool10thFileName != null && !TextUtils.isEmpty(cert10thFileName)) {
            tvSchool10thFileName.setText(cert10thFileName);
            if (tvBtnUploadSchool10th != null)
                tvBtnUploadSchool10th.setText("Replace 10th");
        }
        if (tvSchool12thFileName != null && !TextUtils.isEmpty(cert12thFileName)) {
            tvSchool12thFileName.setText(cert12thFileName);
            if (tvBtnUploadSchool12th != null)
                tvBtnUploadSchool12th.setText("Replace 12th");
        }
        if (tvSchoolGradFileName != null && !TextUtils.isEmpty(certGradFileName)) {
            tvSchoolGradFileName.setText(certGradFileName);
            if (tvBtnUploadSchoolGrad != null)
                tvBtnUploadSchoolGrad.setText("Replace Degree");
        }

        // Vocational Certificate: hidden by default unless uploaded
        if (layoutVocationalItem != null) {
            if (!TextUtils.isEmpty(vocationalCertFileName)) {
                layoutVocationalItem.setVisibility(View.VISIBLE);
                if (tvVocationalTitle != null) {
                    tvVocationalTitle.setText("Vocational Certificate");
                }
                if (tvVocationalSubtitle != null) {
                    tvVocationalSubtitle.setText("Uploaded · " + vocationalCertFileName);
                }
            } else {
                layoutVocationalItem.setVisibility(View.GONE);
            }
        }

        if (btnAddVocationalCert != null) {
            btnAddVocationalCert.setOnClickListener(v -> openDocumentPicker(7));
        }
        if (ivDeleteVocational != null) {
            ivDeleteVocational.setOnClickListener(v -> {
                vocationalCertFileName = "";
                vocationalCertDocBase64 = "";
                if (layoutVocationalItem != null) {
                    layoutVocationalItem.setVisibility(View.GONE);
                }
                Toast.makeText(Register.this, "Certificate removed", Toast.LENGTH_SHORT).show();
                validateCurrentPage();
            });
        }
    }

    // ============================================================
    // PAGE 1 - WORLDWIDE SEARCHABLE CITY SELECTION
    // ============================================================

    public static class CityItem {
        public final String cityName;
        public final String countryName;
        public final String displayName;

        public CityItem(String cityName, String countryName, String displayName) {
            this.cityName = cityName;
            this.countryName = countryName;
            this.displayName = displayName;
        }

        @NonNull
        @Override
        public String toString() {
            return displayName;
        }
    }

    public static class CitySearchAdapter extends ArrayAdapter<CityItem> {
        private final List<CityItem> items;
        private final LayoutInflater inflater;

        public CitySearchAdapter(Context context, List<CityItem> items) {
            super(context, 0, items);
            this.items = items;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return items != null ? items.size() : 0;
        }

        @Override
        public CityItem getItem(int position) {
            return (items != null && position >= 0 && position < items.size()) ? items.get(position) : null;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            }
            TextView tv = convertView.findViewById(android.R.id.text1);
            CityItem item = getItem(position);
            if (item != null && tv != null) {
                tv.setText(item.displayName);
                if (TextUtils.isEmpty(item.cityName)) {
                    // Status, empty or error item
                    tv.setTextColor(Color.parseColor("#64748B"));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                } else {
                    tv.setTextColor(Color.parseColor("#14213D"));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                }
                tv.setPadding(36, 28, 36, 28);
            }
            return convertView;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    results.values = items;
                    results.count = items != null ? items.size() : 0;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    notifyDataSetChanged();
                }
            };
        }
    }

    private void setupSearchableCityPage1() {
        if (regCity == null) {
            return;
        }

        regCity.setFocusable(true);
        regCity.setFocusableInTouchMode(true);
        regCity.setCursorVisible(true);
        regCity.setClickable(true);
        regCity.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        regCity.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));
        regCity.setThreshold(1);

        regCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s != null ? s.toString().trim() : "";

                // Invalidate selected city if user edits text away from previously selected city
                if (!text.equalsIgnoreCase(selectedLivingCityName)) {
                    selectedLivingCityName = "";
                    validateCurrentPage();
                } else if (!TextUtils.isEmpty(selectedLivingCityName)) {
                    // Current text matches the confirmed selected city; do not search again
                    if (citySearchRunnable != null) {
                        citySearchHandler.removeCallbacks(citySearchRunnable);
                    }
                    if (pbCityLoading != null) {
                        pbCityLoading.setVisibility(View.GONE);
                    }
                    return;
                }

                if (citySearchRunnable != null) {
                    citySearchHandler.removeCallbacks(citySearchRunnable);
                }

                if (text.length() >= 2) {
                    citySearchRunnable = () -> performCitySearch(text);
                    citySearchHandler.postDelayed(citySearchRunnable, 300);
                } else {
                    if (pbCityLoading != null) {
                        pbCityLoading.setVisibility(View.GONE);
                    }
                    if (regCity != null && regCity.isPopupShowing()) {
                        regCity.dismissDropDown();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        regCity.setOnClickListener(v -> {
            if (TextUtils.isEmpty(selectedLivingCityName) && regCity.getText() != null
                    && regCity.getText().toString().trim().length() >= 2) {
                if (!regCity.isPopupShowing()) {
                    performCitySearch(regCity.getText().toString().trim());
                }
            }
        });

        regCity.setOnItemClickListener((parent, view, position, id) -> {
            CityItem item = (CityItem) parent.getItemAtPosition(position);
            if (item == null || TextUtils.isEmpty(item.cityName)) {
                if (regCity.isPopupShowing()) {
                    regCity.dismissDropDown();
                }
                return;
            }
            selectedLivingCityName = item.displayName;
            formCityName = item.displayName;
            if (citySearchRunnable != null) {
                citySearchHandler.removeCallbacks(citySearchRunnable);
            }
            getAppRequestQueue().cancelAll(TAG_CITY_SEARCH);
            if (pbCityLoading != null) {
                pbCityLoading.setVisibility(View.GONE);
            }
            regCity.setText(item.displayName, false);
            regCity.dismissDropDown();
            regCity.clearFocus();
            validateCurrentPage();
        });
    }

    private void performCitySearch(String query) {
        if (isFinishing() || isDestroyed() || regCity == null)
            return;

        // If this text is already the selected city, do not search
        if (!TextUtils.isEmpty(selectedLivingCityName) && query.equalsIgnoreCase(selectedLivingCityName)) {
            if (pbCityLoading != null) {
                pbCityLoading.setVisibility(View.GONE);
            }
            return;
        }

        if (pbCityLoading != null) {
            pbCityLoading.setVisibility(View.VISIBLE);
        }

        getAppRequestQueue().cancelAll(TAG_CITY_SEARCH);

        String url = Constant.SearchCityUrl + "?query=" + Uri.encode(query);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    if (pbCityLoading != null) {
                        pbCityLoading.setVisibility(View.GONE);
                    }

                    if (regCity == null || isFinishing() || isDestroyed())
                        return;

                    // If user has already confirmed a city selection, do not pop up suggestions
                    if (!TextUtils.isEmpty(selectedLivingCityName)) {
                        return;
                    }

                    String currentText = regCity.getText() != null ? regCity.getText().toString().trim() : "";
                    if (currentText.length() < 2) {
                        if (regCity.isPopupShowing()) {
                            regCity.dismissDropDown();
                        }
                        return;
                    }

                    if (response == null || !response.trim().startsWith("{")) {
                        showCitySearchState("Unable to search cities. Please retry.");
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(response);
                        if (!json.optBoolean("status", false)) {
                            showCitySearchState("City search error. Please retry.");
                            return;
                        }

                        JSONArray arr = json.optJSONArray("cities");
                        List<CityItem> cityList = new ArrayList<>();
                        Set<String> seen = new HashSet<>();

                        if (arr != null && arr.length() > 0) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                String cName = obj.optString("city_name", "").trim();
                                String country = obj.optString("country_name", "").trim();
                                String dName = obj.optString("display_name", "").trim();
                                if (TextUtils.isEmpty(dName)) {
                                    dName = !TextUtils.isEmpty(country) ? cName + ", " + country : cName;
                                }
                                if (!TextUtils.isEmpty(dName) && !seen.contains(dName.toLowerCase())) {
                                    seen.add(dName.toLowerCase());
                                    cityList.add(new CityItem(cName, country, dName));
                                }
                            }
                        }

                        if (cityList.isEmpty()) {
                            showCitySearchState("No matching cities found");
                        } else {
                            displayCitySuggestions(cityList);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing city search response", e);
                        showCitySearchState("Error loading city suggestions");
                    }
                },
                error -> {
                    if (pbCityLoading != null) {
                        pbCityLoading.setVisibility(View.GONE);
                    }
                    if (regCity == null || isFinishing() || isDestroyed())
                        return;
                    if (!TextUtils.isEmpty(selectedLivingCityName))
                        return;
                    showCitySearchState("Network error searching cities");
                });

        request.setRetryPolicy(new DefaultRetryPolicy(12000, 1, 1.0f));
        getAppRequestQueue().add(request.setTag(TAG_CITY_SEARCH));
    }

    private void displayCitySuggestions(List<CityItem> cityList) {
        if (regCity == null || isFinishing() || isDestroyed())
            return;
        if (!TextUtils.isEmpty(selectedLivingCityName))
            return;
        CitySearchAdapter adapter = new CitySearchAdapter(this, cityList);
        regCity.setAdapter(adapter);
        regCity.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));
        regCity.post(() -> {
            if (regCity != null && !isFinishing() && !isDestroyed() && TextUtils.isEmpty(selectedLivingCityName)) {
                regCity.showDropDown();
            }
        });
    }

    private void showCitySearchState(String message) {
        if (regCity == null || isFinishing() || isDestroyed())
            return;
        if (!TextUtils.isEmpty(selectedLivingCityName))
            return;
        List<CityItem> emptyList = new ArrayList<>();
        emptyList.add(new CityItem("", "", message));
        CitySearchAdapter adapter = new CitySearchAdapter(this, emptyList);
        regCity.setAdapter(adapter);
        regCity.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));
        regCity.post(() -> {
            if (regCity != null && !isFinishing() && !isDestroyed() && TextUtils.isEmpty(selectedLivingCityName)) {
                regCity.showDropDown();
            }
        });
    }

    // ============================================================
    // PAGE 3 - CITY PREFERENCES
    // ============================================================

    private void openCityPickerPage3() {
        toggleArrowDirection(true);
        if (cityLoadedP3 && !cityNamesP3.isEmpty()) {
            showCityDialogPage3();
        } else {
            isCityPickerPendingP3 = true;
            loadCities(1);
        }
    }

    private void showCityDialogPage3() {
        if (cityNamesP3.isEmpty()) {
            openCityPickerPage3();
            return;
        }

        String[] cities = cityNamesP3.toArray(new String[0]);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.select_dialog_item,
                cities) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView.setPadding(40, 24, 40, 24);
                textView.setBackgroundColor(Color.WHITE);
                return view;
            }
        };

        AlertDialog cityDialog = new AlertDialog.Builder(this, R.style.WhiteAlertDialog)
                .setTitle("Select City")
                .setAdapter(cityAdapter, (dialog, which) -> {
                    if (which >= 0 && which < cityIdsP3.size()) {
                        String newCityId = cityIdsP3.get(which);
                        String newCityName = cityNamesP3.get(which);
                        onCitySelectedPage3(newCityId, newCityName);
                    }
                    toggleArrowDirection(false);
                })
                .create();

        cityDialog.setOnDismissListener(dialog -> toggleArrowDirection(false));

        cityDialog.setOnShowListener(dialogInterface -> {
            Window w = cityDialog.getWindow();
            if (w != null) {
                // Force whole dialog window white
                w.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                // Title
                int titleId = getResources().getIdentifier("alertTitle", "id", "android");
                TextView title = cityDialog.findViewById(titleId);
                if (title != null) {
                    title.setTextColor(Color.BLACK);
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setPadding(40, 30, 40, 20);
                    title.setBackgroundColor(Color.WHITE);
                }

                // List
                ListView listView = cityDialog.getListView();
                if (listView != null) {
                    listView.setBackgroundColor(Color.WHITE);
                    listView.setDivider(new ColorDrawable(Color.LTGRAY));
                    listView.setDividerHeight(1);
                    listView.setPadding(0, 10, 0, 10);
                    listView.setVerticalScrollBarEnabled(true);
                }
            }
        });

        cityDialog.show();

        if (cityDialog.getWindow() != null) {
            cityDialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private void setupCityDropdownPage3() {
        if (actvCityDropdown == null) {
            return;
        }

        refreshCityDropdownPage3();

        actvCityDropdown.setOnClickListener(v -> openCityPickerPage3());
        actvCityDropdown.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.clearFocus();
                openCityPickerPage3();
                return true;
            }
            return false;
        });

        actvCityDropdown.setOnItemClickListener((parent, view, position, id) -> {
            Object selectedItem = parent.getItemAtPosition(position);
            if (selectedItem == null)
                return;

            String selectedCityName = selectedItem.toString();
            int which = cityNamesP3.indexOf(selectedCityName);
            if (which >= 0 && which < cityIdsP3.size()) {
                onCitySelectedPage3(cityIdsP3.get(which), selectedCityName);
            }
        });

        if (cityNamesP3.isEmpty() && !cityLoadedP3 && !cityLoadingP3) {
            loadCities(1);
        }
    }

    private void refreshCityDropdownPage3() {
        if (actvCityDropdown == null)
            return;

        actvCityDropdown.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));

        if (!cityNamesP3.isEmpty()) {
            ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    cityNamesP3) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        tv.setTextColor(Color.BLACK);
                        tv.setBackgroundColor(Color.WHITE);
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        tv.setTextColor(Color.BLACK);
                        tv.setBackgroundColor(Color.WHITE);
                        tv.setPadding(32, 24, 32, 24);
                    }
                    return view;
                }
            };
            actvCityDropdown.setAdapter(cityAdapter);
        }

        if (!TextUtils.isEmpty(selectedCityIdP3) && !cityNamesP3.isEmpty()) {
            int idx = cityIdsP3.indexOf(selectedCityIdP3);
            if (idx >= 0 && idx < cityNamesP3.size()) {
                actvCityDropdown.setText(cityNamesP3.get(idx), false);
            }
        } else {
            actvCityDropdown.setText("", false);
        }
    }

    private void onCitySelectedPage3(String newCityId, String newCityName) {
        boolean cityChanged = !newCityId.equals(selectedCityIdP3);
        selectedCityIdP3 = newCityId;

        if (actvCityDropdown != null) {
            actvCityDropdown.setText(newCityName, false);
        }

        if (cityChanged) {
            allAreas.clear();
            selectedAreaIds.clear();
            areasLoaded = false;
            isLoadingAreas = false;
            areasLoadedCityId = "";

            if (areaAdapter != null) {
                areaAdapter.clear();
            }

            updateAreaCount();
        }

        loadAreasByCity();
        validateCurrentPage();
    }

    // ============================================================
    // COMMON CITY LOADER (STATUS = 0 FOR P1, STATUS = 1 FOR P3)
    // ============================================================

    private static final String[][] DEFAULT_CITIES = {
            { "1", "Delhi" },
            { "2", "New Delhi" },
            { "3", "Kolkata" },
            { "4", "Bengaluru" },
            { "5", "Hyderabad" },
            { "6", "Chennai" },
            { "7", "Pune" },
            { "8", "Ahmedabad" },
            { "9", "Gurgaon" },
            { "10", "Noida" }
    };

    private void populateDefaultCities(int status) {
        cityNamesP3.clear();
        cityIdsP3.clear();

        for (String[] city : DEFAULT_CITIES) {
            cityIdsP3.add(city[0]);
            cityNamesP3.add(city[1]);
        }

        cityLoadedP3 = true;
        cityLoadingP3 = false;
        refreshCityDropdownPage3();
        toggleArrowDirection(false);
        if (isCityPickerPendingP3) {
            isCityPickerPendingP3 = false;
            showCityDialogPage3();
        }
    }

    private void loadCities(int status) {
        if (cityLoadedP3 && !cityNamesP3.isEmpty()) {
            if (isCityPickerPendingP3) {
                isCityPickerPendingP3 = false;
                showCityDialogPage3();
            }
            return;
        }

        // Prevent duplicate calls while one is already in flight
        if (cityLoadingP3) {
            return;
        }

        cityLoadingP3 = true;
        toggleArrowDirection(true);

        String url = "https://jiifit.com/Jifiit/get_cities_for_expert.php?status=1";
        Log.d(TAG, "Fetching cities with URL: " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    cityLoadingP3 = false;

                    if (TextUtils.isEmpty(response) || !response.trim().startsWith("{")) {
                        Log.w(TAG, "Non-JSON response from city API (WAF challenge or HTML). Loading default cities.");
                        populateDefaultCities(1);
                        return;
                    }

                    try {
                        JSONObject jsonResponse = new JSONObject(response.trim());
                        boolean apiStatus = jsonResponse.optBoolean("status", false);

                        if (!apiStatus) {
                            Log.w(TAG, "City API returned status=false. Loading default cities.");
                            populateDefaultCities(1);
                            return;
                        }

                        JSONArray citiesArray = jsonResponse.optJSONArray("cities");

                        if (citiesArray == null || citiesArray.length() == 0) {
                            populateDefaultCities(1);
                            return;
                        }

                        cityNamesP3.clear();
                        cityIdsP3.clear();

                        for (int i = 0; i < citiesArray.length(); i++) {
                            JSONObject cityObject = citiesArray.optJSONObject(i);
                            if (cityObject == null)
                                continue;

                            String cId = cityObject.optString("id", "").trim();
                            String cName = cityObject.optString("name", "").trim();
                            if (!TextUtils.isEmpty(cName)) {
                                cityIdsP3.add(cId);
                                cityNamesP3.add(cName);
                            }
                        }

                        if (cityNamesP3.isEmpty()) {
                            populateDefaultCities(1);
                            return;
                        }

                        cityLoadedP3 = true;
                        refreshCityDropdownPage3();
                        toggleArrowDirection(false);
                        if (isCityPickerPendingP3) {
                            isCityPickerPendingP3 = false;
                            showCityDialogPage3();
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "City parsing failed, fallback to default cities", e);
                        populateDefaultCities(1);
                    }
                },
                error -> {
                    cityLoadingP3 = false;
                    Log.w(TAG, "City API error, falling back to default cities: " + error.getMessage());
                    populateDefaultCities(1);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(
                new DefaultRetryPolicy(
                        8000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        getAppRequestQueue().add(request.setTag(TAG_PREFETCH));
    }

    private void toggleArrowDirection(boolean loading) {
        runOnUiThread(() -> {
            View pageView = getCurrentPageView();
            if (pageView == null)
                return;

            View ivCityArrowP1 = pageView.findViewById(R.id.ivCityDropdownArrow);
            View ivCityArrowP3 = pageView.findViewById(R.id.ivCityDropdownArrow);

            View arrowView = ivCityArrowP3 != null ? ivCityArrowP3 : ivCityArrowP1;

            if (arrowView != null) {
                if (loading) {
                    arrowView.animate()
                            .rotation(180f)
                            .setDuration(200)
                            .start();
                    arrowView.setEnabled(false);
                } else {
                    arrowView.animate()
                            .rotation(0f)
                            .setDuration(200)
                            .start();
                    arrowView.setEnabled(true);
                }
            }
        });
    }

    private void loadDataByCityId(String cityId) {
        if (TextUtils.isEmpty(cityId)) {
            return;
        }

        localityNames.clear();
        localityIds.clear();
        localityHubIds.clear();
        selectedLocalityId = "";
        selectedHubId = "";

        String url = Constant.get_localities + "?city_id=" + Uri.encode(cityId);
        Log.d(TAG, "Loading localities from: " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    if (TextUtils.isEmpty(response) || !response.trim().startsWith("{")) {
                        Log.w(TAG, "Non-JSON response from localities API");
                        return;
                    }
                    try {
                        JSONObject jsonResponse = new JSONObject(response.trim());
                        JSONArray localityArray = null;
                        if (jsonResponse.has("localities")) {
                            localityArray = jsonResponse.optJSONArray("localities");
                        } else if (jsonResponse.has("data")) {
                            localityArray = jsonResponse.optJSONArray("data");
                        }

                        if (localityArray != null && localityArray.length() > 0) {
                            for (int i = 0; i < localityArray.length(); i++) {
                                JSONObject obj = localityArray.optJSONObject(i);
                                if (obj == null)
                                    continue;

                                String id = obj.optString("id", "");
                                String name = obj.optString("name", "");
                                String hubId = obj.optString("hub_id", "");

                                if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(name)) {
                                    localityIds.add(id.trim());
                                    localityNames.add(name.trim());
                                    localityHubIds.add(hubId.trim());
                                }
                            }
                        }

                        Log.d(TAG, "Loaded " + localityNames.size() + " localities for city ID: " + cityId);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse locality/hub data", e);
                    }
                },
                error -> {
                    String errorMsg = "Failed to load area hubs";
                    if (error instanceof TimeoutError) {
                        errorMsg = "Timeout loading area hubs";
                    } else if (error.networkResponse != null) {
                        errorMsg = "HTTP Error " + error.networkResponse.statusCode;
                    }
                    Log.e(TAG, "Locality API error: " + errorMsg, error);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(
                new DefaultRetryPolicy(
                        10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        getAppRequestQueue().add(request.setTag(TAG_PREFETCH));
    }

    // ============================================================
    // PAGE 3 - AREA PREFERENCES
    // ============================================================

    private void setupAreaPreferences() {
        if (rvAreas == null) {
            return;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvAreas.setLayoutManager(layoutManager);
        rvAreas.setNestedScrollingEnabled(true);
        rvAreas.setHasFixedSize(true);
        rvAreas.setItemAnimator(null);

        areaAdapter = new AreaAdapter(
                allAreas,
                selectedAreaIds,
                () -> {
                    updateAreaCount();
                    validateCurrentPage();
                });
        rvAreas.setAdapter(areaAdapter);

        updateAreaCount();

        if (!TextUtils.isEmpty(selectedCityIdP3) && allAreas.isEmpty()) {
            loadAreasByCity();
        }
    }

    private int getRequiredAreasCount() {
        if (allAreas == null || allAreas.isEmpty()) {
            return MIN_REQUIRED_AREAS;
        }
        return Math.min(MIN_REQUIRED_AREAS, allAreas.size());
    }

    private void updateAreaCount() {
        int count = selectedAreaIds.size();
        int required = getRequiredAreasCount();

        if (tvTotalAreas != null) {
            if (count < required) {
                tvTotalAreas.setText(count + " / " + required + " selected");
            } else {
                tvTotalAreas.setText(count + " locations selected");
            }
        }

        if (tvMinAreasHint != null) {
            if (count < required) {
                int remaining = required - count;
                tvMinAreasHint.setText("Minimum select " + required + " areas (" + remaining + " more required)");
                tvMinAreasHint.setTextColor(Color.parseColor("#DC2626"));
            } else {
                tvMinAreasHint.setText("✓ Minimum requirement met (" + count + " selected)");
                tvMinAreasHint.setTextColor(Color.parseColor("#16A34A"));
            }
        }

        if (llNoAreasSelected != null && rvAreas != null) {
            if (isLoadingAreas) {
                llNoAreasSelected.setVisibility(View.GONE);
                rvAreas.setVisibility(View.GONE);
            } else if (allAreas.isEmpty() || TextUtils.isEmpty(selectedCityIdP3)) {
                llNoAreasSelected.setVisibility(View.VISIBLE);
                rvAreas.setVisibility(View.GONE);
                if (tvEmptyTitle != null) {
                    if (TextUtils.isEmpty(selectedCityIdP3)) {
                        tvEmptyTitle.setText("Select a city to view hubs");
                        if (tvEmptySubtitle != null) {
                            tvEmptySubtitle.setText(
                                    "Choose your primary city above to display all available operating locations.");
                        }
                    } else {
                        tvEmptyTitle.setText("No locations available");
                        if (tvEmptySubtitle != null) {
                            tvEmptySubtitle.setText("No operating hubs found for the selected city.");
                        }
                    }
                }
            } else {
                llNoAreasSelected.setVisibility(View.GONE);
                if (rvAreas.getVisibility() != View.VISIBLE) {
                    rvAreas.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void loadAreasByCity() {
        if (TextUtils.isEmpty(selectedCityIdP3)) {
            return;
        }

        final String requestCityId = selectedCityIdP3;

        isLoadingAreas = true;
        areasLoaded = false;

        allAreas.clear();
        selectedAreaIds.clear();

        if (areaAdapter != null) {
            areaAdapter.clear();
        }

        if (llAreasLoading != null) {
            llAreasLoading.setVisibility(View.VISIBLE);
        }
        if (llNoAreasSelected != null) {
            llNoAreasSelected.setVisibility(View.GONE);
        }
        if (rvAreas != null) {
            rvAreas.setVisibility(View.GONE);
        }

        updateAreaCount();
        validateCurrentPage();

        String url = Constant.get_localities + "?city_id=" + Uri.encode(selectedCityIdP3);
        Log.d(TAG, "Area API URL: " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                responseStr -> {
                    // Prevent race condition: ignore stale response if user selected another city
                    if (!requestCityId.equals(selectedCityIdP3)) {
                        Log.d(TAG, "Ignoring stale area response for city " + requestCityId + ", active city is "
                                + selectedCityIdP3);
                        return;
                    }

                    isLoadingAreas = false;
                    if (llAreasLoading != null) {
                        llAreasLoading.setVisibility(View.GONE);
                    }

                    try {
                        allAreas.clear();
                        String trimmed = responseStr != null ? responseStr.trim() : "";
                        JSONArray dataArray = null;

                        if (trimmed.startsWith("{")) {
                            JSONObject json = new JSONObject(trimmed);
                            if (json.has("data")) {
                                dataArray = json.optJSONArray("data");
                            } else if (json.has("areas")) {
                                dataArray = json.optJSONArray("areas");
                            }
                        } else if (trimmed.startsWith("[")) {
                            dataArray = new JSONArray(trimmed);
                        }

                        if (dataArray != null) {
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject item = dataArray.optJSONObject(i);
                                if (item == null)
                                    continue;

                                String areaId = item.optString("id", "");
                                String areaName = item.optString("name", "");
                                String areaDesc = item.optString("description", "");

                                if (!TextUtils.isEmpty(areaId) && !TextUtils.isEmpty(areaName)) {
                                    allAreas.add(new AreaAdapter.Area(areaId.trim(), areaName.trim(), areaDesc.trim()));
                                }
                            }
                        }

                        Collections.sort(allAreas, (a, b) -> a.name.trim().compareToIgnoreCase(b.name.trim()));

                        areasLoaded = true;
                        areasLoadedCityId = selectedCityIdP3;

                        if (areaAdapter != null) {
                            areaAdapter.setAreas(allAreas);
                        }

                        updateAreaCount();
                        validateCurrentPage();

                        if (allAreas.isEmpty()) {
                            Toast.makeText(Register.this, "No areas available for this city", Toast.LENGTH_SHORT)
                                    .show();
                        }

                    } catch (Exception e) {
                        areasLoaded = false;
                        areasLoadedCityId = "";
                        Log.e(TAG, "Area parsing failed", e);
                        Toast.makeText(Register.this, "Unable to read area data", Toast.LENGTH_SHORT).show();
                        updateAreaCount();
                        validateCurrentPage();
                    }
                },
                error -> {
                    if (!requestCityId.equals(selectedCityIdP3)) {
                        return;
                    }
                    isLoadingAreas = false;
                    areasLoaded = false;
                    areasLoadedCityId = "";
                    if (llAreasLoading != null) {
                        llAreasLoading.setVisibility(View.GONE);
                    }

                    String errorMessage = "Unknown error";
                    if (error instanceof TimeoutError) {
                        errorMessage = "Request timeout";
                    } else if (error instanceof com.android.volley.NoConnectionError) {
                        errorMessage = "No internet connection";
                    } else if (error.networkResponse != null) {
                        errorMessage = "HTTP " + error.networkResponse.statusCode;
                    }

                    Log.e(TAG, "Area API failed: " + errorMessage, error);
                    Toast.makeText(Register.this, "Area loading failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    updateAreaCount();
                    validateCurrentPage();
                });

        request.setRetryPolicy(
                new DefaultRetryPolicy(
                        15000,
                        1,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        getAppRequestQueue().add(request.setTag(TAG_PREFETCH));
    }

    private void setupAreaDropdownAdapter() {
        // Obsolete: list is now rendered directly
    }

    private void showAllAreasDropdown() {
        // Obsolete: list is now rendered directly
    }

    // ============================================================
    // PAGE 1 - AADHAAR FORMATTER
    // ============================================================

    private void setupAadhaarFormatter() {
        if (regAadhaar == null) {
            return;
        }

        if (aadhaarFormatterWatcher != null) {
            regAadhaar.removeTextChangedListener(aadhaarFormatterWatcher);
        }

        aadhaarFormatterWatcher = new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting)
                    return;

                isFormatting = true;
                String input = s.toString().replaceAll("\\s+", "");

                if (input.length() > 12) {
                    input = input.substring(0, 12);
                }

                formAadhaar = input;

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    formatted.append(input.charAt(i));
                    if (i == 3 || i == 7) {
                        formatted.append(" ");
                    }
                }

                regAadhaar.setText(formatted.toString());
                regAadhaar.setSelection(formatted.length());
                isFormatting = false;
                validateCurrentPage();
            }
        };

        regAadhaar.addTextChangedListener(aadhaarFormatterWatcher);
    }

    // ============================================================
    // PAGE 1 - GENDER
    // ============================================================

    private void setupGenderChips() {
        if (rgGender == null) {
            return;
        }

        // Restore previously selected gender if already chosen
        if (!TextUtils.isEmpty(selectedGenderString)) {
            for (int i = 0; i < rgGender.getChildCount(); i++) {
                View child = rgGender.getChildAt(i);
                if (child instanceof RadioButton) {
                    RadioButton rb = (RadioButton) child;
                    if (selectedGenderString.equalsIgnoreCase(rb.getText().toString().trim())) {
                        rgGender.check(rb.getId());
                        break;
                    }
                }
            }
        }

        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRb = group.findViewById(checkedId);
            if (selectedRb != null && selectedRb.isChecked()) {
                selectedGenderString = selectedRb.getText().toString().trim();
            }

            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (!(child instanceof RadioButton))
                    continue;

                RadioButton rb = (RadioButton) child;
                boolean selected = rb.getId() == checkedId;

                rb.setBackgroundResource(
                        selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);

                rb.setTextColor(
                        getResources().getColor(
                                selected ? R.color.white : R.color.Text_Blue));
            }
            validateCurrentPage();
        });
    }

    // ============================================================
    // PAGE 1 - DATE OF BIRTH
    // ============================================================

    private void setupDobPicker() {
        if (regDob == null) {
            return;
        }

        regDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    Register.this,
                    (view, year, month, day) -> {
                        Calendar dob = Calendar.getInstance();
                        dob.set(year, month, day);

                        selectedDob = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dob.getTime());
                        regDob.setText(
                                new SimpleDateFormat("MM / dd / yyyy", Locale.getDefault()).format(dob.getTime()));

                        Calendar today = Calendar.getInstance();
                        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                        int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);

                        if (today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
                            months--;
                        }

                        if (months < 0) {
                            years--;
                            months += 12;
                        }

                        if (years < 18) {
                            tvAge.setText(R.string.age_warning);
                            tvAge.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        } else {
                            tvAge.setText("Your current age is : " + years + " years and " + months + " months");
                            tvAge.setTextColor(getResources().getColor(R.color.green_success));
                        }

                        validateCurrentPage();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            dialog.getDatePicker().setMaxDate(new Date().getTime());
            dialog.show();
        });
    }

    // ============================================================
    // PAGE 1 - PHOTO UPLOAD
    // ============================================================

    private void setupPhotoUpload() {
        if (btnUploadPhoto == null) {
            return;
        }

        btnUploadPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });
    }

    // ============================================================
    // PAGE 2 - CATEGORY & SERVICE SELECTION
    // ============================================================

    private Set<String> getSelectedServicesForCategory(String categoryName) {
        if (TextUtils.isEmpty(categoryName)) {
            return Collections.emptySet();
        }
        Set<String> set = categorySelectedServices.get(categoryName);
        if (set == null) {
            set = new LinkedHashSet<>();
            categorySelectedServices.put(categoryName, set);
        }
        return set;
    }

    private void syncCategoryStrings() {
        selectedCategoryId = TextUtils.join(",", selectedCategoryIds);
        selectedCategory = TextUtils.join(", ", selectedCategories);
        syncSelectedServices();
    }

    private void syncSelectedServices() {
        selectedServices.clear();
        for (String catName : selectedCategories) {
            Set<String> set = categorySelectedServices.get(catName);
            if (set != null) {
                selectedServices.addAll(set);
            }
        }
    }

    private boolean isCategorySelected(String catId, String catName) {
        if (!TextUtils.isEmpty(catId) && selectedCategoryIds.contains(catId)) {
            return true;
        }
        if (!TextUtils.isEmpty(catName) && selectedCategories.contains(catName)) {
            return true;
        }
        if (!TextUtils.isEmpty(selectedCategoryId)) {
            String[] ids = selectedCategoryId.split(",");
            for (String id : ids) {
                if (id.trim().equals(catId))
                    return true;
            }
        }
        if (!TextUtils.isEmpty(selectedCategory)) {
            String[] names = selectedCategory.split(",");
            for (String name : names) {
                if (name.trim().equalsIgnoreCase(catName))
                    return true;
            }
        }
        return false;
    }

    private void loadCategoriesFromApi() {
        if (isCategoriesLoading) {
            return;
        }
        isCategoriesLoading = true;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                Constant.getTherapistCategories,
                response -> {
                    isCategoriesLoading = false;
                    Log.d(TAG, "Categories loaded: "
                            + (response != null && response.length() > 200 ? response.substring(0, 200) : response));
                    List<CategoryItem> parsed = parseCategoriesResponse(response);
                    if (!parsed.isEmpty()) {
                        dynamicCategories.clear();
                        dynamicCategories.addAll(parsed);
                        if (rgCategory != null) {
                            buildCategoryRows();
                        }
                    } else {
                        Log.w(TAG,
                                "Categories API returned non-JSON (WAF challenge or HTML). Retaining default categories.");
                        initDefaultCategories();
                        if (rgCategory != null && rgCategory.getChildCount() <= 1) {
                            buildCategoryRows();
                        }
                    }
                },
                error -> {
                    isCategoriesLoading = false;
                    Log.w(TAG, "Failed to load categories from API (" + error.getMessage()
                            + "). Retaining default categories.");
                    initDefaultCategories();
                    if (rgCategory != null && rgCategory.getChildCount() <= 1) {
                        buildCategoryRows();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                8000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        getAppRequestQueue().add(request.setTag(TAG_PREFETCH));
    }

    private List<CategoryItem> parseCategoriesResponse(String response) {
        List<CategoryItem> result = new ArrayList<>();
        if (TextUtils.isEmpty(response)) {
            return result;
        }

        try {
            String trimmed = response.trim();
            JSONArray array = null;

            if (trimmed.startsWith("[")) {
                array = new JSONArray(trimmed);
            } else if (trimmed.startsWith("{")) {
                JSONObject obj = new JSONObject(trimmed);
                if (obj.has("categories")) {
                    array = obj.optJSONArray("categories");
                } else if (obj.has("data")) {
                    array = obj.optJSONArray("data");
                }
            }

            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    String id = item.optString("id");
                    String name = item.optString("name");
                    String subtitle = item.optString("subtitle", item.optString("description", ""));

                    if (TextUtils.isEmpty(subtitle)) {
                        for (String[] def : DEFAULT_CATEGORIES) {
                            if (def[0].equals(id) || def[1].equalsIgnoreCase(name)) {
                                subtitle = def[2];
                                break;
                            }
                        }
                    }

                    if (!TextUtils.isEmpty(name)) {
                        result.add(new CategoryItem(id, name, subtitle));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing categories response: " + e.getMessage());
        }

        return result;
    }

    private void buildCategoryRows() {
        if (rgCategory == null) {
            return;
        }

        initDefaultCategories();

        rgCategory.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (CategoryItem catItem : dynamicCategories) {
            View row = inflater.inflate(R.layout.item_category_option, rgCategory, false);
            CheckBox cb = row.findViewById(R.id.cbOption);
            TextView title = row.findViewById(R.id.tvCatTitle);
            TextView subtitle = row.findViewById(R.id.tvCatSubtitle);

            if (cb != null) {
                cb.setClickable(false);
                cb.setFocusable(false);
            }

            if (title != null) {
                title.setText(catItem.name);
            }
            if (subtitle != null) {
                if (!TextUtils.isEmpty(catItem.subtitle)) {
                    subtitle.setText(catItem.subtitle);
                    subtitle.setVisibility(View.VISIBLE);
                } else {
                    subtitle.setVisibility(View.GONE);
                }
            }

            final String catId = catItem.id;
            final String catName = catItem.name;

            boolean isCurrent = isCategorySelected(catId, catName);
            if (cb != null) {
                cb.setChecked(isCurrent);
            }
            row.setBackgroundResource(
                    isCurrent ? R.drawable.bg_category_row_selected : R.drawable.bg_category_row);

            row.setOnClickListener(view -> toggleCategory(catId, catName));

            rgCategory.addView(row);
        }
    }

    private void toggleCategory(String catId, String catName) {
        if (isCategorySelected(catId, catName)) {
            selectedCategoryIds.remove(catId);
            selectedCategories.remove(catName);
            categorySelectedServices.remove(catName);
        } else {
            if (!TextUtils.isEmpty(catId)) {
                selectedCategoryIds.add(catId);
            }
            if (!TextUtils.isEmpty(catName)) {
                selectedCategories.add(catName);
            }
        }

        syncCategoryStrings();
        highlightCategoryRows();
        renderServicesForSelectedCategories(true);
        validateCurrentPage();
    }

    private void showCategoriesLoadingView() {
        if (rgCategory == null)
            return;
        rgCategory.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText("Loading categories...");
        tv.setTextColor(Color.parseColor("#64748B"));
        tv.setTextSize(14f);
        tv.setPadding(24, 28, 24, 28);
        rgCategory.addView(tv);
    }

    private void showCategoriesErrorView() {
        if (rgCategory == null)
            return;
        rgCategory.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText("Unable to load categories. Tap to retry.");
        tv.setTextColor(Color.parseColor("#EF4444"));
        tv.setTextSize(14f);
        tv.setPadding(24, 28, 24, 28);
        tv.setOnClickListener(v -> loadCategoriesFromApi());
        rgCategory.addView(tv);
    }

    private void showCategoriesEmptyView() {
        if (rgCategory == null)
            return;
        rgCategory.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText("No categories found.");
        tv.setTextColor(Color.parseColor("#64748B"));
        tv.setTextSize(14f);
        tv.setPadding(24, 28, 24, 28);
        rgCategory.addView(tv);
    }

    private void selectCategory(String categoryName, boolean smoothScroll) {
        if (TextUtils.isEmpty(categoryName))
            return;
        String catId = "";
        for (CategoryItem item : dynamicCategories) {
            if (item.name.equalsIgnoreCase(categoryName)) {
                catId = item.id;
                break;
            }
        }
        selectCategory(catId, categoryName, smoothScroll);
    }

    private void selectCategory(String categoryId, String categoryName, boolean smoothScroll) {
        if (!TextUtils.isEmpty(categoryId)) {
            selectedCategoryIds.add(categoryId);
        }
        if (!TextUtils.isEmpty(categoryName)) {
            selectedCategories.add(categoryName);
        }
        syncCategoryStrings();
        highlightCategoryRows();
        renderServicesForSelectedCategories(smoothScroll);
        validateCurrentPage();
    }

    private void highlightCategoryRows() {
        if (rgCategory == null) {
            return;
        }

        for (int i = 0; i < rgCategory.getChildCount(); i++) {
            View row = rgCategory.getChildAt(i);
            CheckBox cb = row.findViewById(R.id.cbOption);
            TextView title = row.findViewById(R.id.tvCatTitle);

            if (title == null)
                continue;
            String rowName = title.getText().toString();
            String rowId = "";
            for (CategoryItem item : dynamicCategories) {
                if (item.name.equals(rowName)) {
                    rowId = item.id;
                    break;
                }
            }

            boolean isMatch = isCategorySelected(rowId, rowName);
            if (cb != null) {
                cb.setChecked(isMatch);
            }
            row.setBackgroundResource(
                    isMatch ? R.drawable.bg_category_row_selected : R.drawable.bg_category_row);
        }
    }

    // ============================================================
    // PAGE 2 - SERVICE NAMES SELECTION
    // ============================================================

    private void showServiceSectionForCategory(String categoryName) {
        renderServicesForSelectedCategories(false);
    }

    private void showServiceSectionForCategory(String categoryId, String categoryName) {
        renderServicesForSelectedCategories(false);
    }

    private void renderServicesForSelectedCategories(boolean smoothScroll) {
        if (llServiceSection == null || llServiceItemsContainer == null) {
            return;
        }

        if (selectedCategories.isEmpty()) {
            llServiceSection.setVisibility(View.GONE);
            if (dividerServiceSectionBottom != null) {
                dividerServiceSectionBottom.setVisibility(View.GONE);
            }
            selectedServices.clear();
            updateSelectedServicesCount();
            return;
        }

        llServiceSection.setVisibility(View.VISIBLE);
        if (dividerServiceSectionBottom != null) {
            dividerServiceSectionBottom.setVisibility(View.VISIBLE);
        }

        if (tvServiceCategoryHeader != null) {
            if (selectedCategories.size() == 1) {
                tvServiceCategoryHeader.setText(getServiceHeaderForCategory(selectedCategories.iterator().next()));
            } else {
                tvServiceCategoryHeader.setText("SERVICES FOR " + selectedCategories.size() + " SELECTED CATEGORIES");
            }
        }

        llServiceItemsContainer.removeAllViews();

        for (String catName : selectedCategories) {
            String catId = "";
            for (CategoryItem item : dynamicCategories) {
                if (item.name.equalsIgnoreCase(catName)) {
                    catId = item.id;
                    break;
                }
            }

            if (selectedCategories.size() > 1) {
                TextView catHeader = new TextView(this);
                catHeader.setText(catName.toUpperCase(Locale.ROOT) + " SERVICES");
                catHeader.setTextColor(Color.parseColor("#1E293B"));
                catHeader.setTextSize(13f);
                catHeader.setTypeface(null, Typeface.BOLD);
                catHeader.setPadding(0, 20, 0, 8);
                llServiceItemsContainer.addView(catHeader);
            }

            List<String> services = dynamicCategoryServicesCache.get(catName);
            if (services == null && !TextUtils.isEmpty(catId)) {
                services = dynamicCategoryServicesCache.get(catId);
            }

            boolean isFailed = failedCategories.contains(catName) || (!TextUtils.isEmpty(catId) && failedCategories.contains(catId));
            boolean isLoading = loadingCategories.contains(catName) || (!TextUtils.isEmpty(catId) && loadingCategories.contains(catId));

            if (isFailed && (services == null || services.isEmpty())) {
                // Error state with retry
                LinearLayout errBox = new LinearLayout(this);
                errBox.setOrientation(LinearLayout.VERTICAL);
                errBox.setPadding(16, 20, 16, 20);
                errBox.setGravity(Gravity.CENTER);

                TextView tvErr = new TextView(this);
                tvErr.setText("Unable to load services for " + catName + ".");
                tvErr.setTextColor(Color.parseColor("#EF4444"));
                tvErr.setTextSize(13f);
                tvErr.setGravity(Gravity.CENTER);
                errBox.addView(tvErr);

                TextView tvRetry = new TextView(this);
                tvRetry.setText("Tap to retry");
                tvRetry.setTextColor(Color.parseColor("#2563EB"));
                tvRetry.setTextSize(13f);
                tvRetry.setTypeface(null, Typeface.BOLD);
                tvRetry.setPadding(12, 10, 12, 10);
                tvRetry.setGravity(Gravity.CENTER);
                final String finalCatId = catId;
                tvRetry.setOnClickListener(v -> {
                    failedCategories.remove(catName);
                    if (!TextUtils.isEmpty(finalCatId)) {
                        failedCategories.remove(finalCatId);
                    }
                    loadingCategories.add(catName);
                    renderServicesForSelectedCategories(false);
                    fetchCategoryServicesFromApi(finalCatId, catName);
                });
                errBox.addView(tvRetry);

                llServiceItemsContainer.addView(errBox);
            } else if (services != null && !services.isEmpty()) {
                // Success state: display database services
                appendServiceRowsForCategory(catName, services);
            } else if (services != null && services.isEmpty()) {
                // Empty state: database returned 0 records
                TextView tvEmpty = new TextView(this);
                tvEmpty.setText("No services available for " + catName);
                tvEmpty.setTextColor(Color.parseColor("#64748B"));
                tvEmpty.setTextSize(13f);
                tvEmpty.setPadding(16, 20, 16, 20);
                tvEmpty.setGravity(Gravity.CENTER);
                llServiceItemsContainer.addView(tvEmpty);
            } else {
                // Loading state: request in progress
                TextView tvLoading = new TextView(this);
                tvLoading.setText("Loading services for " + catName + "...");
                tvLoading.setTextColor(Color.parseColor("#64748B"));
                tvLoading.setTextSize(13f);
                tvLoading.setPadding(8, 12, 8, 12);
                llServiceItemsContainer.addView(tvLoading);

                if (!isLoading) {
                    loadingCategories.add(catName);
                    fetchCategoryServicesFromApi(catId, catName);
                }
            }
        }

        syncSelectedServices();
        updateSelectedServicesCount();

        if (smoothScroll && llServiceSection != null) {
            llServiceSection.post(() -> {
                View pageView = getCurrentPageView();
                ScrollView sv = null;
                if (pageView instanceof ScrollView) {
                    sv = (ScrollView) pageView;
                } else if (pageView != null) {
                    sv = pageView.findViewById(R.id.svExperienced);
                }
                if (sv != null) {
                    sv.smoothScrollTo(0, Math.max(0, llServiceSection.getTop() - 30));
                }
            });
        }
    }

    private void appendServiceRowsForCategory(String categoryName, List<String> services) {
        if (llServiceItemsContainer == null || services == null) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        Set<String> currentSelected = getSelectedServicesForCategory(categoryName);

        for (String service : services) {
            View row = inflater.inflate(R.layout.item_service_option, llServiceItemsContainer, false);
            ImageView ivCheck = row.findViewById(R.id.ivServiceCheck);
            TextView tvName = row.findViewById(R.id.tvServiceName);

            tvName.setText(service);
            updateServiceRowUi(row, ivCheck, tvName, currentSelected.contains(service));

            row.setOnClickListener(v -> {
                if (currentSelected.contains(service)) {
                    currentSelected.remove(service);
                    updateServiceRowUi(row, ivCheck, tvName, false);
                } else {
                    currentSelected.add(service);
                    updateServiceRowUi(row, ivCheck, tvName, true);
                }
                syncSelectedServices();
                updateSelectedServicesCount();
                validateCurrentPage();
            });

            llServiceItemsContainer.addView(row);
        }
    }

    private boolean isAyurvedaCategory(String id, String name) {
        if ("2".equals(id))
            return true;
        if (name == null)
            return false;
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.contains("ayurved");
    }

    private boolean isMassageCategory(String id, String name) {
        if (isAyurvedaCategory(id, name))
            return false; // Never treat Ayurveda as generic Massage
        if ("1".equals(id) || "9".equals(id))
            return true;
        if (name == null)
            return false;
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.contains("massage");
    }

    private void cacheMassageServices(List<String> services) {
        List<String> safeList = (services != null) ? services : Collections.emptyList();
        dynamicCategoryServicesCache.put("Massage Therapist", safeList);
        dynamicCategoryServicesCache.put("Massage at home", safeList);
        dynamicCategoryServicesCache.put("Massage at hotel", safeList);
        dynamicCategoryServicesCache.put("1", safeList);
        dynamicCategoryServicesCache.put("9", safeList);
    }

    private void cacheAyurvedaServices(List<String> services) {
        List<String> safeList = (services != null) ? services : Collections.emptyList();
        dynamicCategoryServicesCache.put("Ayurveda Therapist", safeList);
        dynamicCategoryServicesCache.put("Ayurveda at home", safeList);
        dynamicCategoryServicesCache.put("Ayurveda Massage Therapist", safeList);
        dynamicCategoryServicesCache.put("2", safeList);
    }

    private void fetchHomeMassageServicesFromApi() {
        loadingCategories.add("Massage Therapist");
        loadingCategories.add("Massage at home");
        loadingCategories.add("1");
        loadingCategories.add("9");
        Log.d(TAG, "Fetching dynamic home_massage services from URL: " + Constant.getHomeMassage);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                Constant.getHomeMassage,
                response -> {
                    loadingCategories.remove("Massage Therapist");
                    loadingCategories.remove("Massage at home");
                    loadingCategories.remove("1");
                    loadingCategories.remove("9");
                    failedCategories.remove("Massage Therapist");
                    failedCategories.remove("Massage at home");
                    failedCategories.remove("1");
                    failedCategories.remove("9");

                    List<String> services = parseServicesResponse(response);
                    Log.d(TAG, "Dynamic home_massage API returned " + (services != null ? services.size() : 0) + " services.");
                    cacheMassageServices(services);

                    boolean massageSelected = false;
                    for (String sc : selectedCategories) {
                        if (isMassageCategory("", sc)) {
                            massageSelected = true;
                            break;
                        }
                    }
                    if (massageSelected) {
                        renderServicesForSelectedCategories(false);
                        validateCurrentPage();
                    }
                },
                error -> {
                    loadingCategories.remove("Massage Therapist");
                    loadingCategories.remove("Massage at home");
                    loadingCategories.remove("1");
                    loadingCategories.remove("9");
                    failedCategories.add("Massage Therapist");
                    failedCategories.add("Massage at home");
                    failedCategories.add("1");
                    failedCategories.add("9");

                    Log.e(TAG, "Failed to load dynamic home_massage services from " + Constant.getHomeMassage
                            + " (" + error.getMessage() + ")", error);

                    boolean massageSelected = false;
                    for (String sc : selectedCategories) {
                        if (isMassageCategory("", sc)) {
                            massageSelected = true;
                            break;
                        }
                    }
                    if (massageSelected) {
                        renderServicesForSelectedCategories(false);
                        validateCurrentPage();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        getAppRequestQueue().add(request.setTag(TAG_PREFETCH));
    }

    private void fetchAyurvedaServicesFromApi() {
        loadingCategories.add("Ayurveda Therapist");
        loadingCategories.add("Ayurveda at home");
        loadingCategories.add("2");
        Log.d(TAG, "Fetching dynamic ayurved_massages services from URL: " + Constant.getAyurvedaServices);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                Constant.getAyurvedaServices,
                response -> {
                    loadingCategories.remove("Ayurveda Therapist");
                    loadingCategories.remove("Ayurveda at home");
                    loadingCategories.remove("2");
                    failedCategories.remove("Ayurveda Therapist");
                    failedCategories.remove("Ayurveda at home");
                    failedCategories.remove("2");

                    List<String> services = parseServicesResponse(response);
                    Log.d(TAG, "Dynamic ayurved_massages API returned " + (services != null ? services.size() : 0) + " services.");
                    cacheAyurvedaServices(services);

                    boolean ayurvedaSelected = false;
                    for (String sc : selectedCategories) {
                        if (isAyurvedaCategory("", sc)) {
                            ayurvedaSelected = true;
                            break;
                        }
                    }
                    if (ayurvedaSelected) {
                        renderServicesForSelectedCategories(false);
                        validateCurrentPage();
                    }
                },
                error -> {
                    loadingCategories.remove("Ayurveda Therapist");
                    loadingCategories.remove("Ayurveda at home");
                    loadingCategories.remove("2");
                    failedCategories.add("Ayurveda Therapist");
                    failedCategories.add("Ayurveda at home");
                    failedCategories.add("2");

                    Log.e(TAG, "Failed to load dynamic ayurved_massages services from " + Constant.getAyurvedaServices
                            + " (" + error.getMessage() + ")", error);

                    boolean ayurvedaSelected = false;
                    for (String sc : selectedCategories) {
                        if (isAyurvedaCategory("", sc)) {
                            ayurvedaSelected = true;
                            break;
                        }
                    }
                    if (ayurvedaSelected) {
                        renderServicesForSelectedCategories(false);
                        validateCurrentPage();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        getAppRequestQueue().add(request.setTag(TAG_PREFETCH));
    }

    private List<String> getFallbackServicesForCategory(String categoryName) {
        if (TextUtils.isEmpty(categoryName)) {
            return Collections.emptyList();
        }
        // STRICT: Under no circumstances return static services for Massage or Ayurveda!
        if (isMassageCategory("", categoryName) || isAyurvedaCategory("", categoryName)) {
            return Collections.emptyList();
        }
        List<String> list = CATEGORY_SERVICES.get(categoryName);
        if (list != null && !list.isEmpty()) {
            return new ArrayList<>(list);
        }
        for (Map.Entry<String, List<String>> entry : CATEGORY_SERVICES.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(categoryName)
                    || categoryName.toLowerCase(Locale.ROOT).contains(entry.getKey().toLowerCase(Locale.ROOT))
                    || entry.getKey().toLowerCase(Locale.ROOT).contains(categoryName.toLowerCase(Locale.ROOT))) {
                return new ArrayList<>(entry.getValue());
            }
        }
        return Arrays.asList(
                "Standard Consultation",
                "One-on-One Session",
                "Comprehensive Care Package",
                "Follow-up & Evaluation");
    }

    private void fetchCategoryServicesFromApi(String categoryId, String categoryName) {
        final boolean isAyurveda = isAyurvedaCategory(categoryId, categoryName);
        final boolean isMassage = isMassageCategory(categoryId, categoryName);

        final String apiUrl = isMassage ? Constant.getHomeMassage
                : (isAyurveda ? Constant.getAyurvedaServices : Constant.getCategoryServices);
        final int httpMethod = (isMassage || isAyurveda) ? Request.Method.GET : Request.Method.POST;

        loadingCategories.add(categoryName);
        if (!TextUtils.isEmpty(categoryId)) {
            loadingCategories.add(categoryId);
        }
        Log.d(TAG, "Fetching services for category: name='" + categoryName + "', id='" + categoryId + "' from URL: " + apiUrl);

        StringRequest request = new StringRequest(
                httpMethod,
                apiUrl,
                response -> {
                    loadingCategories.remove(categoryName);
                    if (!TextUtils.isEmpty(categoryId)) {
                        loadingCategories.remove(categoryId);
                    }
                    failedCategories.remove(categoryName);
                    if (!TextUtils.isEmpty(categoryId)) {
                        failedCategories.remove(categoryId);
                    }

                    List<String> services = parseServicesResponse(response);
                    Log.d(TAG, "Services response for " + categoryName + " (" + categoryId + "): "
                            + (services != null ? services.size() : 0) + " items parsed.");

                    if (services == null) {
                        services = Collections.emptyList();
                    }

                    // STRICT: Only use static fallback for other categories, NEVER for Massage or Ayurveda!
                    if (services.isEmpty() && !isMassage && !isAyurveda) {
                        services = getFallbackServicesForCategory(categoryName);
                    }

                    if (isMassage) {
                        cacheMassageServices(services);
                    } else if (isAyurveda) {
                        cacheAyurvedaServices(services);
                    } else {
                        if (!TextUtils.isEmpty(categoryId)) {
                            dynamicCategoryServicesCache.put(categoryId, services);
                        }
                        dynamicCategoryServicesCache.put(categoryName, services);
                    }

                    if (selectedCategories.contains(categoryName)) {
                        renderServicesForSelectedCategories(false);
                        validateCurrentPage();
                    }
                },
                error -> {
                    loadingCategories.remove(categoryName);
                    if (!TextUtils.isEmpty(categoryId)) {
                        loadingCategories.remove(categoryId);
                    }
                    Log.e(TAG, "Failed to fetch services for " + categoryName + " from " + apiUrl + ": " + error.getMessage(), error);

                    if (isMassage || isAyurveda) {
                        failedCategories.add(categoryName);
                        if (!TextUtils.isEmpty(categoryId)) {
                            failedCategories.add(categoryId);
                        }
                    } else {
                        List<String> fallback = getFallbackServicesForCategory(categoryName);
                        if (!TextUtils.isEmpty(categoryId)) {
                            dynamicCategoryServicesCache.put(categoryId, fallback);
                        }
                        dynamicCategoryServicesCache.put(categoryName, fallback);
                    }

                    if (selectedCategories.contains(categoryName)) {
                        renderServicesForSelectedCategories(false);
                        validateCurrentPage();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("category_id", categoryId != null ? categoryId : "");
                params.put("category_name", categoryName != null ? categoryName : "");
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        getAppRequestQueue().add(request.setTag(TAG_PREFETCH));
    }

    private List<String> parseServicesResponse(String response) {
        List<String> result = new ArrayList<>();
        if (TextUtils.isEmpty(response)) {
            Log.w(TAG, "parseServicesResponse: empty or null response string received");
            return result;
        }

        try {
            String trimmed = response.trim();
            JSONArray array = null;

            if (trimmed.startsWith("[")) {
                array = new JSONArray(trimmed);
            } else if (trimmed.startsWith("{")) {
                JSONObject obj = new JSONObject(trimmed);
                if (obj.has("services")) {
                    array = obj.optJSONArray("services");
                } else if (obj.has("home_massage")) {
                    array = obj.optJSONArray("home_massage");
                } else if (obj.has("ayurved_massages")) {
                    array = obj.optJSONArray("ayurved_massages");
                } else if (obj.has("ayurveda_services")) {
                    array = obj.optJSONArray("ayurveda_services");
                } else if (obj.has("data")) {
                    array = obj.optJSONArray("data");
                } else if (obj.has("service_items")) {
                    array = obj.optJSONArray("service_items");
                } else if (obj.has("items")) {
                    array = obj.optJSONArray("items");
                }
            }

            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (item instanceof String) {
                        String s = ((String) item).trim();
                        if (!s.isEmpty() && !result.contains(s)) {
                            result.add(s);
                        }
                    } else if (item instanceof JSONObject) {
                        JSONObject sObj = (JSONObject) item;
                        String name = "";
                        if (sObj.has("name")) {
                            name = sObj.optString("name").trim();
                        } else if (sObj.has("service_name")) {
                            name = sObj.optString("service_name").trim();
                        } else if (sObj.has("title")) {
                            name = sObj.optString("title").trim();
                        }
                        if (!TextUtils.isEmpty(name) && !result.contains(name)) {
                            result.add(name);
                        }
                    }
                }
                Log.d(TAG, "parseServicesResponse: extracted " + result.size() + " items from array length " + array.length());
            } else {
                Log.w(TAG, "parseServicesResponse: no recognized services array found in response: "
                        + (trimmed.length() > 200 ? trimmed.substring(0, 200) + "..." : trimmed));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing services response: " + e.getMessage(), e);
        }

        return result;
    }

    private String getServiceHeaderForCategory(String category) {
        if (category == null)
            return "SERVICES & SESSIONS";
        String upper = category.trim().toUpperCase(Locale.ROOT);
        if (upper.contains("TRAINER") || upper.contains("YOGA") || upper.contains("DANCE") || upper.contains("ZUMBA")) {
            return upper + " SESSIONS";
        }
        return upper + " TREATMENTS";
    }

    private void buildServiceRows(String categoryName) {
        renderServicesForSelectedCategories(false);
    }

    private void buildServiceRows(String categoryName, List<String> services) {
        appendServiceRowsForCategory(categoryName, services);
    }

    private void updateServiceRowUi(View row, ImageView ivCheck, TextView tvName, boolean isSelected) {
        if (isSelected) {
            row.setBackgroundResource(R.drawable.bg_service_item_selected);
            ivCheck.setImageResource(R.drawable.ic_service_checked);
            tvName.setTextColor(Color.WHITE);
            tvName.setTypeface(null, Typeface.BOLD);
        } else {
            row.setBackgroundResource(R.drawable.bg_service_item_unselected);
            ivCheck.setImageResource(R.drawable.ic_service_unchecked);
            tvName.setTextColor(Color.parseColor("#1E293B"));
            tvName.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void updateSelectedServicesCount() {
        if (tvSelectedServicesCount == null) {
            return;
        }
        int count = selectedServices.size();
        tvSelectedServicesCount.setText(count + (count == 1 ? " service selected" : " services selected"));
    }

    // ============================================================
    // PAGE 2 - EXPERIENCE (SELECT / DROPDOWN OPTIONS)
    // ============================================================

    private void setupExperienceDropdown() {
        if (spExperience == null) {
            return;
        }

        if (experienceOptions.isEmpty()) {
            experienceOptions.add(new ExperienceOption("Select years of experience", ""));
            experienceOptions.add(new ExperienceOption("Freshers", "0"));
            for (int i = 1; i <= 20; i++) {
                String label = i + (i == 1 ? " Year" : " Years");
                experienceOptions.add(new ExperienceOption(label, String.valueOf(i)));
            }
        }

        ArrayAdapter<ExperienceOption> adapter = new ArrayAdapter<ExperienceOption>(
                this,
                R.layout.item_spinner_experience,
                experienceOptions) {

            @Override
            public boolean isEnabled(int position) {
                // Position 0 is the placeholder prompt and cannot be selected as valid experience
                return position != 0;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                if (position == 0) {
                    tv.setTextColor(Color.parseColor("#94A3B8"));
                    tv.setTypeface(null, Typeface.NORMAL);
                } else {
                    tv.setTextColor(Color.parseColor("#14213D"));
                    tv.setTypeface(null, Typeface.BOLD);
                }
                return tv;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                if (position == 0) {
                    tv.setTextColor(Color.parseColor("#94A3B8"));
                    tv.setTypeface(null, Typeface.NORMAL);
                } else {
                    tv.setTextColor(Color.parseColor("#14213D"));
                    tv.setTypeface(null, Typeface.NORMAL);
                }
                return tv;
            }
        };

        adapter.setDropDownViewResource(R.layout.item_spinner_experience_dropdown);
        spExperience.setAdapter(adapter);

        // Pre-select if selectedExperience was previously chosen or restored
        int selectedIndex = 0;
        if (!TextUtils.isEmpty(selectedExperience)) {
            for (int i = 0; i < experienceOptions.size(); i++) {
                ExperienceOption opt = experienceOptions.get(i);
                if (opt.getValue().equals(selectedExperience)
                        || opt.getLabel().equalsIgnoreCase(selectedExperience)
                        || (selectedExperience.equals("0") && opt.getLabel().equalsIgnoreCase("Freshers"))
                        || (selectedExperience.equalsIgnoreCase("Fresher") && opt.getValue().equals("0"))) {
                    selectedIndex = i;
                    break;
                }
            }
        }
        spExperience.setSelection(selectedIndex, false);

        spExperience.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position < experienceOptions.size()) {
                    selectedExperience = experienceOptions.get(position).getValue();
                } else {
                    selectedExperience = "";
                }
                validateCurrentPage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedExperience = "";
                validateCurrentPage();
            }
        });

        if (flExperienceDropdown != null) {
            flExperienceDropdown.setOnClickListener(v -> spExperience.performClick());
        }
    }

    // ============================================================
    // TEXT WATCHERS
    // ============================================================

    private void safeAddWatcher(EditText editText) {
        if (editText == null)
            return;
        editText.removeTextChangedListener(pageFieldWatcher);
        editText.addTextChangedListener(pageFieldWatcher);
    }

    private void setupTextWatchers(int position) {
        switch (position) {
            case 0:
                safeAddWatcher(regName);
                safeAddWatcher(regEmail);
                safeAddWatcher(regAddress);
                safeAddWatcher(regCity);
                safeAddWatcher(regMobile);
                break;

            case 1:
                safeAddWatcher(etExpDesc);
                break;

            case 2:
                // Checked via AreaAdapter callbacks
                break;

            case 3:
                // Documents page
                break;

            case 4:
                safeAddWatcher(etReferral);
                break;
        }
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private boolean validateCurrentPage() {
        boolean valid = false;

        switch (currentPage) {
            case 0:
                valid = validatePersonal();
                break;
            case 1:
                valid = validateProfessional();
                break;
            case 2:
                valid = validatePreferences();
                break;
            case 3:
                valid = validateDocuments();
                break;
            case 4:
                valid = validateReferral();
                break;
        }

        updateNextButtonState(valid);
        return valid;
    }

    private boolean validatePersonal() {
        capturePage1FormState();

        if (TextUtils.isEmpty(formName) && (regName == null || TextUtils.isEmpty(regName.getText())))
            return false;

        String aadhaar = regAadhaar != null && regAadhaar.getText() != null
                ? regAadhaar.getText().toString().trim()
                : formAadhaar;
        String cleanAadhaar = aadhaar.replaceAll("\\s+", "");
        if (cleanAadhaar.length() != 12 || !TextUtils.isDigitsOnly(cleanAadhaar))
            return false;

        if (ccp == null || !ccp.isValidFullNumber()) {
            if (TextUtils.isEmpty(formMobile) || formMobile.length() < 10) {
                return false;
            }
        }

        String email = regEmail != null && regEmail.getText() != null
                ? regEmail.getText().toString().trim()
                : formEmail;

        if (!isValidEmail(email))
            return false;

        boolean genderSelected = false;
        if (rgGender != null && rgGender.getCheckedRadioButtonId() != -1) {
            genderSelected = true;
        } else if (!TextUtils.isEmpty(selectedGenderString)) {
            genderSelected = true;
        }
        if (!genderSelected)
            return false;

        if (selectedDob == null)
            return false;

        if (tvAge != null && tvAge.getText().toString().equals(getString(R.string.age_warning))) {
            return false;
        }

        if (TextUtils.isEmpty(selectedLivingCityName)) {
            return false;
        }
        if (regCity != null && regCity.getText() != null) {
            String currentCityText = regCity.getText().toString().trim();
            if (TextUtils.isEmpty(currentCityText) || !currentCityText.equalsIgnoreCase(selectedLivingCityName)) {
                return false;
            }
        } else if (TextUtils.isEmpty(formCityName)) {
            return false;
        }
        if ((regAddress == null || TextUtils.isEmpty(regAddress.getText())) && TextUtils.isEmpty(formAddress))
            return false;
        if (encodedPhoto == null && TextUtils.isEmpty(existingProfilePhotoUrl))
            return false;

        return true;
    }

    private boolean validateProfessional() {
        return (!selectedCategories.isEmpty() || !TextUtils.isEmpty(selectedCategory))
                && !selectedServices.isEmpty()
                && !TextUtils.isEmpty(selectedExperience);
    }

    private boolean validatePreferences() {
        return !TextUtils.isEmpty(selectedCityIdP3)
                && areasLoaded
                && selectedAreaIds != null
                && selectedAreaIds.size() >= getRequiredAreasCount();
    }

    private boolean validateReferral() {
        return true;
    }

    private boolean validateDocuments() {
        // Except Aadhaar (Front and Back), all other documents are optional
        return !TextUtils.isEmpty(aadhaarFrontDocBase64)
                && !TextUtils.isEmpty(aadhaarBackDocBase64);
    }

    private String getDocumentValidationErrorMessage() {
        if (TextUtils.isEmpty(aadhaarFrontDocBase64) || TextUtils.isEmpty(aadhaarBackDocBase64)) {
            return "Please upload Aadhaar Front and Back";
        }
        return "Please upload required documents";
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email))
            return false;
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return pattern.matcher(email).matches();
    }

    // ============================================================
    // NAVIGATION UI
    // ============================================================

    private void updateNextButtonState(boolean valid) {
        if (btnNext == null)
            return;

        if (valid) {
            btnNext.setEnabled(true);
            btnNext.setBackgroundColor(Color.parseColor("#1F5FE0"));
            btnNext.setTextColor(Color.WHITE);
        } else {
            btnNext.setEnabled(false);
            btnNext.setBackgroundColor(Color.parseColor("#BDBDBD"));
            btnNext.setTextColor(Color.parseColor("#757575"));
        }
    }

    private void updateUI() {
        tvStep.setText((currentPage + 1) + " / " + TOTAL_PAGES);
        progressBar.setProgress((currentPage + 1) * 100 / TOTAL_PAGES);

        btnPrevious.setVisibility(View.VISIBLE);

        if (currentPage == TOTAL_PAGES - 1) {
            btnNext.setText("SUBMIT");
        } else {
            btnNext.setText("CONTINUE");
        }

        btnPrevious.setText(currentPage == 0 ? "BACK" : "PREVIOUS");
    }

    private void setupButtons() {
        btnNext.setOnClickListener(v -> {
            hideKeyboard();
            if (isSubmitting) {
                return;
            }

            if (!validateCurrentPage()) {
                String message;
                if (currentPage == 2 && TextUtils.isEmpty(selectedCityIdP3)) {
                    message = "Please select your primary city";
                } else if (currentPage == 2 && !areasLoaded) {
                    message = "Please wait for areas to load";
                } else if (currentPage == 2) {
                    int req = getRequiredAreasCount();
                    message = "Please select at least " + req + " location hubs (Currently selected: "
                            + selectedAreaIds.size() + ")";
                } else if (currentPage == 3) {
                    message = getDocumentValidationErrorMessage();
                } else {
                    message = "Please fill all required fields";
                }

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                return;
            }

            switch (currentPage) {
                case 0:
                    submitPage1Personal();
                    break;
                case 1:
                    submitPage2Professional();
                    break;
                case 2:
                    submitPage3Preferences();
                    break;
                case 3:
                    submitPage4Documents();
                    break;
                case 4:
                    submitPage5ReferralFinal();
                    break;
            }
        });

        btnPrevious.setOnClickListener(v -> {
            hideKeyboard();
            if (currentPage > 0) {
                viewPager.setCurrentItem(currentPage - 1, true);
            } else {
                onBackPressed();
            }
        });
    }

    // ============================================================
    // STEP SUBMISSION HELPERS
    // ============================================================

    private void showStepErrorDialog(String title, String message, int statusCode) {
        if (isFinishing() || isDestroyed())
            return;
        String displayMsg = !TextUtils.isEmpty(message) ? message : "Unable to process request";
        if (statusCode > 0) {
            displayMsg = displayMsg + "\n\n(HTTP Status: " + statusCode + ")";
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(displayMsg)
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean checkPayloadSizeOrAlert(Map<String, String> params, String stepName) {
        int totalPayloadBytes = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey() != null)
                totalPayloadBytes += entry.getKey().getBytes(StandardCharsets.UTF_8).length;
            if (entry.getValue() != null)
                totalPayloadBytes += entry.getValue().getBytes(StandardCharsets.UTF_8).length;
            totalPayloadBytes += 2; // '=' and '&'
        }
        final int MAX_SAFE_PAYLOAD = 950 * 1024; // 950 KB (server limit is 1,024 KB)
        if (totalPayloadBytes > MAX_SAFE_PAYLOAD) {
            final int sizeKb = totalPayloadBytes / 1024;
            new AlertDialog.Builder(this)
                    .setTitle("Upload Size Exceeds Limit")
                    .setMessage(stepName + " payload size is " + sizeKb
                            + " KB, which exceeds the server limit of 950 KB.\n\n" +
                            "Please compress your files or select camera photos instead.")
                    .setPositiveButton("OK", null)
                    .show();
            return false;
        }
        return true;
    }

    private Runnable createSafeDismiss(ProgressDialog dialog) {
        return () -> {
            isSubmitting = false;
            if (btnNext != null) {
                btnNext.setEnabled(true);
                btnNext.setAlpha(1.0f);
            }
            if (!isFinishing() && !isDestroyed() && dialog != null && dialog.isShowing()) {
                try {
                    dialog.dismiss();
                } catch (Exception ignored) {
                }
            }
        };
    }

    // ============================================================
    // STEP 1: PERSONAL DETAILS (INSERT)
    // ============================================================

    private void submitPage1Personal() {
        if (isSubmitting)
            return;

        capturePage1FormState();

        final String name = regName != null && regName.getText() != null && regName.getText().length() > 0
                ? regName.getText().toString().trim()
                : formName;

        final String rawAadhaar = regAadhaar != null && regAadhaar.getText() != null && regAadhaar.getText().length() > 0
                ? regAadhaar.getText().toString().trim()
                : formAadhaar;
        final String cleanAadhaar = rawAadhaar.replaceAll("\\s+", "").trim();

        final String mobile = (ccp != null && ccp.isValidFullNumber())
                ? ccp.getFullNumberWithPlus()
                : (!TextUtils.isEmpty(formMobile) ? formMobile : (regMobile != null && regMobile.getText() != null ? regMobile.getText().toString().trim() : ""));

        final String email = regEmail != null && regEmail.getText() != null && regEmail.getText().length() > 0
                ? regEmail.getText().toString().trim()
                : formEmail;
        final String dob = selectedDob != null ? selectedDob : "";
        final String address = regAddress != null && regAddress.getText() != null && regAddress.getText().length() > 0
                ? regAddress.getText().toString().trim()
                : formAddress;
        final String livingCity = !TextUtils.isEmpty(selectedLivingCityName)
                ? selectedLivingCityName
                : (regCity != null && regCity.getText() != null && regCity.getText().length() > 0
                        ? regCity.getText().toString().trim()
                        : formCityName);
        final String photo = encodedPhoto != null ? encodedPhoto : "";

        String gender = "";
        if (rgGender != null && rgGender.getCheckedRadioButtonId() != -1) {
            RadioButton selectedGender = rgGender.findViewById(rgGender.getCheckedRadioButtonId());
            if (selectedGender != null) {
                gender = selectedGender.getText().toString().trim();
            }
        }
        if (TextUtils.isEmpty(gender) && !TextUtils.isEmpty(selectedGenderString)) {
            gender = selectedGenderString;
        }
        final String finalGender = gender;

        final Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("aadhaar", cleanAadhaar);
        params.put("aadhar_card_no", cleanAadhaar);
        params.put("mobile", mobile);
        params.put("phone", mobile);
        params.put("email", email);
        params.put("dob", dob);
        params.put("date_of_birth", dob);
        params.put("gender", finalGender);
        params.put("living_city_name", livingCity);
        params.put("address", address);
        params.put("photo", photo);
        params.put("profile_photo", photo);
        if (!TextUtils.isEmpty(registeredExpertId)) {
            params.put("id", registeredExpertId);
            params.put("expert_id", registeredExpertId);
        }

        if (!checkPayloadSizeOrAlert(params, "Personal Details")) {
            return;
        }

        isSubmitting = true;
        if (btnNext != null) {
            btnNext.setEnabled(false);
            btnNext.setAlpha(0.6f);
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving personal details...");
        progressDialog.setCancelable(false);
        if (!isFinishing() && !isDestroyed()) {
            progressDialog.show();
        }

        Runnable safeDismiss = createSafeDismiss(progressDialog);

        final String requestUrl = Constant.RegisterStep1Url;
        Log.d(TAG, "Step 1 Request URL: " + requestUrl + " | Params keys: " + params.keySet());

        StringRequest request = new StringRequest(
                Request.Method.POST,
                requestUrl,
                response -> {
                    safeDismiss.run();
                    Log.d(TAG, "Step 1 Response from " + requestUrl + ": " + response);

                    if (response == null || !response.trim().startsWith("{")) {
                        showStepErrorDialog("Server Error",
                                "Server returned invalid response (non-JSON):\n"
                                        + (response != null ? response : "Empty response"),
                                500);
                        return;
                    }

                    JSONObject json;
                    try {
                        json = new JSONObject(response);
                    } catch (Exception e) {
                        showStepErrorDialog("Response Error", "Could not parse server response as JSON:\n" + response,
                                200);
                        return;
                    }

                    // Check for already fully registered account
                    if (json.optBoolean("already_registered", false)) {
                        String msg = json.optString("message", "An account with this mobile number or Aadhaar already exists. Please log in.");
                        showAlreadyRegisteredDialog(msg);
                        return;
                    }

                    // Check for conflict (e.g. mobile/aadhaar mismatch)
                    if (json.optBoolean("conflict", false)) {
                        String msg = json.optString("message", "The provided credentials conflict with an existing registration.");
                        showStepErrorDialog("Account Conflict", msg, 409);
                        return;
                    }

                    if (!json.optBoolean("status", false)) {
                        String errMsg = json.optString("message", "Personal details registration failed.");
                        showStepErrorDialog("Registration Error", errMsg, 400);
                        return;
                    }

                    // Extract registered expert id: "id" -> "expert_id" -> "inserted_id" ->
                    // "data.id"
                    String extractedId = json.optString("id", "");
                    if (TextUtils.isEmpty(extractedId)) {
                        extractedId = json.optString("expert_id", "");
                    }
                    if (TextUtils.isEmpty(extractedId)) {
                        extractedId = json.optString("inserted_id", "");
                    }
                    if (TextUtils.isEmpty(extractedId) && json.has("data") && json.optJSONObject("data") != null) {
                        extractedId = json.optJSONObject("data").optString("id", "");
                    }

                    if (TextUtils.isEmpty(extractedId)) {
                        showStepErrorDialog("Step 1 Failed",
                                "Could not obtain expert ID from server response:\n" + response, 200);
                        return;
                    }

                    // 3 & 4: Save expert id to SharedPreferences and in-memory field
                    saveExpertIdToPrefs(extractedId);

                    boolean isRecovered = json.optBoolean("recovered", false);
                    if (isRecovered) {
                        Toast.makeText(Register.this, "Incomplete registration recovered", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Register.this, "Personal details saved successfully", Toast.LENGTH_SHORT).show();
                    }

                    // Preload Page-3 city list as soon as user leaves Page 1
                    if (!cityLoadedP3 && !cityLoadingP3) {
                        loadCities(1);
                    }

                    // Advance to Page 2 (2nd layout: Professional Details / Experience / Categories)
                    viewPager.setCurrentItem(1, true);
                },
                error -> {
                    safeDismiss.run();
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                    String serverMsg = "";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            serverMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8).trim();
                        } catch (Exception ignored) {
                        }
                    }
                    if (TextUtils.isEmpty(serverMsg)) {
                        serverMsg = (error instanceof TimeoutError) ? "Network timeout. Please check your connection."
                                : "Unable to save personal details.";
                    }
                    Log.e(TAG, "Step 1 request failed (HTTP " + statusCode + "): " + serverMsg, error);
                    showStepErrorDialog("Step 1 Failed", serverMsg, statusCode);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 1.0f));
        getAppRequestQueue().add(request.setTag(TAG_REGISTRATION));
    }

    private void showAlreadyRegisteredDialog(String message) {
        if (isFinishing() || isDestroyed())
            return;
        new AlertDialog.Builder(this, R.style.WhiteAlertDialog)
                .setTitle("Account Already Registered")
                .setMessage(message)
                .setPositiveButton("Log In", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(Register.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    // ============================================================
    // STEP 2: PROFESSIONAL DETAILS (UPDATE)
    // ============================================================

    private void submitPage2Professional() {
        if (isSubmitting)
            return;

        if (TextUtils.isEmpty(registeredExpertId)) {
            showStepErrorDialog("Step 1 Required", "Please complete Personal Details (Step 1) before proceeding.", 0);
            viewPager.setCurrentItem(0, true);
            return;
        }

        syncSelectedServices();

        List<String> resolvedIds = new ArrayList<>();
        if (!selectedCategoryIds.isEmpty()) {
            resolvedIds.addAll(selectedCategoryIds);
        } else if (!selectedCategories.isEmpty()) {
            for (String catName : selectedCategories) {
                for (CategoryItem item : dynamicCategories) {
                    if (item.name.equalsIgnoreCase(catName)) {
                        resolvedIds.add(item.id);
                        break;
                    }
                }
            }
        }
        final String finalCategoryId = !resolvedIds.isEmpty() ? TextUtils.join(",", resolvedIds) : selectedCategoryId;
        final String finalCategory = !selectedCategories.isEmpty() ? TextUtils.join(", ", selectedCategories)
                : selectedCategory;

        final String expDesc = etExpDesc != null && etExpDesc.getText() != null
                ? etExpDesc.getText().toString().trim()
                : "";
        String fullExpDesc = expDesc;
        if (!selectedServices.isEmpty()) {
            String servicesLine = "Services: " + TextUtils.join(", ", selectedServices);
            fullExpDesc = TextUtils.isEmpty(expDesc) ? servicesLine : expDesc + "\n" + servicesLine;
        }

        final Map<String, String> params = new HashMap<>();
        // TODO: Backend confirmation: Confirm whether primary key parameter is named
        // "id" or "expert_id" for UPDATE.
        params.put("id", registeredExpertId);
        // Categories stored as IDs only (e.g. "1,2" or "1"), not text, like area_ids
        params.put("categories", finalCategoryId);
        params.put("category_id", finalCategoryId);
        params.put("expert_category", finalCategoryId);
        params.put("selected_services", TextUtils.join(", ", selectedServices));
        params.put("experience", selectedExperience);
        params.put("experience_desc", fullExpDesc);

        if (!checkPayloadSizeOrAlert(params, "Page 2")) {
            return;
        }

        isSubmitting = true;
        if (btnNext != null) {
            btnNext.setEnabled(false);
            btnNext.setAlpha(0.6f);
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving professional details...");
        progressDialog.setCancelable(false);
        if (!isFinishing() && !isDestroyed()) {
            progressDialog.show();
        }

        Runnable safeDismiss = createSafeDismiss(progressDialog);

        // TODO: Backend confirmation: Confirm the exact endpoint URL for Page 2 update
        // (e.g. Constant.RegisterStep2Url).
        final String requestUrl = Constant.RegisterStep2Url;
        Log.d(TAG, "Step 2 Request URL: " + requestUrl + " | Params keys: " + params.keySet());

        StringRequest request = new StringRequest(
                Request.Method.POST,
                requestUrl,
                response -> {
                    safeDismiss.run();
                    Log.d(TAG, "Step 2 Response from " + requestUrl + ": " + response);

                    boolean success = response.contains("success") || response.contains("Success")
                            || response.contains("updated");
                    if (success) {
                        // Advance to Page 3 only on success
                        viewPager.setCurrentItem(2, true);
                    } else {
                        showStepErrorDialog("Step 2 Failed", "Failed to update professional details:\n" + response,
                                200);
                    }
                },
                error -> {
                    safeDismiss.run();
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                    String serverMsg = "";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            serverMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8).trim();
                        } catch (Exception ignored) {
                        }
                    }
                    if (TextUtils.isEmpty(serverMsg)) {
                        serverMsg = (error instanceof TimeoutError) ? "Network timeout. Please check your connection."
                                : "Unable to save professional details.";
                    }
                    Log.e(TAG, "Step 2 request failed (HTTP " + statusCode + "): " + serverMsg, error);
                    showStepErrorDialog("Step 2 Failed", serverMsg, statusCode);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 1.0f));
        getAppRequestQueue().add(request.setTag(TAG_REGISTRATION));
    }

    // ============================================================
    // STEP 3: LOCATION PREFERENCES (UPDATE)
    // ============================================================

    private void submitPage3Preferences() {
        if (isSubmitting)
            return;

        if (TextUtils.isEmpty(registeredExpertId)) {
            showStepErrorDialog("Step 1 Required", "Please complete Personal Details (Step 1) before proceeding.", 0);
            viewPager.setCurrentItem(0, true);
            return;
        }

        final String cityId = !TextUtils.isEmpty(selectedCityIdP3) ? selectedCityIdP3 : "";
        final String areaIds = TextUtils.join(",", selectedAreaIds);

        final Map<String, String> params = new HashMap<>();
        // TODO: Backend confirmation: Confirm whether primary key parameter is named
        // "id" or "expert_id" for UPDATE.
        params.put("id", registeredExpertId);
        params.put("city_id", cityId);
        params.put("area_ids", areaIds);

        if (!checkPayloadSizeOrAlert(params, "Page 3")) {
            return;
        }

        isSubmitting = true;
        if (btnNext != null) {
            btnNext.setEnabled(false);
            btnNext.setAlpha(0.6f);
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving location preferences...");
        progressDialog.setCancelable(false);
        if (!isFinishing() && !isDestroyed()) {
            progressDialog.show();
        }

        Runnable safeDismiss = createSafeDismiss(progressDialog);

        // TODO: Backend confirmation: Confirm the exact endpoint URL for Page 3 update
        // (e.g. Constant.RegisterStep3Url).
        final String requestUrl = Constant.RegisterStep3Url;
        Log.d(TAG, "Step 3 Request URL: " + requestUrl + " | Params keys: " + params.keySet());

        StringRequest request = new StringRequest(
                Request.Method.POST,
                requestUrl,
                response -> {
                    safeDismiss.run();
                    Log.d(TAG, "Step 3 Response from " + requestUrl + ": " + response);

                    boolean success = response.contains("success") || response.contains("Success")
                            || response.contains("updated");
                    if (success) {
                        // Advance to Page 4 only on success
                        viewPager.setCurrentItem(3, true);
                    } else {
                        showStepErrorDialog("Step 3 Failed", "Failed to update locations:\n" + response, 200);
                    }
                },
                error -> {
                    safeDismiss.run();
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                    String serverMsg = "";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            serverMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8).trim();
                        } catch (Exception ignored) {
                        }
                    }
                    if (TextUtils.isEmpty(serverMsg)) {
                        serverMsg = (error instanceof TimeoutError) ? "Network timeout. Please check your connection."
                                : "Unable to save location preferences.";
                    }
                    Log.e(TAG, "Step 3 request failed (HTTP " + statusCode + "): " + serverMsg, error);
                    showStepErrorDialog("Step 3 Failed", serverMsg, statusCode);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 1.0f));
        getAppRequestQueue().add(request.setTag(TAG_REGISTRATION));
    }

    // ============================================================
    // STEP 4: VERIFICATION DOCUMENTS (UPDATE)
    // ============================================================

    private void submitPage4Documents() {
        if (isSubmitting)
            return;

        if (TextUtils.isEmpty(registeredExpertId)) {
            showStepErrorDialog("Step 1 Required", "Please complete Personal Details (Step 1) before proceeding.", 0);
            viewPager.setCurrentItem(0, true);
            return;
        }

        final String resumeDoc = resumeDocBase64 != null ? resumeDocBase64 : "";
        final String cert10thDoc = cert10thDocBase64 != null ? cert10thDocBase64 : "";
        final String cert12thDoc = cert12thDocBase64 != null ? cert12thDocBase64 : "";
        final String certGradDoc = certGradDocBase64 != null ? certGradDocBase64 : "";
        final String aadhaarFrontDoc = aadhaarFrontDocBase64 != null ? aadhaarFrontDocBase64 : "";
        final String aadhaarBackDoc = aadhaarBackDocBase64 != null ? aadhaarBackDocBase64 : "";

        final Map<String, String> params = new HashMap<>();
        // TODO: Backend confirmation: Confirm whether primary key parameter is named
        // "id" or "expert_id" for UPDATE.
        params.put("id", registeredExpertId);
        params.put("resume_file", resumeDoc);
        params.put("madhyamik_certificate", cert10thDoc);
        params.put("hs_certificate", cert12thDoc);
        params.put("graduation_certificate", certGradDoc);
        params.put("aadhaar_front", aadhaarFrontDoc);
        params.put("aadhaar_back", aadhaarBackDoc);

        if (!checkPayloadSizeOrAlert(params, "Page 4 Documents")) {
            return;
        }

        isSubmitting = true;
        if (btnNext != null) {
            btnNext.setEnabled(false);
            btnNext.setAlpha(0.6f);
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading verification documents...");
        progressDialog.setCancelable(false);
        if (!isFinishing() && !isDestroyed()) {
            progressDialog.show();
        }

        Runnable safeDismiss = createSafeDismiss(progressDialog);

        // TODO: Backend confirmation: Confirm the exact endpoint URL for Page 4 update
        // (e.g. Constant.RegisterStep4Url).
        final String requestUrl = Constant.RegisterStep4Url;
        Log.d(TAG, "Step 4 Request URL: " + requestUrl + " | Params keys: " + params.keySet());

        StringRequest request = new StringRequest(
                Request.Method.POST,
                requestUrl,
                response -> {
                    safeDismiss.run();
                    Log.d(TAG, "Step 4 Response from " + requestUrl + ": " + response);

                    boolean success = response.contains("success") || response.contains("Success")
                            || response.contains("uploaded");
                    if (success) {
                        // Advance to Page 5 only on success
                        viewPager.setCurrentItem(4, true);
                    } else {
                        showStepErrorDialog("Step 4 Failed", "Failed to upload documents:\n" + response, 200);
                    }
                },
                error -> {
                    safeDismiss.run();
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                    if (statusCode == 413) {
                        showStepErrorDialog("Upload Exceeds Limit (Error 413)",
                                "The document upload exceeds the server payload limit. Please compress your PDF files or upload camera photos instead.",
                                statusCode);
                        return;
                    }
                    String serverMsg = "";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            serverMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8).trim();
                        } catch (Exception ignored) {
                        }
                    }
                    if (TextUtils.isEmpty(serverMsg)) {
                        serverMsg = (error instanceof TimeoutError)
                                ? "Network timeout while uploading documents. Please check your connection."
                                : "Unable to upload documents.";
                    }
                    Log.e(TAG, "Step 4 request failed (HTTP " + statusCode + "): " + serverMsg, error);
                    showStepErrorDialog("Step 4 Failed", serverMsg, statusCode);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 1.0f));
        getAppRequestQueue().add(request.setTag(TAG_REGISTRATION));
    }

    // ============================================================
    // STEP 5: REFERRAL & FINALIZE (UPDATE)
    // ============================================================

    private void submitPage5ReferralFinal() {
        if (isSubmitting)
            return;

        if (TextUtils.isEmpty(registeredExpertId)) {
            showStepErrorDialog("Step 1 Required", "Please complete Personal Details (Step 1) before proceeding.", 0);
            viewPager.setCurrentItem(0, true);
            return;
        }

        final String referral = etReferral != null && etReferral.getText() != null
                ? etReferral.getText().toString().trim()
                : "";
        final String mobile = ccp != null ? ccp.getFullNumberWithPlus() : formMobile;

        final Map<String, String> params = new HashMap<>();
        // TODO: Backend confirmation: Confirm whether primary key parameter is named
        // "id" or "expert_id" for UPDATE.
        params.put("id", registeredExpertId);
        // TODO: Backend confirmation: Confirm whether referral parameter name is
        // "bounsReferral" or "referral_code".
        params.put("bounsReferral", referral);

        if (!checkPayloadSizeOrAlert(params, "Page 5")) {
            return;
        }

        isSubmitting = true;
        if (btnNext != null) {
            btnNext.setEnabled(false);
            btnNext.setAlpha(0.6f);
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Finalizing registration...");
        progressDialog.setCancelable(false);
        if (!isFinishing() && !isDestroyed()) {
            progressDialog.show();
        }

        Runnable safeDismiss = createSafeDismiss(progressDialog);

        // TODO: Backend confirmation: Confirm the exact endpoint URL for Page 5
        // finalize (e.g. Constant.RegisterStep5Url).
        final String requestUrl = Constant.RegisterStep5Url;
        Log.d(TAG, "Step 5 Request URL: " + requestUrl + " | Params keys: " + params.keySet());

        StringRequest request = new StringRequest(
                Request.Method.POST,
                requestUrl,
                response -> {
                    safeDismiss.run();
                    Log.d(TAG, "Step 5 Response from " + requestUrl + ": " + response);

                    if (response.contains("The code is not valid")) {
                        showStepErrorDialog("Referral Error", "Invalid referral code. Please check and try again.",
                                400);
                        return;
                    }

                    boolean success = response.contains("Successfully") || response.contains("success")
                            || response.contains("Success");
                    if (success) {
                        Toast.makeText(this, "Registration Success", Toast.LENGTH_SHORT).show();
                        sendEmail(mobile);

                        getSharedPreferences("credentials", MODE_PRIVATE)
                                .edit()
                                .putString("Id", registeredExpertId)
                                .putString("userType", "expert")
                                .apply();

                        Intent intent = new Intent(Register.this, LoginActivity.class);
                        intent.putExtra("EXTRA_EXPERT_ID", registeredExpertId);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        showStepErrorDialog("Registration Failed", "Failed to finalize registration:\n" + response,
                                200);
                    }
                },
                error -> {
                    safeDismiss.run();
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                    String serverMsg = "";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            serverMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8).trim();
                        } catch (Exception ignored) {
                        }
                    }
                    if (TextUtils.isEmpty(serverMsg)) {
                        serverMsg = (error instanceof TimeoutError) ? "Network timeout. Please check your connection."
                                : "Unable to finalize registration.";
                    }
                    Log.e(TAG, "Step 5 request failed (HTTP " + statusCode + "): " + serverMsg, error);
                    showStepErrorDialog("Registration Failed", serverMsg, statusCode);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 1.0f));
        getAppRequestQueue().add(request.setTag(TAG_REGISTRATION));
    }

    // ============================================================
    // SEND EMAIL
    // ============================================================

    private void sendEmail(String mobile) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constant.email,
                response -> Log.d(TAG, "Email response: " + response),
                error -> Log.e(TAG, "Email request failed", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("phone", mobile);
                return params;
            }
        };

        getAppRequestQueue().add(request.setTag(TAG_PREFETCH));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG_PREFETCH);
        }
    }

    // ============================================================
    // BACK BUTTON
    // ============================================================

    @Override
    public void onBackPressed() {
        if (currentPage > 0) {
            viewPager.setCurrentItem(currentPage - 1, true);
        } else {
            super.onBackPressed();
        }
    }
}