<?php
require "../connection.php";

header('Content-Type: text/plain; charset=utf-8');

$db = isset($con) ? $con : (isset($conn) ? $conn : null);

if (!$db) {
    echo "Database connection failed";
    exit();
}

// 1. Basic Personal Details
$t_Name     = isset($_POST['name']) ? mysqli_real_escape_string($db, trim($_POST['name'])) : '';
$t_Mobile   = isset($_POST['phone']) ? mysqli_real_escape_string($db, trim($_POST['phone'])) : '';
$t_Email    = isset($_POST['email']) ? mysqli_real_escape_string($db, trim($_POST['email'])) : '';
$t_aadhaar  = isset($_POST['aadhar_card_no']) ? mysqli_real_escape_string($db, preg_replace('/\s+/', '', trim($_POST['aadhar_card_no']))) : '';
$t_Address  = isset($_POST['address']) ? mysqli_real_escape_string($db, trim($_POST['address'])) : '';
$t_Age      = isset($_POST['date_of_birth']) ? mysqli_real_escape_string($db, trim($_POST['date_of_birth'])) : '';
$t_gender   = isset($_POST['gender']) ? mysqli_real_escape_string($db, trim($_POST['gender'])) : '';
$t_Photo    = isset($_POST["profile_photo"]) ? $_POST["profile_photo"] : '';
$Refferal   = isset($_POST["bounsReferral"]) ? mysqli_real_escape_string($db, trim($_POST["bounsReferral"])) : '';
$rawCity    = isset($_POST["city_id"]) ? trim($_POST["city_id"]) : '';

// 2. Professional Details (categories stores IDs only, e.g. "1,2", like area_ids)
$category_ids = isset($_POST['category_id']) && trim($_POST['category_id']) !== ''
    ? mysqli_real_escape_string($db, trim($_POST['category_id']))
    : (isset($_POST['categories']) && trim($_POST['categories']) !== '' 
        ? mysqli_real_escape_string($db, trim($_POST['categories'])) 
        : (isset($_POST['expert_category']) ? mysqli_real_escape_string($db, trim($_POST['expert_category'])) : ''));
$category_id       = $category_ids;
$expert_category   = $category_ids;
$selected_services = isset($_POST['selected_services']) ? mysqli_real_escape_string($db, trim($_POST['selected_services'])) : '';
$experience        = isset($_POST['experience']) ? mysqli_real_escape_string($db, trim($_POST['experience'])) : '';
$experience_desc   = isset($_POST['experience_desc']) ? mysqli_real_escape_string($db, trim($_POST['experience_desc'])) : '';
$area_ids          = isset($_POST['area_ids']) ? mysqli_real_escape_string($db, trim($_POST['area_ids'])) : '';

// 3. Mandatory Verification Documents
$resume_file       = isset($_POST['resume_file']) ? $_POST['resume_file'] : '';
$aadhaar_front     = isset($_POST['aadhaar_front']) ? $_POST['aadhaar_front'] : (isset($_POST['adhaar_picture']) ? $_POST['adhaar_picture'] : '');
$aadhaar_back      = isset($_POST['aadhaar_back']) ? $_POST['aadhaar_back'] : '';

// 4. Separate Optional Qualification Certificates
$madhyamik_cert    = isset($_POST['madhyamik_certificate']) ? $_POST['madhyamik_certificate'] : '';
$hs_cert           = isset($_POST['hs_certificate']) ? $_POST['hs_certificate'] : '';
$grad_cert         = isset($_POST['graduation_certificate']) ? $_POST['graduation_certificate'] : '';

// 5. City Mapping / Resolution
$cityMap = [
    'Delhi'      => 1,
    'New Delhi'  => 2,
    'Kolkata'    => 3,
    'Bengaluru'  => 4,
    'Hyderabad'  => 5,
    'Chennai'    => 6,
    'Pune'       => 7,
    'Ahmedabad'  => 8,
    'Gurgaon'    => 9,
    'Noida'      => 10
];

$CityId = null;
if (is_numeric($rawCity) && intval($rawCity) > 0) {
    $CityId = intval($rawCity);
} elseif (array_key_exists($rawCity, $cityMap)) {
    $CityId = $cityMap[$rawCity];
} else {
    $CityId = 1;
}

// 6. Validate Referral Code if provided
if ($Refferal !== "") {
    $checkQuery = "SELECT id FROM therapists WHERE referral_code = '$Refferal' LIMIT 1";
    $result = mysqli_query($db, $checkQuery);

    if (mysqli_num_rows($result) === 0) {
        echo "The code is not valid";
        exit;
    }
}

// 7. Check if Aadhaar number already exists
if ($t_aadhaar !== '') {
    $checkAadhaar = "SELECT id FROM therapists WHERE REPLACE(aadhar_card_no, ' ', '') = '$t_aadhaar' LIMIT 1";
    $aadhaarQuery = mysqli_query($db, $checkAadhaar);
    if ($aadhaarQuery && mysqli_num_rows($aadhaarQuery) > 0) {
        echo "Aadhaar number already exists";
        exit;
    }
}

