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

// User specified table: ayurved_massages (find id, name)
// GROUP BY name to prevent double/duplicate names from SQL level
$result = @mysqli_query($db, "SELECT MIN(`id`) AS `id`, `name` FROM `ayurved_massages` WHERE `status` = 1 GROUP BY `name` ORDER BY MIN(`id`) ASC");
if (!$result || mysqli_num_rows($result) === 0) {
    $result = @mysqli_query($db, "SELECT MIN(`id`) AS `id`, `name` FROM `ayurved_massages` WHERE `stasus` = 1 GROUP BY `name` ORDER BY MIN(`id`) ASC");
}
if (!$result || mysqli_num_rows($result) === 0) {
    $result = @mysqli_query($db, "SELECT MIN(`id`) AS `id`, `name` FROM `ayurved_massages` GROUP BY `name` ORDER BY MIN(`id`) ASC");
}
if (!$result || mysqli_num_rows($result) === 0) {
    $result = @mysqli_query($db, "SELECT `id`, `name` FROM `ayurved_massages` ORDER BY `id` ASC");
}

$services = [];
$ayurvedList = [];
$seenNames = [];

if ($result && mysqli_num_rows($result) > 0) {
    while ($row = mysqli_fetch_assoc($result)) {
        $sName = trim($row['name'] ?? ($row['service_name'] ?? ($row['title'] ?? '')));
        $normalized = strtolower(preg_replace('/\s+/', ' ', $sName));
        
        // If name is double/duplicate, keep only single (unique)
        if (!empty($sName) && !isset($seenNames[$normalized])) {
            $seenNames[$normalized] = true;
            $services[] = $sName;
            $ayurvedList[] = [
                "id"   => (string)$row['id'],
                "name" => $sName
            ];
        }
    }
}

// Fallback in case table is empty during setup
if (empty($services)) {
    $services = [
        "Abhyanga Full Body Massage",
        "Shirodhara Therapy",
        "Potli Herbal Massage",
        "Panchakarma Therapy",
        "Kadi Vasti (Spine Care)",
        "Udwarthanam (Herbal Scrub)",
        "Nasya Therapy",
        "Janu Vasti (Knee Care)"
    ];
    foreach ($services as $idx => $s) {
        $ayurvedList[] = [
            "id"   => (string)($idx + 1),
            "name" => $s
        ];
    }
}

echo json_encode([
    "status"           => true,
    "success"          => true,
    "category"         => "Ayurveda Therapist",
    "table"            => "ayurved_massages",
    "count"            => count($services),
    "services"         => $services,
    "ayurved_massages" => $ayurvedList,
    "data"             => $ayurvedList
], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

mysqli_close($db);
?>

