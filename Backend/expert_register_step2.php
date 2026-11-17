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

// User requirement: store categories as IDs only, not in text, like area_ids (e.g. "1,2" or "1")
$category_ids = '';
if (isset($_POST['category_id']) && trim($_POST['category_id']) !== '') {
    $category_ids = mysqli_real_escape_string($db, trim($_POST['category_id']));
} elseif (isset($_POST['categories']) && trim($_POST['categories']) !== '') {
    $category_ids = mysqli_real_escape_string($db, trim($_POST['categories']));
} elseif (isset($_POST['expert_category']) && trim($_POST['expert_category']) !== '') {
    $category_ids = mysqli_real_escape_string($db, trim($_POST['expert_category']));
}

$selected_services = isset($_POST['selected_services']) ? mysqli_real_escape_string($db, trim($_POST['selected_services'])) : '';
$experience        = isset($_POST['experience']) ? mysqli_real_escape_string($db, trim($_POST['experience'])) : '';
$experience_desc   = isset($_POST['experience_desc']) ? mysqli_real_escape_string($db, trim($_POST['experience_desc'])) : '';

$sql = "UPDATE therapists SET 
            categories = '$category_ids',
            selected_services = '$selected_services',
            experience = '$experience',
            experience_desc = '$experience_desc',
            `2nd_layout` = 1
        WHERE id = $id";

if (mysqli_query($db, $sql) === false) {
    // Fallback if column is named expert_category
    $sqlFallback = "UPDATE therapists SET 
                        expert_category = '$category_ids',
                        selected_services = '$selected_services',
                        experience = '$experience',
                        experience_desc = '$experience_desc',
                        `2nd_layout` = 1
                    WHERE id = $id";
    if (mysqli_query($db, $sqlFallback) === false) {
        // Fallback without layout column
        $sqlFallback2 = "UPDATE therapists SET 
                            categories = '$category_ids',
                            selected_services = '$selected_services',
                            experience = '$experience',
                            experience_desc = '$experience_desc'
                        WHERE id = $id";
        if (mysqli_query($db, $sqlFallback2) === false) {
            http_response_code(500);
            echo json_encode(["status" => false, "message" => "Database error: " . mysqli_error($db)]);
            exit();
        }
    }
}

echo json_encode([
    "status"       => true, 
    "id"           => $id, 
    "2nd_layout"   => 1,
    "category_ids" => $category_ids,
    "message"      => "Professional details updated successfully"
]);
mysqli_close($db);
?>
