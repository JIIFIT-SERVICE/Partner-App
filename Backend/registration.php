<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

require '../connection.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);

    echo json_encode([
        "status" => false,
        "message" => "Only POST method is allowed"
    ]);
    exit;
}

// Read POST data
$partner_category = trim($_POST['partner_category'] ?? '');
$full_name        = trim($_POST['full_name'] ?? '');
$country_code     = trim($_POST['country_code'] ?? '+91');
$mobile_number    = trim($_POST['mobile_number'] ?? '');
$email            = trim($_POST['email'] ?? '');
$channel_url      = trim($_POST['channel_url'] ?? '');
$profile_image_url = null;

// Required field validation
if (
    $partner_category === '' ||
    $full_name === '' ||
    $mobile_number === ''
) {
    http_response_code(400);

    echo json_encode([
        "status" => false,
        "message" => "Partner category, full name and mobile number are required"
    ]);
    exit;
}

// Validate email if provided
if ($email !== '' && !filter_var($email, FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);

    echo json_encode([
        "status" => false,
        "message" => "Invalid email address"
    ]);
    exit;
}

// Optional image upload
if (isset($_FILES['profile_image'])) {

    if ($_FILES['profile_image']['error'] !== UPLOAD_ERR_OK) {
        http_response_code(400);

        echo json_encode([
            "status" => false,
            "message" => "Image upload failed"
        ]);
        exit;
    }

    $tmpFile = $_FILES['profile_image']['tmp_name'];
    $fileSize = $_FILES['profile_image']['size'];

    // Maximum image size: 5 MB
    if ($fileSize > 5 * 1024 * 1024) {
        http_response_code(400);

        echo json_encode([
            "status" => false,
            "message" => "Image size must be 5 MB or less"
        ]);
        exit;
    }

    // Validate actual image type
    $imageInfo = @getimagesize($tmpFile);

    $allowedTypes = [
        'image/jpeg' => 'jpg',
        'image/png'  => 'png',
        'image/webp' => 'webp'
    ];

    if (
        $imageInfo === false ||
        !isset($allowedTypes[$imageInfo['mime']])
    ) {
        http_response_code(400);

        echo json_encode([
            "status" => false,
            "message" => "Only JPG, PNG and WEBP images are allowed"
        ]);
        exit;
    }

    // Folder location
    $uploadDir = __DIR__ . 'profile photo';

    // Create folder if it does not exist
    if (!is_dir($uploadDir)) {
        if (!mkdir($uploadDir, 0755, true) && !is_dir($uploadDir)) {
            http_response_code(500);

            echo json_encode([
                "status" => false,
                "message" => "Unable to create image folder"
            ]);
            exit;
        }
    }

    // Generate unique filename
    $extension = $allowedTypes[$imageInfo['mime']];
    $fileName = 'influencer_' . bin2hex(random_bytes(16)) . '.' . $extension;

    $targetPath = $uploadDir . $fileName;

    // Save image to folder
    if (!move_uploaded_file($tmpFile, $targetPath)) {
        http_response_code(500);

        echo json_encode([
            "status" => false,
            "message" => "Failed to save image"
        ]);
        exit;
    }

    // Relative path stored in database
   $profile_image_url = 'profile photo/' . $fileName;
}

// Generate registration UUID
$registration_uuid = sprintf(
    '%04x%04x-%04x-%04x-%04x-%04x%04x%04x',
    mt_rand(0, 65535),
    mt_rand(0, 65535),
    mt_rand(0, 65535),
    mt_rand(16384, 20479),
    mt_rand(32768, 49151),
    mt_rand(0, 65535),
    mt_rand(0, 65535),
    mt_rand(0, 65535)
);

// Convert empty optional values to NULL
$email = ($email === '') ? null : $email;
$channel_url = ($channel_url === '') ? null : $channel_url;

// Insert data
$sql = "INSERT INTO influencer_registration
(
    registration_uuid,
    partner_category,
    full_name,
    country_code,
    mobile_number,
    email,
    channel_url,
    profile_image_url,
    created_at
)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

$stmt = mysqli_prepare($con, $sql);

if (!$stmt) {
    http_response_code(500);

    echo json_encode([
        "status" => false,
        "message" => "Failed to prepare database query"
    ]);
    exit;
}

mysqli_stmt_bind_param(
    $stmt,
    "ssssssss",
    $registration_uuid,
    $partner_category,
    $full_name,
    $country_code,
    $mobile_number,
    $email,
    $channel_url,
    $profile_image_url
);

if (mysqli_stmt_execute($stmt)) {

    http_response_code(201);

    echo json_encode([
        "status" => true,
        "message" => "Influencer registration submitted successfully",
        "data" => [
            "id" => mysqli_insert_id($con),
            "registration_uuid" => $registration_uuid,
            "partner_category" => $partner_category,
            "full_name" => $full_name,
            "mobile_number" => $mobile_number,
            "email" => $email,
            "channel_url" => $channel_url,
            "profile_image_url" => $profile_image_url
        ]
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

} else {

    if (mysqli_stmt_errno($stmt) === 1062) {
        http_response_code(409);

        echo json_encode([
            "status" => false,
            "message" => "This mobile number is already registered"
        ]);
    } else {
        http_response_code(500);

        echo json_encode([
            "status" => false,
            "message" => "Registration failed"
        ]);
    }
}

mysqli_stmt_close($stmt);
mysqli_close($con);
?>