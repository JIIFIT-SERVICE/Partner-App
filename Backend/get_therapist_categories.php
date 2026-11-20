<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require "../connection.php";

$db = isset($con) ? $con : (isset($conn) ? $conn : null);

if (!$db) {
    http_response_code(500);
    echo json_encode([
        "status"  => false,
        "success" => false,
        "message" => "Database connection failed"
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
    exit();
}

// User specified SQL Query
$query = "SELECT `id`, `name`, `stasus`, `created_at` FROM `therapists_category` WHERE `stasus` = 1 ORDER BY `id` ASC";
$result = mysqli_query($db, $query);

$categories = [];

if ($result && mysqli_num_rows($result) > 0) {
    while ($row = mysqli_fetch_assoc($result)) {
        $categories[] = [
            "id"         => (string)$row['id'],
            "name"       => (string)$row['name'],
            "stasus"     => (string)$row['stasus'],
            "created_at" => (string)$row['created_at']
        ];
    }
}

echo json_encode([
    "status"     => true,
    "success"    => true,
    "count"      => count($categories),
    "categories" => $categories
], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

mysqli_close($db);
?>

