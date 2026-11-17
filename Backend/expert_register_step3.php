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

$rawCity  = isset($_POST["city_id"]) ? trim($_POST["city_id"]) : '';
$area_ids = isset($_POST['area_ids']) ? mysqli_real_escape_string($db, trim($_POST['area_ids'])) : '';

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

$sql = "UPDATE therapists SET 
            city_id = '$CityId',
            area_ids = '$area_ids',
            `3rd_layout` = 1
        WHERE id = $id";

if (!mysqli_query($db, $sql)) {
    // Fallback if 3rd_layout column not yet added
    $sqlFallback = "UPDATE therapists SET 
                city_id = '$CityId',
                area_ids = '$area_ids'
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
    "3rd_layout" => 1,
    "message"    => "Service locations updated successfully"
]);

mysqli_close($db);
?>

