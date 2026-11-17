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

$existingId = isset($_POST['id']) ? intval($_POST['id']) : (isset($_POST['expert_id']) ? intval($_POST['expert_id']) : 0);

$t_Name    = isset($_POST['name']) ? mysqli_real_escape_string($db, trim($_POST['name'])) : '';
$t_Mobile  = isset($_POST['phone']) ? mysqli_real_escape_string($db, trim($_POST['phone'])) : '';
$t_Email   = isset($_POST['email']) ? mysqli_real_escape_string($db, trim($_POST['email'])) : '';
$t_aadhaar = isset($_POST['aadhar_card_no']) ? mysqli_real_escape_string($db, preg_replace('/\s+/', '', trim($_POST['aadhar_card_no']))) : '';
$t_Address = isset($_POST['address']) ? mysqli_real_escape_string($db, trim($_POST['address'])) : '';
$t_Age     = isset($_POST['date_of_birth']) ? mysqli_real_escape_string($db, trim($_POST['date_of_birth'])) : '';
$t_gender  = isset($_POST['gender']) ? mysqli_real_escape_string($db, trim($_POST['gender'])) : '';
$t_Photo   = isset($_POST["profile_photo"]) ? $_POST["profile_photo"] : '';
$rawCity   = isset($_POST["city_id"]) ? trim($_POST["city_id"]) : '';

$cityMap = [
    'Delhi' => 1, 'New Delhi' => 2, 'Kolkata' => 3, 'Bengaluru' => 4,
    'Hyderabad' => 5, 'Chennai' => 6, 'Pune' => 7, 'Ahmedabad' => 8,
    'Gurgaon' => 9, 'Noida' => 10
];
$CityId = 1;
if (is_numeric($rawCity) && intval($rawCity) > 0) {
    $CityId = intval($rawCity);
} elseif (array_key_exists($rawCity, $cityMap)) {
    $CityId = $cityMap[$rawCity];
}

function saveDocFile($data, $folder, $prefix = 'DOC') {
    if (empty($data)) return '';
    if (strlen($data) < 255 && !preg_match('/^[a-zA-Z0-9\/+]+={0,2}$/', $data)) return $data;
    if (!is_dir($folder)) mkdir($folder, 0777, true);
    
    $ext = '.jpg';
    if (strpos($data, 'application/pdf') !== false) $ext = '.pdf';
    elseif (strpos($data, 'image/png') !== false) $ext = '.png';

    $clean = preg_replace('/^data:[a-zA-Z0-9\/+-]+;base64,/', '', $data);
    $decoded = base64_decode($clean);
    if ($decoded === false) return $data;

    $filename = $prefix . "_" . time() . "_" . rand(1000, 9999) . $ext;
    file_put_contents($folder . '/' . $filename, $decoded);
    return $filename;
}

$filenamePhoto = saveDocFile($t_Photo, 'PassportSize', 'PHOTO');

$cleanPhone = preg_replace('/[^0-9]/', '', $t_Mobile);
$last10Mobile = strlen($cleanPhone) >= 10 ? substr($cleanPhone, -10) : $cleanPhone;

// 1. Check if therapist record already exists (prevents duplicate database rows)
$existingRow = null;
if ($existingId > 0) {
    $checkQuery = "SELECT * FROM therapists WHERE id = $existingId LIMIT 1";
    $res = mysqli_query($db, $checkQuery);
    if ($res && mysqli_num_rows($res) > 0) {
        $existingRow = mysqli_fetch_assoc($res);
    }
}

if (!$existingRow && strlen($last10Mobile) >= 10) {
    $checkPhone = "SELECT * FROM therapists WHERE RIGHT(REPLACE(REPLACE(phone, '+', ''), ' ', ''), 10) = '$last10Mobile' LIMIT 1";
    $res = mysqli_query($db, $checkPhone);
    if ($res && mysqli_num_rows($res) > 0) {
        $existingRow = mysqli_fetch_assoc($res);
    }
}

if (!$existingRow && strlen($t_aadhaar) >= 12) {
    $checkAadhaar = "SELECT * FROM therapists WHERE REPLACE(aadhar_card_no, ' ', '') = '$t_aadhaar' LIMIT 1";
    $res = mysqli_query($db, $checkAadhaar);
    if ($res && mysqli_num_rows($res) > 0) {
        $existingRow = mysqli_fetch_assoc($res);
    }
}

