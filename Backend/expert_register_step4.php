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

$id = isset($_POST['id']) ? intval($_POST['id']) : (isset($_POST['expert_id']) ? intval($_POST['expert_id']) : 0);
if ($id <= 0) {
    http_response_code(400);
    echo json_encode(["status" => false, "message" => "Valid therapist ID is required"]);
    exit();
}

$resume_file    = isset($_POST['resume_file']) ? $_POST['resume_file'] : '';
$aadhaar_front  = isset($_POST['aadhaar_front']) ? $_POST['aadhaar_front'] : (isset($_POST['adhaar_picture']) ? $_POST['adhaar_picture'] : '');
$aadhaar_back   = isset($_POST['aadhaar_back']) ? $_POST['aadhaar_back'] : '';
$madhyamik_cert = isset($_POST['madhyamik_certificate']) ? $_POST['madhyamik_certificate'] : '';
$hs_cert        = isset($_POST['hs_certificate']) ? $_POST['hs_certificate'] : '';
$grad_cert      = isset($_POST['graduation_certificate']) ? $_POST['graduation_certificate'] : '';

function saveDocFile($data, $folder, $prefix = 'DOC') {
    if (empty($data)) return '';
    if (strlen($data) < 255 && !preg_match('/^[a-zA-Z0-9\/+]+={0,2}$/', $data)) return $data;
    if (!is_dir($folder)) mkdir($folder, 0777, true);
    
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
    if ($decoded === false) return $data;

    $filename = $prefix . "_" . time() . "_" . rand(1000, 9999) . $ext;
    file_put_contents($folder . '/' . $filename, $decoded);
    return $filename;
}

$filenameResume       = saveDocFile($resume_file, 'Resume', 'RESUME');
$filenameAadhaarFront = saveDocFile($aadhaar_front, 'IdProof', 'AADHAAR_FRONT');
$filenameAadhaarBack  = saveDocFile($aadhaar_back, 'aadharback', 'AADHAAR_BACK');
$filenameMadhyamik    = saveDocFile($madhyamik_cert, 'Madhyamik', '10TH');
$filenameHs           = saveDocFile($hs_cert, 'HigherSecondary', '12TH');
$filenameGrad         = saveDocFile($grad_cert, 'Graduation', 'GRAD');

$sql = "UPDATE therapists SET 
            resume_file = '$filenameResume',
            adhaar_picture = '$filenameAadhaarFront',
            aadhaar_back = '$filenameAadhaarBack',
            madhyamik_certificate = '$filenameMadhyamik',
            hs_certificate = '$filenameHs',
            graduation_certificate = '$filenameGrad',
            `4th_layout` = 1
        WHERE id = $id";

if (mysqli_query($db, $sql) === false) {
    // Fallback if column is named aadhaar_front
    $sqlFallback = "UPDATE therapists SET 
                        resume_file = '$filenameResume',
                        aadhaar_front = '$filenameAadhaarFront',
                        aadhaar_back = '$filenameAadhaarBack',
                        madhyamik_certificate = '$filenameMadhyamik',
                        hs_certificate = '$filenameHs',
                        graduation_certificate = '$filenameGrad',
                        `4th_layout` = 1
                    WHERE id = $id";
    if (mysqli_query($db, $sqlFallback) === false) {
        $sqlFallback2 = "UPDATE therapists SET 
                            resume_file = '$filenameResume',
                            adhaar_picture = '$filenameAadhaarFront',
                            aadhaar_back = '$filenameAadhaarBack',
                            madhyamik_certificate = '$filenameMadhyamik',
                            hs_certificate = '$filenameHs',
                            graduation_certificate = '$filenameGrad'
                        WHERE id = $id";
        if (mysqli_query($db, $sqlFallback2) === false) {
            http_response_code(500);
            echo json_encode(["status" => false, "message" => "Database error: " . mysqli_error($db)]);
            exit();
        }
    }
}

echo json_encode([
    "status"     => true, 
    "id"         => $id, 
    "4th_layout" => 1,
    "message"    => "Documents uploaded successfully"
]);
mysqli_close($db);
?>

