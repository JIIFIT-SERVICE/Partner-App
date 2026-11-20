<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Read inputs from POST or JSON body
$partnerCategory = isset($_POST['partner_category']) ? trim($_POST['partner_category']) : '';
$fullName        = isset($_POST['full_name']) ? trim($_POST['full_name']) : '';
$countryCode     = isset($_POST['country_code']) ? trim($_POST['country_code']) : '+91';
$mobileNumber    = isset($_POST['mobile_number']) ? trim($_POST['mobile_number']) : '';
$email           = isset($_POST['email']) ? trim($_POST['email']) : '';
$channelUrl      = isset($_POST['channel_url']) ? trim($_POST['channel_url']) : '';
$profileImageUrl = isset($_POST['profile_image_url']) ? trim($_POST['profile_image_url']) : '';

// Validation check
if (empty($fullName) || empty($mobileNumber) || empty($email)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "status"  => false,
        "message" => "Please provide full name, mobile number and email."
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
    exit();
}

// Handle optional profile image file upload if uploaded as multipart
if (isset($_FILES['profile_image']) && $_FILES['profile_image']['error'] === UPLOAD_ERR_OK) {
    $uploadDir = __DIR__ . '/uploads/influencers/';
    if (!is_dir($uploadDir)) {
        mkdir($uploadDir, 0777, true);
    }
    $fileName = 'inf_' . time() . '_' . rand(100, 999) . '_' . preg_replace('/[^a-zA-Z0-9._-]/', '', basename($_FILES['profile_image']['name']));
    $targetPath = $uploadDir . $fileName;
    if (move_uploaded_file($_FILES['profile_image']['tmp_name'], $targetPath)) {
        $profileImageUrl = 'uploads/influencers/' . $fileName;
    }
}

// Generate registration reference ID
$generatedPartnerId = "INF" . rand(10000, 99999);

$response = [
    "success"     => true,
    "status"      => true,
    "message"     => "Partner registration submitted successfully",
    "partner_id"  => $generatedPartnerId,
    "data"        => [
        "partner_id"        => $generatedPartnerId,
        "partner_category"  => $partnerCategory,
        "full_name"         => $fullName,
        "country_code"      => $countryCode,
        "mobile_number"     => $mobileNumber,
        "full_mobile"       => $countryCode . $mobileNumber,
        "email"             => $email,
        "channel_url"       => $channelUrl,
        "profile_image_url" => $profileImageUrl,
        "registered_at"     => date('Y-m-d H:i:s')
    ]
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