// 8. Check if mobile number already exists
if ($t_Mobile !== '') {
    $checkMobile = "SELECT id FROM therapists WHERE phone = '$t_Mobile' LIMIT 1";
    $mobileQuery = mysqli_query($db, $checkMobile);
    if ($mobileQuery && mysqli_num_rows($mobileQuery) > 0) {
        echo "Mobile number already exists";
        exit;
    }
}

// 9. Generate Referral Code
$spacePosition = strpos($t_Name, ' ');
$namePart = ($spacePosition !== false) ? substr($t_Name, 0, $spacePosition) : $t_Name;
$referralCode = strtolower(substr($namePart, 0, 1)) . substr($namePart, 1) . strtoupper(bin2hex(random_bytes(2)));

// 10. Helper function to decode Base64 and save into designated folder
function saveDocFile($data, $folder, $prefix = 'DOC') {
    if (empty($data)) {
        return '';
    }
    // If it's already a saved filename rather than a Base64 payload
    if (strlen($data) < 255 && !preg_match('/^[a-zA-Z0-9\/+]+={0,2}$/', $data)) {
        return $data;
    }
    // Automatically create folder if it does not exist
    if (!is_dir($folder)) {
        mkdir($folder, 0777, true);
    }
    
    $ext = '.jpg';
    if (strpos($data, 'application/pdf') !== false) {
        $ext = '.pdf';
    } elseif (strpos($data, 'image/png') !== false) {
        $ext = '.png';
    } elseif (strpos($data, 'application/msword') !== false) {
        $ext = '.doc';
    } elseif (strpos($data, 'wordprocessingml') !== false) {
        $ext = '.docx';
    }

    $clean = preg_replace('/^data:[a-zA-Z0-9\/+-]+;base64,/', '', $data);
    $decoded = base64_decode($clean);
    if ($decoded === false) {
        return $data;
    }

    $filename = $prefix . "_" . time() . "_" . rand(1000, 9999) . $ext;
    file_put_contents($folder . '/' . $filename, $decoded);
    return $filename;
}

// 11. Decode and save documents into their designated folders
$filenamePhoto        = saveDocFile($t_Photo, 'PassportSize', 'PHOTO');
$filenameResume       = saveDocFile($resume_file, 'Resume', 'RESUME');
$filenameAadhaarFront = saveDocFile($aadhaar_front, 'IdProof', 'AADHAAR_FRONT');
$filenameAadhaarBack  = saveDocFile($aadhaar_back, 'aadharback', 'AADHAAR_BACK');
$filenameMadhyamik    = saveDocFile($madhyamik_cert, 'Madhyamik', '10TH');
$filenameHs           = saveDocFile($hs_cert, 'HigherSecondary', '12TH');
$filenameGrad         = saveDocFile($grad_cert, 'Graduation', 'GRAD');

$created_at = date('Y-m-d H:i:s');

// 12. Insert new therapist record with separate certificate columns
$sql = "INSERT INTO therapists (
            name, phone, email, aadhar_card_no, address, date_of_birth, gender, 
            profile_photo, categories, selected_services, experience, experience_desc, 
            area_ids, madhyamik_certificate, hs_certificate, graduation_certificate, 
            resume_file, adhaar_picture, aadhaar_back, created_at, verification, 
            referral_code, referral_payment, off_day, yearly_off, illness, bounsReferral, city_id
        ) VALUES (
            '$t_Name', 
            '$t_Mobile', 
            '$t_Email', 
            '$t_aadhaar', 
            '$t_Address', 
            '$t_Age', 
            '$t_gender', 
            '$filenamePhoto', 
            '$expert_category', 
            '$selected_services', 
            '$experience', 
            '$experience_desc', 
            '$area_ids', 
            '$filenameMadhyamik', 
            '$filenameHs', 
            '$filenameGrad', 
            '$filenameResume', 
            '$filenameAadhaarFront', 
            '$filenameAadhaarBack', 
            '$created_at', 
            'UNVERIFIED', 
            '$referralCode', 
            '0', 
            '1', 
            '18', 
            '10', 
            '$Refferal', 
            '$CityId'
        )";

if (mysqli_query($db, $sql) === false) {
    // If adhaar_picture failed, try fallback with aadhaar_front / expert_category
    $sqlFallback = str_replace(
        ['adhaar_picture', 'categories'], 
        ['aadhaar_front', 'expert_category'], 
        $sql
    );
    if (mysqli_query($db, $sqlFallback) === false) {
        echo "Error: " . mysqli_error($db);
    } else {
        echo "Successfully added user";
    }
} else {
    echo "Successfully added user";
}

mysqli_close($db);
?>
