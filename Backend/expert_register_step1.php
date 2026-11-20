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

$t_Name       = isset($_POST['name']) ? mysqli_real_escape_string($db, trim($_POST['name'])) : '';
$t_Mobile     = isset($_POST['phone']) ? mysqli_real_escape_string($db, trim($_POST['phone'])) : (isset($_POST['mobile']) ? mysqli_real_escape_string($db, trim($_POST['mobile'])) : '');
$t_Email      = isset($_POST['email']) ? mysqli_real_escape_string($db, trim($_POST['email'])) : '';
$t_aadhaar    = isset($_POST['aadhar_card_no']) ? mysqli_real_escape_string($db, preg_replace('/\s+/', '', trim($_POST['aadhar_card_no']))) : (isset($_POST['aadhaar']) ? mysqli_real_escape_string($db, preg_replace('/\s+/', '', trim($_POST['aadhaar']))) : '');
$t_Address    = isset($_POST['address']) ? mysqli_real_escape_string($db, trim($_POST['address'])) : '';
$t_Age        = isset($_POST['date_of_birth']) ? mysqli_real_escape_string($db, trim($_POST['date_of_birth'])) : (isset($_POST['dob']) ? mysqli_real_escape_string($db, trim($_POST['dob'])) : '');
$t_gender     = isset($_POST['gender']) ? mysqli_real_escape_string($db, trim($_POST['gender'])) : '';
$t_Photo      = isset($_POST["profile_photo"]) ? $_POST["profile_photo"] : (isset($_POST["photo"]) ? $_POST["photo"] : '');
$t_livingCity = isset($_POST["living_city_name"]) ? mysqli_real_escape_string($db, trim($_POST["living_city_name"])) : '';

// Ensure living_city_name and layout columns exist in therapists table
$colCheck = mysqli_query($db, "SHOW COLUMNS FROM therapists LIKE 'living_city_name'");
if ($colCheck && mysqli_num_rows($colCheck) === 0) {
    @mysqli_query($db, "ALTER TABLE `therapists` ADD COLUMN `living_city_name` VARCHAR(255) NULL DEFAULT NULL");
}
$layoutCols = ['1st_layout', '2nd_layout', '3rd_layout', '4th_layout', '5th_layout'];
foreach ($layoutCols as $col) {
    $cCheck = @mysqli_query($db, "SHOW COLUMNS FROM therapists LIKE '$col'");
    if ($cCheck && mysqli_num_rows($cCheck) === 0) {
        @mysqli_query($db, "ALTER TABLE `therapists` ADD COLUMN `$col` TINYINT(1) NOT NULL DEFAULT 0");
    }
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

// Case 1: Client provided existing expert ID (e.g. user navigated Back from Step 2 to Step 1 or has local session)
if ($existingId > 0) {
    $checkQuery = "SELECT * FROM therapists WHERE id = $existingId LIMIT 1";
    $res = mysqli_query($db, $checkQuery);
    if ($res && mysqli_num_rows($res) > 0) {
        $existingRow = mysqli_fetch_assoc($res);

        // Security check: verify this ID matches the phone or Aadhaar provided
        $dbPhoneLast10 = strlen($existingRow['phone']) >= 10 ? substr(preg_replace('/[^0-9]/', '', $existingRow['phone']), -10) : '';
        $dbAadhaarClean = preg_replace('/\s+/', '', $existingRow['aadhar_card_no']);

        if (!empty($dbPhoneLast10) && !empty($dbAadhaarClean) && 
            $dbPhoneLast10 !== $last10Mobile && $dbAadhaarClean !== $t_aadhaar) {
            http_response_code(403);
            echo json_encode([
                "status"  => false,
                "message" => "Security verification failed: Expert ID does not match registration credentials."
            ]);
            mysqli_close($db);
            exit();
        }

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
                        living_city_name = '$t_livingCity',
                        `1st_layout` = 1
                        $photoUpdate
                      WHERE id = $targetId";
                      
        if (!mysqli_query($db, $sqlUpdate)) {
            $sqlFallbackUpdate = "UPDATE therapists SET 
                                    name = '$t_Name',
                                    email = '$t_Email',
                                    phone = '$t_Mobile',
                                    aadhar_card_no = '$t_aadhaar',
                                    address = '$t_Address',
                                    date_of_birth = '$t_Age',
                                    gender = '$t_gender',
                                    living_city_name = '$t_livingCity'
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
            "status"           => true,
            "id"               => $targetId,
            "expert_id"        => $targetId,
            "1st_layout"       => $l1,
            "2nd_layout"       => $l2,
            "3rd_layout"       => $l3,
            "4th_layout"       => $l4,
            "5th_layout"       => $l5,
            "verification"     => $ver,
            "is_existing"      => true,
            "recovered"        => false,
            "living_city_name" => $t_livingCity,
            "message"          => "Personal details updated successfully"
        ]);
        mysqli_close($db);
        exit();
    }
}

