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

// User specified table: home_massage (find id, name)
// GROUP BY name to prevent double/duplicate names from SQL level
$result = @mysqli_query($db, "SELECT MIN(`id`) AS `id`, `name` FROM `home_massage` WHERE `status` = 1 GROUP BY `name` ORDER BY MIN(`id`) ASC");
if (!$result || mysqli_num_rows($result) === 0) {
    $result = @mysqli_query($db, "SELECT MIN(`id`) AS `id`, `name` FROM `home_massage` WHERE `stasus` = 1 GROUP BY `name` ORDER BY MIN(`id`) ASC");
}
if (!$result || mysqli_num_rows($result) === 0) {
    $result = @mysqli_query($db, "SELECT MIN(`id`) AS `id`, `name` FROM `home_massage` GROUP BY `name` ORDER BY MIN(`id`) ASC");
}
if (!$result || mysqli_num_rows($result) === 0) {
    $result = @mysqli_query($db, "SELECT `id`, `name` FROM `home_massage` ORDER BY `id` ASC");
}

$services = [];
$homeMassageList = [];
$seenNames = [];

if ($result && mysqli_num_rows($result) > 0) {
    while ($row = mysqli_fetch_assoc($result)) {
        $sName = trim($row['name'] ?? ($row['service_name'] ?? ($row['title'] ?? '')));
        $normalized = strtolower(preg_replace('/\s+/', ' ', $sName));
        
        // If name is double/duplicate, keep only single (unique)
        if (!empty($sName) && !isset($seenNames[$normalized])) {
            $seenNames[$normalized] = true;
            $services[] = $sName;
            $homeMassageList[] = [
                "id"   => (string)$row['id'],
                "name" => $sName
            ];
        }
    }
}

// Fallback in case table is empty during setup
if (empty($services)) {
    $services = [
        "Deep Tissue Massage",
        "Swedish Relaxation Massage",
        "Aromatherapy Massage",
        "Balinese Body Therapy",
        "Hot Stone Therapy",
        "Foot Reflexology",
        "Thai Traditional Massage",
        "Head, Neck & Shoulder Relief"
    ];
    foreach ($services as $idx => $s) {
        $homeMassageList[] = [
            "id"   => (string)($idx + 1),
            "name" => $s
        ];
    }
}

echo json_encode([
    "status"       => true,
    "success"      => true,
    "category"     => "Massage Therapist",
    "table"        => "home_massage",
    "count"        => count($services),
    "services"     => $services,
    "home_massage" => $homeMassageList,
    "data"         => $homeMassageList
], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

mysqli_close($db);
?>
