<?php
error_reporting(0);
ini_set('display_errors', '0');

if (function_exists('mysqli_report')) {
    @mysqli_report(MYSQLI_REPORT_OFF);
}

header('Content-Type: application/json; charset=utf-8');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    echo json_encode(["status" => true]);
    exit();
}

try {
    require_once "../connection.php";
} catch (Throwable $e) {
    echo json_encode([
        "status"  => false,
        "message" => "Database connection error on server: " . $e->getMessage()
    ]);
    exit();
}

if (function_exists('mysqli_report')) {
    @mysqli_report(MYSQLI_REPORT_OFF);
}

$db = isset($con) ? $con : (isset($conn) ? $conn : null);

if (!$db || !($db instanceof mysqli)) {
    echo json_encode([
        "status"  => false, 
        "message" => "Database connection failed"
    ]);
    exit();
}

try {
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

    $rawAadhaar = isset($_POST['aadhar_card_no']) ? trim($_POST['aadhar_card_no']) : (isset($_GET['aadhar_card_no']) ? trim($_GET['aadhar_card_no']) : (isset($_POST['aadhaar']) ? trim($_POST['aadhaar']) : ''));
    $cleanAadhaar = preg_replace('/\s+/', '', $rawAadhaar);

    if ($id > 0) {
        $sql = "SELECT * FROM therapists WHERE id = $id LIMIT 1";
    } elseif (strlen($last10) >= 10 && strlen($cleanAadhaar) >= 12) {
        $sql = "SELECT * FROM therapists WHERE RIGHT(REPLACE(REPLACE(phone, '+', ''), ' ', ''), 10) = '$last10' AND REPLACE(aadhar_card_no, ' ', '') = '$cleanAadhaar' LIMIT 1";
    } elseif (strlen($last10) >= 10) {
        $sql = "SELECT * FROM therapists WHERE RIGHT(REPLACE(REPLACE(phone, '+', ''), ' ', ''), 10) = '$last10' LIMIT 1";
    } else {
        echo json_encode(["status" => false, "message" => "Valid therapist ID or phone number is required"]);
        exit();
    }

    $res = @mysqli_query($db, $sql);

    if (!$res || mysqli_num_rows($res) === 0) {
        echo json_encode(["status" => false, "message" => "Therapist not found"]);
        exit();
    }

    $row = mysqli_fetch_assoc($res);

    $verification = (isset($row['verification']) && trim($row['verification']) !== '') ? trim($row['verification']) : 'UNVERIFIED';

    $kitPayment = (isset($row['kit_payment']) && $row['kit_payment'] !== null) ? trim($row['kit_payment']) : '';

    $hasLayoutCols = array_key_exists('1st_layout', $row);

    if ($hasLayoutCols) {
        $firstLayout  = isset($row['1st_layout']) ? intval($row['1st_layout']) : 0;
        $secondLayout = isset($row['2nd_layout']) ? intval($row['2nd_layout']) : 0;
        $thirdLayout  = isset($row['3rd_layout']) ? intval($row['3rd_layout']) : 0;
        $fourthLayout = isset($row['4th_layout']) ? intval($row['4th_layout']) : 0;
        $fifthLayout  = isset($row['5th_layout']) ? intval($row['5th_layout']) : 0;
    } else {
        // Fallback inference ONLY for legacy tables without layout columns
        $firstLayout  = (!empty($row['name']) && !empty($row['phone'])) ? 1 : 0;
        $catVal       = isset($row['categories']) ? trim($row['categories']) : '';
        $expCatVal    = isset($row['expert_category']) ? trim($row['expert_category']) : '';
        $secondLayout = (($catVal !== '' && $catVal !== '0') || ($expCatVal !== '' && $expCatVal !== '0')) ? 1 : 0;
        $thirdLayout  = (!empty($row['city_id']) && !empty($row['area_ids'])) ? 1 : 0;
        $fourthLayout = (!empty($row['resume_file']) || !empty($row['adhaar_picture']) || !empty($row['aadhaar_front'])) ? 1 : 0;
        $fifthLayout  = ($fourthLayout === 1 && (isset($row['bounsReferral']) || $verification === 'VERIFIED')) ? 1 : 0;
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

    // Resolve City Name
    $cityName = '';
    $rawCity = isset($row['city_id']) ? trim($row['city_id']) : '';
    $cityMapReverse = [
        1 => 'Delhi', 2 => 'New Delhi', 3 => 'Kolkata', 4 => 'Bengaluru',
        5 => 'Hyderabad', 6 => 'Chennai', 7 => 'Pune', 8 => 'Ahmedabad',
        9 => 'Gurgaon', 10 => 'Noida'
    ];

    if (!empty($rawCity)) {
        if (is_numeric($rawCity)) {
            $cInt = intval($rawCity);
            try {
                $cRes = @mysqli_query($db, "SELECT name FROM city WHERE id = $cInt LIMIT 1");
                if ($cRes && $cRow = mysqli_fetch_assoc($cRes)) {
                    $cityName = $cRow['name'];
                }
            } catch (Throwable $ignore) {}
            if (empty($cityName)) {
                try {
                    $cRes2 = @mysqli_query($db, "SELECT city_name FROM city WHERE id = $cInt LIMIT 1");
                    if ($cRes2 && $cRow2 = mysqli_fetch_assoc($cRes2)) {
                        $cityName = $cRow2['city_name'];
                    }
                } catch (Throwable $ignore) {}
            }
            if (empty($cityName)) {
                try {
                    $cRes3 = @mysqli_query($db, "SELECT name FROM cities WHERE id = $cInt LIMIT 1");
                    if ($cRes3 && $cRow3 = mysqli_fetch_assoc($cRes3)) {
                        $cityName = $cRow3['name'];
                    }
                } catch (Throwable $ignore) {}
            }
            if (empty($cityName) && isset($cityMapReverse[$cInt])) {
                $cityName = $cityMapReverse[$cInt];
            }
        } else {
            $cityName = $rawCity;
        }
    }

    // Resolve Area Names from locality_covered table using area_ids
    $areaNames = [];
    $rawAreaIds = isset($row['area_ids']) ? trim($row['area_ids']) : '';

    if (!empty($rawAreaIds)) {
        $idTokens = [];
        if (strpos($rawAreaIds, '[') !== false && strpos($rawAreaIds, ']') !== false) {
            $decoded = @json_decode($rawAreaIds, true);
            if (is_array($decoded)) {
                $idTokens = $decoded;
            }
        }
        if (empty($idTokens)) {
            $cleanStr = str_replace(['[', ']', '"', "'", ' '], '', $rawAreaIds);
            $idTokens = explode(',', $cleanStr);
        }
        
        $cleanIds = [];
        foreach ($idTokens as $token) {
            $t = trim($token);
            if (is_numeric($t) && intval($t) > 0) {
                $cleanIds[] = intval($t);
            }
        }
        
        if (!empty($cleanIds)) {
            $inList = implode(',', array_unique($cleanIds));
            try {
                $locRes = @mysqli_query($db, "SELECT id, name FROM locality_covered WHERE id IN ($inList)");
                if ($locRes && mysqli_num_rows($locRes) > 0) {
                    while ($lRow = mysqli_fetch_assoc($locRes)) {
                        if (!empty($lRow['name'])) {
                            $areaNames[] = $lRow['name'];
                        }
                    }
                }
            } catch (Throwable $ignore) {}
            if (empty($areaNames)) {
                try {
                    $locRes2 = @mysqli_query($db, "SELECT id, locality_name FROM locality_covered WHERE id IN ($inList)");
                    if ($locRes2 && mysqli_num_rows($locRes2) > 0) {
                        while ($lRow2 = mysqli_fetch_assoc($locRes2)) {
                            if (!empty($lRow2['locality_name'])) {
                                $areaNames[] = $lRow2['locality_name'];
                            }
                        }
                    }
                } catch (Throwable $ignore) {}
            }
            if (empty($areaNames)) {
                try {
                    $locRes3 = @mysqli_query($db, "SELECT id, name FROM localities WHERE id IN ($inList)");
                    if ($locRes3 && mysqli_num_rows($locRes3) > 0) {
                        while ($lRow3 = mysqli_fetch_assoc($locRes3)) {
                            if (!empty($lRow3['name'])) {
                                $areaNames[] = $lRow3['name'];
                            }
                        }
                    }
                } catch (Throwable $ignore) {}
            }
        }
    }

    echo json_encode([
        "status"             => true,
        "id"                 => intval($row['id']),
        "name"               => isset($row['name']) ? $row['name'] : '',
        "phone"              => isset($row['phone']) ? $row['phone'] : '',
        "email"              => isset($row['email']) ? $row['email'] : '',
        "aadhar_card_no"     => isset($row['aadhar_card_no']) ? $row['aadhar_card_no'] : '',
        "gender"             => isset($row['gender']) ? $row['gender'] : '',
        "date_of_birth"      => isset($row['date_of_birth']) ? $row['date_of_birth'] : '',
        "address"            => isset($row['address']) ? $row['address'] : '',
        "living_city_name"   => isset($row['living_city_name']) ? $row['living_city_name'] : '',
        "city_id"            => isset($row['city_id']) ? $row['city_id'] : '',
        "city_name"          => $cityName,
        "area_ids"           => $rawAreaIds,
        "areas"              => $areaNames,
        "created_at"         => isset($row['created_at']) ? $row['created_at'] : '',
        "verification"       => $verification,
        "experience"         => isset($row['experience']) ? $row['experience'] : '',
        "experience_desc"    => isset($row['experience_desc']) ? $row['experience_desc'] : '',
        "selected_services"  => isset($row['selected_services']) ? $row['selected_services'] : '',
        "categories"         => isset($row['categories']) ? $row['categories'] : (isset($row['expert_category']) ? $row['expert_category'] : ''),
        "1st_layout"         => $firstLayout,
        "2nd_layout"         => $secondLayout,
        "3rd_layout"         => $thirdLayout,
        "4th_layout"         => $fourthLayout,
        "5th_layout"         => $fifthLayout,
        "kit_payment"        => $kitPayment,
        "payment_amount"     => isset($row['payment_amount']) ? $row['payment_amount'] : '',
        "transaction_id"     => isset($row['transaction_id']) ? $row['transaction_id'] : '',
        "payment_date"       => isset($row['payment_date']) ? $row['payment_date'] : '',
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

    @mysqli_close($db);

} catch (Throwable $t) {
    echo json_encode([
        "status"  => false,
        "message" => "Error processing status: " . $t->getMessage()
    ]);
}
?>