// Case 2: No existing ID provided. Check whether mobile or Aadhaar already exists in database.
$phoneRow = null;
$aadhaarRow = null;

if (strlen($last10Mobile) >= 10) {
    $resP = mysqli_query($db, "SELECT * FROM therapists WHERE RIGHT(REPLACE(REPLACE(phone, '+', ''), ' ', ''), 10) = '$last10Mobile' LIMIT 1");
    if ($resP && mysqli_num_rows($resP) > 0) {
        $phoneRow = mysqli_fetch_assoc($resP);
    }
}

if (strlen($t_aadhaar) >= 12) {
    $resA = mysqli_query($db, "SELECT * FROM therapists WHERE REPLACE(aadhar_card_no, ' ', '') = '$t_aadhaar' LIMIT 1");
    if ($resA && mysqli_num_rows($resA) > 0) {
        $aadhaarRow = mysqli_fetch_assoc($resA);
    }
}

// Case 2A: Conflict - Mobile number and Aadhaar number match different therapists
if ($phoneRow && $aadhaarRow && intval($phoneRow['id']) !== intval($aadhaarRow['id'])) {
    http_response_code(409);
    echo json_encode([
        "status"   => false,
        "conflict" => true,
        "message"  => "The provided mobile number and Aadhaar number are registered under different accounts. Please check your credentials."
    ]);
    mysqli_close($db);
    exit();
}

// Case 2B: Mismatch - Mobile number is already registered with a different Aadhaar number
if ($phoneRow && strlen($t_aadhaar) >= 12) {
    $dbAadhaar = preg_replace('/\s+/', '', $phoneRow['aadhar_card_no']);
    if (!empty($dbAadhaar) && $dbAadhaar !== $t_aadhaar) {
        http_response_code(409);
        echo json_encode([
            "status"   => false,
            "conflict" => true,
            "message"  => "This mobile number is already registered with a different Aadhaar number. Please use your registered phone number."
        ]);
        mysqli_close($db);
        exit();
    }
}

// Case 2C: Mismatch - Aadhaar is already registered with a different mobile number
if ($aadhaarRow && strlen($last10Mobile) >= 10) {
    $dbPhoneClean = preg_replace('/[^0-9]/', '', $aadhaarRow['phone']);
    $dbPhoneLast10 = strlen($dbPhoneClean) >= 10 ? substr($dbPhoneClean, -10) : $dbPhoneClean;
    if (!empty($dbPhoneLast10) && $dbPhoneLast10 !== $last10Mobile) {
        http_response_code(409);
        echo json_encode([
            "status"   => false,
            "conflict" => true,
            "message"  => "This Aadhaar number is already registered with a different mobile number. Please check your credentials."
        ]);
        mysqli_close($db);
        exit();
    }
}

// Case 2D & 2E: Either phone or Aadhaar matched an existing therapist
$matchedRow = $phoneRow ? $phoneRow : $aadhaarRow;

