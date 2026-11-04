<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require "../connection.php";

// Support both $con and $conn variable names from connection.php
$db = isset($con) ? $con : (isset($conn) ? $conn : null);

if (!$db) {
    http_response_code(500);
    echo json_encode([
        "status"  => "error",
        "success" => false,
        "message" => "Database connection failed"
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
    exit();
}

// 1. Read input filters from GET, POST, or JSON body
$rawInput = file_get_contents('php://input');
$jsonData = !empty($rawInput) ? json_decode($rawInput, true) : [];

$id           = isset($_GET['id']) ? trim($_GET['id']) : (isset($_POST['id']) ? trim($_POST['id']) : ($jsonData['id'] ?? null));
$phone        = isset($_GET['phone']) ? trim($_GET['phone']) : (isset($_POST['phone']) ? trim($_POST['phone']) : (isset($_GET['mobile']) ? trim($_GET['mobile']) : ($jsonData['phone'] ?? ($jsonData['mobile'] ?? null))));
$referralCode = isset($_GET['referral_code']) ? trim($_GET['referral_code']) : (isset($_POST['referral_code']) ? trim($_POST['referral_code']) : (isset($_GET['code']) ? trim($_GET['code']) : ($jsonData['referral_code'] ?? ($jsonData['code'] ?? null))));

// 2. Base SQL Query
$sql = "SELECT 
            `id`, 
            `partner_category`, 
            `full_name`, 
            `channel_url`, 
            `profile_image_url`, 
            `referral_code`, 
            `device_token`, 
            `login`, 
            `last_login_at`, 
            `verification`, 
            `terms_accepted`, 
            `terms_accepted_at`, 
            `created_at` 
        FROM `influencer_registration` 
        WHERE 1";

// Dynamic filtering based on passed inputs
$whereClause = "";

if (!empty($id)) {
    $safeId = (int)$id;
    $whereClause .= " AND `id` = $safeId";
} elseif (!empty($referralCode)) {
    $safeCode = mysqli_real_escape_string($db, $referralCode);
    $whereClause .= " AND `referral_code` = '$safeCode'";
} elseif (!empty($phone)) {
    $safePhone = mysqli_real_escape_string($db, $phone);
    $whereClause .= " AND `mobile_number` = '$safePhone'";
}

$sql .= $whereClause . " ORDER BY `id` DESC";

// If single record lookup by ID / phone / referral code, limit to 1
$isSingleLookup = (!empty($id) || !empty($referralCode) || !empty($phone));
if ($isSingleLookup) {
    $sql .= " LIMIT 1";
}

$result = mysqli_query($db, $sql);
if (!$result) {
    http_response_code(500);
    echo json_encode([
        "status"  => "error",
        "success" => false,
        "message" => "Database query failed: " . mysqli_error($db)
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
    exit();
}

/**
 * Generate 2-letter user initials for circular avatar:
 * Corresponds to @+id/tvDrawerUserInitials in influencer_nav_drawer.xml
 */
function getInitials($name) {
    $parts = preg_split('/\s+/', trim((string)$name));
    if (count($parts) >= 2 && !empty($parts[0]) && !empty($parts[1])) {
        return strtoupper(mb_substr($parts[0], 0, 1) . mb_substr($parts[1], 0, 1));
    } elseif (!empty($parts[0]) && strlen($parts[0]) >= 2) {
        return strtoupper(mb_substr($parts[0], 0, 2));
    }
    return "IP";
}

/**
 * Ensure referral code format matches Android app:
 * Corresponds to @+id/tvReferralCodeDisplay in activity_influencer_main.xml
 */
function formatCode($name, $id, $existingCode) {
    if (!empty($existingCode) && $existingCode !== 'null') {
        return strtoupper(trim($existingCode));
    }
    $cleanPrefix = strtoupper(substr(preg_replace('/[^a-zA-Z]/', '', (string)$name), 0, 4));
    if (strlen($cleanPrefix) < 4) {
        $cleanPrefix = str_pad($cleanPrefix, 4, 'X');
    }
    $codeId = str_pad(substr((string)$id, -4), 4, '0', STR_PAD_LEFT);
    return $cleanPrefix . $codeId;
}

$influencers = [];
$baseUrl = "https://jiifit.com/Jifiit/";

while ($row = mysqli_fetch_assoc($result)) {
    $fullName = $row['full_name'] ?: 'JIIFIT Partner';
    $refCode  = formatCode($fullName, $row['id'], $row['referral_code']);
    $category = !empty($row['partner_category']) ? $row['partner_category'] : 'YouTuber / Content Creator';
    
    // Build full profile image url if relative path is stored
    $profileImage = $row['profile_image_url'];
    $fullProfileImageUrl = "";
    if (!empty($profileImage)) {
        $fullProfileImageUrl = (strpos($profileImage, 'http') === 0) ? $profileImage : $baseUrl . ltrim($profileImage, '/');
    }

    // Structure mapped directly to Android XML views & SharedPreferences
    $formattedItem = [
        // Identification
        "id"                      => (int)$row['id'],
        "partner_id"              => "INF" . str_pad($row['id'], 4, '0', STR_PAD_LEFT),
        "user_type"               => "influencer",

        // Profile & Header (activity_influencer_main.xml & influencer_nav_drawer.xml)
        "full_name"               => $fullName,
        "name"                    => $fullName,                                    // Used by LoginActivity & OTPActivity
        "greeting"                => "Welcome, " . $fullName,                      // tvMainGreeting
        "initials"                => getInitials($fullName),                       // tvDrawerUserInitials
        "partner_category"        => $category,                                    // tvDrawerCategoryBadge
        "role_subtitle"           => "JIIFIT Partner Portal · Doorstep Wellness Referral Engine", // tvMainRoleSubtitle

        // Active Coupon & Sharing (activity_influencer_main.xml)
        "referral_code"           => $refCode,                                     // tvReferralCodeDisplay & tvDrawerReferralCodeBadge
        "share_link"              => "https://jiifit.com/refer?code=" . urlencode($refCode),
        "share_text"              => "Book doorstep wellness services on JIIFIT and save ₹100 using my coupon code: " . $refCode,

        // Media & Social
        "profile_image_url"       => $profileImage,                                // ivDrawerProfilePhoto
        "full_profile_image_url"  => $fullProfileImageUrl,
        "channel_url"             => $row['channel_url'] ?? "",

        // Account Status & Verification
        "verification"            => $row['verification'] ?: "approved",
        "is_verified"             => in_array(strtolower((string)$row['verification']), ['approved', 'verified', '1', 'true']),
        "terms_accepted"          => (int)$row['terms_accepted'],
        "terms_accepted_at"       => $row['terms_accepted_at'],
        "login"                   => (int)$row['login'],
        "last_login_at"           => $row['last_login_at'],
        "device_token"            => $row['device_token'] ?? "",
        "created_at"              => $row['created_at'],

        // Summary Stats (for 2x2 cards in activity_influencer_main.xml)
        "stats"                   => [
            "total_bookings"      => 7,
            "in_wallet"           => "₹532",
            "satisfaction_rate"   => "98%",
            "lifetime_earned"     => "₹3,153"
        ]
    ];

    $influencers[] = $formattedItem;
}

mysqli_free_result($result);
mysqli_close($db);

// Handle Not Found for single record lookup
if ($isSingleLookup && empty($influencers)) {
    http_response_code(404);
    echo json_encode([
        "status"  => "fail",
        "success" => false,
        "message" => "No influencer found matching the provided criteria."
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
    exit();
}

// Return single object in 'data' for lookup by id/code/phone, or array of records
$responseData = $isSingleLookup ? $influencers[0] : $influencers;

$response = [
    "status"      => "success",
    "success"     => true,
    "message"     => "Influencer profile retrieved successfully",
    "count"       => count($influencers),
    "data"        => $responseData
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
?>

