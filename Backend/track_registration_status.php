<?php
header('Content-Type: application/json; charset=utf-8');

try {
    require "../connection.php";
} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode([
        "status"  => false,
        "message" => "Database connection error on server: " . $e->getMessage() . ". Please ensure 'mysqli' PHP extension is enabled in cPanel."
    ]);
    exit();
}

$db = isset($con) ? $con : (isset($conn) ? $conn : null);

if (!$db) {
    http_response_code(500);
    echo json_encode([
        "status"  => false, 
        "message" => "Database connection failed. Please ensure 'mysqli' PHP extension is enabled in cPanel."
    ]);
    exit();
}

$id = 0;
if (isset($_POST['id']) && intval($_POST['id']) > 0) {
    $id = intval($_POST['id']);
} elseif (isset($_GET['id']) && intval($_GET['id']) > 0) {
    $id = intval($_GET['id']);
} elseif (isset($_POST['therapist_id']) && intval($_POST['therapist_id']) > 0) {
    $id = intval($_POST['therapist_id']);
} elseif (isset($_GET['therapist_id']) && intval($_GET['therapist_id']) > 0) {
    $id = intval($_GET['therapist_id']);
}

$rawPhone = isset($_POST['phone']) ? trim($_POST['phone']) : (isset($_GET['phone']) ? trim($_GET['phone']) : '');
$cleanPhone = preg_replace('/[^0-9]/', '', $rawPhone);
$last10 = strlen($cleanPhone) >= 10 ? substr($cleanPhone, -10) : $cleanPhone;

if ($id > 0) {
    $sql = "SELECT * FROM therapists WHERE id = $id LIMIT 1";
} elseif (strlen($last10) >= 10) {
    $sql = "SELECT * FROM therapists WHERE RIGHT(REPLACE(REPLACE(phone, '+', ''), ' ', ''), 10) = '$last10' LIMIT 1";
} else {
    http_response_code(400);
    echo json_encode(["status" => false, "message" => "Valid therapist ID or 10-digit phone number is required"]);
    exit();
}

$res = mysqli_query($db, $sql);

if (!$res || mysqli_num_rows($res) === 0) {
    http_response_code(404);
    echo json_encode(["status" => false, "message" => "Therapist not found"]);
    exit();
}

$row = mysqli_fetch_assoc($res);

$firstLayout  = isset($row['1st_layout']) ? intval($row['1st_layout']) : 0;
$secondLayout = isset($row['2nd_layout']) ? intval($row['2nd_layout']) : 0;
$thirdLayout  = isset($row['3rd_layout']) ? intval($row['3rd_layout']) : 0;
$fourthLayout = isset($row['4th_layout']) ? intval($row['4th_layout']) : 0;
$fifthLayout  = isset($row['5th_layout']) ? intval($row['5th_layout']) : 0;
$verification = isset($row['verification']) && trim($row['verification']) !== '' ? trim($row['verification']) : 'UNVERIFIED';

// Fallback inference if layout columns were not initialized in an older database record
if ($firstLayout === 0 && !empty($row['name']) && !empty($row['phone'])) {
    $firstLayout = 1;
}
if ($secondLayout === 0 && (!empty($row['categories']) || !empty($row['expert_category']))) {
    $secondLayout = 1;
}
if ($thirdLayout === 0 && (!empty($row['city_id']) && !empty($row['area_ids']))) {
    $thirdLayout = 1;
}
if ($fourthLayout === 0 && (!empty($row['resume_file']) || !empty($row['adhaar_picture']) || !empty($row['aadhaar_front']))) {
    $fourthLayout = 1;
}
if ($fifthLayout === 0 && $fourthLayout === 1 && (isset($row['bounsReferral']) || $verification === 'VERIFIED')) {
    $fifthLayout = 1;
}

$baseUrl = "https://jiifit.com/Jifiit/Therapist/";

$resumeFile       = !empty($row['resume_file']) ? $row['resume_file'] : '';
$aadhaarFrontFile = !empty($row['adhaar_picture']) ? $row['adhaar_picture'] : (!empty($row['aadhaar_front']) ? $row['aadhaar_front'] : '');
$aadhaarBackFile  = !empty($row['aadhaar_back']) ? $row['aadhaar_back'] : '';
$madhyamikFile    = !empty($row['madhyamik_certificate']) ? $row['madhyamik_certificate'] : '';
$hsFile           = !empty($row['hs_certificate']) ? $row['hs_certificate'] : '';
$gradFile         = !empty($row['graduation_certificate']) ? $row['graduation_certificate'] : '';
$photoFile        = !empty($row['profile_photo']) ? $row['profile_photo'] : '';

$resumeUrl       = !empty($resumeFile) ? $baseUrl . 'Resume/' . $resumeFile : '';
$aadhaarFrontUrl = !empty($aadhaarFrontFile) ? $baseUrl . 'IdProof/' . $aadhaarFrontFile : '';
$aadhaarBackUrl  = !empty($aadhaarBackFile) ? $baseUrl . 'aadharback/' . $aadhaarBackFile : '';
$madhyamikUrl    = !empty($madhyamikFile) ? $baseUrl . 'Madhyamik/' . $madhyamikFile : '';
$hsUrl           = !empty($hsFile) ? $baseUrl . 'HigherSecondary/' . $hsFile : '';
$gradUrl         = !empty($gradFile) ? $baseUrl . 'Graduation/' . $gradFile : '';
$photoUrl        = !empty($photoFile) ? $baseUrl . 'PassportSize/' . $photoFile : '';

echo json_encode([
    "status"             => true,
    "id"                 => intval($row['id']),
    "name"               => isset($row['name']) ? $row['name'] : '',
    "phone"              => isset($row['phone']) ? $row['phone'] : '',
    "email"              => isset($row['email']) ? $row['email'] : '',
    "city_id"            => isset($row['city_id']) ? $row['city_id'] : '',
    "created_at"         => isset($row['created_at']) ? $row['created_at'] : '',
    "verification"       => $verification,
    "1st_layout"         => $firstLayout,
    "2nd_layout"         => $secondLayout,
    "3rd_layout"         => $thirdLayout,
    "4th_layout"         => $fourthLayout,
    "5th_layout"         => $fifthLayout,
    "resume_url"         => $resumeUrl,
    "resume_file"        => $resumeFile,
    "aadhaar_front_url"  => $aadhaarFrontUrl,
    "aadhaar_front_file" => $aadhaarFrontFile,
    "aadhaar_back_url"   => $aadhaarBackUrl,
    "aadhaar_back_file"  => $aadhaarBackFile,
    "madhyamik_url"      => $madhyamikUrl,
    "madhyamik_file"     => $madhyamikFile,
    "hs_url"             => $hsUrl,
    "hs_file"            => $hsFile,
    "graduation_url"     => $gradUrl,
    "graduation_file"    => $gradFile,
    "profile_photo_url"  => $photoUrl,
    "profile_photo_file" => $photoFile
]);

mysqli_close($db);
?>
