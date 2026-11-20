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

$Refferal = isset($_POST["bounsReferral"]) ? mysqli_real_escape_string($db, trim($_POST["bounsReferral"])) : (isset($_POST["referral_code"]) ? mysqli_real_escape_string($db, trim($_POST["referral_code"])) : '');

// Validate Referral Code if provided
if ($Refferal !== "") {
    $checkQuery = "SELECT id FROM therapists WHERE referral_code = '$Refferal' LIMIT 1";
    $result = mysqli_query($db, $checkQuery);

    if (!$result || mysqli_num_rows($result) === 0) {
        http_response_code(400);
        echo json_encode(["status" => false, "message" => "The code is not valid"]);
        exit();
    }
}

$sql = "UPDATE therapists SET 
            bounsReferral = '$Refferal',
            `5th_layout` = 1
        WHERE id = $id";

if (!mysqli_query($db, $sql)) {
    $sqlFallback = "UPDATE therapists SET 
                bounsReferral = '$Refferal'
            WHERE id = $id";
    if (!mysqli_query($db, $sqlFallback)) {
        http_response_code(500);
        echo json_encode(["status" => false, "message" => "Database error: " . mysqli_error($db)]);
        exit();
    }
}

echo json_encode([
    "status"     => true, 
    "id"         => $id, 
    "5th_layout" => 1,
    "message"    => "Successfully added user"
]);

mysqli_close($db);
?>