if ($matchedRow) {
    $targetId = intval($matchedRow['id']);
    $l1 = isset($matchedRow['1st_layout']) ? intval($matchedRow['1st_layout']) : 1;
    $l2 = isset($matchedRow['2nd_layout']) ? intval($matchedRow['2nd_layout']) : 0;
    $l3 = isset($matchedRow['3rd_layout']) ? intval($matchedRow['3rd_layout']) : 0;
    $l4 = isset($matchedRow['4th_layout']) ? intval($matchedRow['4th_layout']) : 0;
    $l5 = isset($matchedRow['5th_layout']) ? intval($matchedRow['5th_layout']) : 0;
    $ver = isset($matchedRow['verification']) && trim($matchedRow['verification']) !== '' ? trim($matchedRow['verification']) : 'UNVERIFIED';

    $isCompleted = ($l5 === 1 || strtoupper($ver) === 'VERIFIED');

    // Case 2D: Account is already fully registered or verified - redirect to login
    if ($isCompleted) {
        echo json_encode([
            "status"             => false,
            "already_registered" => true,
            "message"            => "An account with this mobile number or Aadhaar already exists and is fully registered. Please log in."
        ]);
        mysqli_close($db);
        exit();
    }

    // Case 2E: Existing incomplete registration draft recovery!
    $photoUpdate = !empty($filenamePhoto) ? ", profile_photo = '$filenamePhoto'" : "";
    $sqlUpdate = "UPDATE therapists SET 
                    name = '$t_Name',
                    email = '$t_Email',
                    phone = '$t_Mobile',
                    aadhar_card_no = '$t_aadhaar',
                    address = '$t_Address',
                    date_of_birth = '$t_Age',
                    gender = '$t_gender',
                    living_city_name = '$t_livingCity',
                    `1st_layout` = 1
                    $photoUpdate
                  WHERE id = $targetId";
                  
    if (!mysqli_query($db, $sqlUpdate)) {
        $sqlFallbackUpdate = "UPDATE therapists SET 
                                name = '$t_Name',
                                email = '$t_Email',
                                phone = '$t_Mobile',
                                aadhar_card_no = '$t_aadhaar',
                                address = '$t_Address',
                                date_of_birth = '$t_Age',
                                gender = '$t_gender',
                                living_city_name = '$t_livingCity'
                                $photoUpdate
                              WHERE id = $targetId";
        mysqli_query($db, $sqlFallbackUpdate);
    }

    echo json_encode([
        "status"           => true,
        "id"               => $targetId,
        "expert_id"        => $targetId,
        "recovered"        => true,
        "is_existing"      => true,
        "1st_layout"       => 1,
        "2nd_layout"       => $l2,
        "3rd_layout"       => $l3,
        "4th_layout"       => $l4,
        "5th_layout"       => $l5,
        "verification"     => $ver,
        "living_city_name" => $t_livingCity,
        "message"          => "Incomplete registration draft recovered successfully"
    ]);
    mysqli_close($db);
    exit();
}

// Case 3: Fresh new therapist: INSERT into therapists table
$spacePosition = strpos($t_Name, ' ');
$namePart = ($spacePosition !== false) ? substr($t_Name, 0, $spacePosition) : $t_Name;
$referralCode = strtolower(substr($namePart, 0, 1)) . substr($namePart, 1) . strtoupper(bin2hex(random_bytes(2)));
$created_at = date('Y-m-d H:i:s');

$sql = "INSERT INTO therapists (
            name, phone, email, aadhar_card_no, address, date_of_birth, gender, 
            living_city_name, profile_photo, created_at, verification, referral_code, referral_payment, 
            off_day, yearly_off, illness,
            `1st_layout`, `2nd_layout`, `3rd_layout`, `4th_layout`, `5th_layout`
        ) VALUES (
            '$t_Name', '$t_Mobile', '$t_Email', '$t_aadhaar', '$t_Address', '$t_Age', '$t_gender', 
            '$t_livingCity', '$filenamePhoto', '$created_at', 'UNVERIFIED', '$referralCode', '0', 
            '1', '18', '10',
            1, 0, 0, 0, 0
        )";

if (!mysqli_query($db, $sql)) {
    // Fallback in case layout columns are not yet present in table schema
    $sqlFallback = "INSERT INTO therapists (
                name, phone, email, aadhar_card_no, address, date_of_birth, gender, 
                living_city_name, profile_photo, created_at, verification, referral_code, referral_payment, 
                off_day, yearly_off, illness
            ) VALUES (
                '$t_Name', '$t_Mobile', '$t_Email', '$t_aadhaar', '$t_Address', '$t_Age', '$t_gender', 
                '$t_livingCity', '$filenamePhoto', '$created_at', 'UNVERIFIED', '$referralCode', '0', 
                '1', '18', '10'
            )";
    if (!mysqli_query($db, $sqlFallback)) {
        http_response_code(500);
        echo json_encode(["status" => false, "message" => "Error inserting record: " . mysqli_error($db)]);
        exit();
    }
}

$newId = mysqli_insert_id($db);
echo json_encode([
    "status"           => true, 
    "id"               => $newId, 
    "expert_id"        => $newId, 
    "1st_layout"       => 1,
    "2nd_layout"       => 0,
    "3rd_layout"       => 0,
    "4th_layout"       => 0,
    "5th_layout"       => 0,
    "verification"     => 'UNVERIFIED',
    "is_existing"      => false,
    "recovered"        => false,
    "living_city_name" => $t_livingCity,
    "message"          => "Personal details registered successfully"
]);

mysqli_close($db);
?>
