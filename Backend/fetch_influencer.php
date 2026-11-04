<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require "../connection.php";

$db = isset($con) ? $con : (isset($conn) ? $conn : null);

if (!$db) {
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "success" => false,
        "message" => "Database connection failed"
    ]);
    exit();
}

// Only ID input
$id = isset($_GET['id']) ? trim($_GET['id']) :
      (isset($_POST['id']) ? trim($_POST['id']) : null);

// Validate ID
if (empty($id) || !ctype_digit($id)) {
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "success" => false,
        "message" => "ID is required"
    ]);
    exit();
}

// SQL Query - Only selected columns
$sql = "SELECT
            id,
            partner_category,
            full_name,
            channel_url,
            profile_image_url,
            referral_code,
            device_token,
            login,
            verification,
            created_at
        FROM influencer_registration
        WHERE id = " . (int)$id . "
        LIMIT 1";

$result = mysqli_query($db, $sql);

if (!$result) {
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "success" => false,
        "message" => mysqli_error($db)
    ]);
    exit();
}

if (mysqli_num_rows($result) == 0) {
    http_response_code(404);
    echo json_encode([
        "status" => "fail",
        "success" => false,
        "message" => "No influencer found"
    ]);
    exit();
}

// Fetch selected columns
$row = mysqli_fetch_assoc($result);

mysqli_free_result($result);
mysqli_close($db);

// Response
echo json_encode([
    "status" => "success",
    "success" => true,
    "message" => "Influencer profile retrieved successfully",
    "data" => $row
], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

exit();
?>