// 2. If therapist already exists in DB, UPDATE their details instead of creating duplicates!
if ($existingRow) {
    $targetId = intval($existingRow['id']);
    
    $photoUpdate = !empty($filenamePhoto) ? ", profile_photo = '$filenamePhoto'" : "";
    $sqlUpdate = "UPDATE therapists SET 
                    name = '$t_Name',
                    email = '$t_Email',
                    phone = '$t_Mobile',
                    aadhar_card_no = '$t_aadhaar',
                    address = '$t_Address',
                    date_of_birth = '$t_Age',
                    gender = '$t_gender',
                    city_id = '$CityId',
                    `1st_layout` = 1
                    $photoUpdate
                  WHERE id = $targetId";
                  
    if (!mysqli_query($db, $sqlUpdate)) {
        // Fallback without layout column
        $sqlFallbackUpdate = "UPDATE therapists SET 
                                name = '$t_Name',
                                email = '$t_Email',
                                phone = '$t_Mobile',
                                aadhar_card_no = '$t_aadhaar',
                                address = '$t_Address',
                                date_of_birth = '$t_Age',
                                gender = '$t_gender',
                                city_id = '$CityId'
                                $photoUpdate
                              WHERE id = $targetId";
        mysqli_query($db, $sqlFallbackUpdate);
    }
    
    $l1 = 1;
    $l2 = isset($existingRow['2nd_layout']) ? intval($existingRow['2nd_layout']) : 0;
    $l3 = isset($existingRow['3rd_layout']) ? intval($existingRow['3rd_layout']) : 0;
    $l4 = isset($existingRow['4th_layout']) ? intval($existingRow['4th_layout']) : 0;
    $l5 = isset($existingRow['5th_layout']) ? intval($existingRow['5th_layout']) : 0;
    $ver = isset($existingRow['verification']) && trim($existingRow['verification']) !== '' ? trim($existingRow['verification']) : 'UNVERIFIED';

    echo json_encode([
        "status"       => true,
        "id"           => $targetId,
        "expert_id"    => $targetId,
        "1st_layout"   => $l1,
        "2nd_layout"   => $l2,
        "3rd_layout"   => $l3,
        "4th_layout"   => $l4,
        "5th_layout"   => $l5,
        "verification" => $ver,
        "is_existing"  => true,
        "message"      => "Existing registration details updated successfully"
    ]);
    mysqli_close($db);
    exit();
}

// 3. New therapist: INSERT into therapists table
$spacePosition = strpos($t_Name, ' ');
$namePart = ($spacePosition !== false) ? substr($t_Name, 0, $spacePosition) : $t_Name;
$referralCode = strtolower(substr($namePart, 0, 1)) . substr($namePart, 1) . strtoupper(bin2hex(random_bytes(2)));
$created_at = date('Y-m-d H:i:s');

$sql = "INSERT INTO therapists (
            name, phone, email, aadhar_card_no, address, date_of_birth, gender, 
            profile_photo, created_at, verification, referral_code, referral_payment, 
            off_day, yearly_off, illness, city_id,
            `1st_layout`, `2nd_layout`, `3rd_layout`, `4th_layout`, `5th_layout`
        ) VALUES (
            '$t_Name', '$t_Mobile', '$t_Email', '$t_aadhaar', '$t_Address', '$t_Age', '$t_gender', 
            '$filenamePhoto', '$created_at', 'UNVERIFIED', '$referralCode', '0', 
            '1', '18', '10', '$CityId',
            1, 0, 0, 0, 0
        )";

if (!mysqli_query($db, $sql)) {
    // Fallback in case layout columns are not yet present in table schema
    $sqlFallback = "INSERT INTO therapists (
                name, phone, email, aadhar_card_no, address, date_of_birth, gender, 
                profile_photo, created_at, verification, referral_code, referral_payment, 
                off_day, yearly_off, illness, city_id
            ) VALUES (
                '$t_Name', '$t_Mobile', '$t_Email', '$t_aadhaar', '$t_Address', '$t_Age', '$t_gender', 
                '$filenamePhoto', '$created_at', 'UNVERIFIED', '$referralCode', '0', 
                '1', '18', '10', '$CityId'
            )";
    if (!mysqli_query($db, $sqlFallback)) {
        http_response_code(500);
        echo json_encode(["status" => false, "message" => "Error inserting record: " . mysqli_error($db)]);
        exit();
    }
}

$newId = mysqli_insert_id($db);
echo json_encode([
    "status"       => true, 
    "id"           => $newId, 
    "expert_id"    => $newId, 
    "1st_layout"   => 1,
    "2nd_layout"   => 0,
    "3rd_layout"   => 0,
    "4th_layout"   => 0,
    "5th_layout"   => 0,
    "verification" => 'UNVERIFIED',
    "is_existing"  => false,
    "message"      => "Personal details registered successfully"
]);

mysqli_close($db);
?>
