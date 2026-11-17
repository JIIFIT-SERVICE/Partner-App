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

// 1. Read input category_id and category_name from GET, POST, or JSON body
$rawInput = file_get_contents('php://input');
$jsonData = !empty($rawInput) ? json_decode($rawInput, true) : [];

$categoryId   = isset($_POST['category_id']) ? trim($_POST['category_id']) : (isset($_GET['category_id']) ? trim($_GET['category_id']) : ($jsonData['category_id'] ?? ''));
$categoryName = isset($_POST['category_name']) ? trim($_POST['category_name']) : (isset($_GET['category_name']) ? trim($_GET['category_name']) : ($jsonData['category_name'] ?? ''));

$safeCatId   = mysqli_real_escape_string($db, $categoryId);
$safeCatName = mysqli_real_escape_string($db, $categoryName);

// 2. Fallback service catalog by Category ID or Name
$defaultCatalog = [
    '1' => [ // Massage Therapist
        "Deep Tissue Massage",
        "Swedish Relaxation Massage",
        "Aromatherapy Massage",
        "Balinese Body Therapy",
        "Hot Stone Therapy",
        "Foot Reflexology",
        "Thai Traditional Massage",
        "Head, Neck & Shoulder Relief"
    ],
    '2' => [ // Ayurveda Massage Therapist
        "Abhyanga Full Body Massage",
        "Shirodhara Therapy",
        "Potli Herbal Massage",
        "Panchakarma Therapy",
        "Kadi Vasti (Spine Care)",
        "Udwarthanam (Herbal Scrub)",
        "Nasya Therapy",
        "Janu Vasti (Knee Care)"
    ],
    '3' => [ // Personal Trainer
        "Strength & Muscle Building",
        "Weight Loss & Fat Burn",
        "Functional & HIIT Training",
        "Core & Mobility Conditioning",
        "Endurance & Cardio Training",
        "Posture & Flexibility Training"
    ],
    '4' => [ // Yoga Teacher
        "Hatha Yoga",
        "Vinyasa Flow Yoga",
        "Pranayama & Meditation",
        "Ashtanga Yoga",
        "Pre & Post Natal Yoga",
        "Therapeutic & Restorative Yoga"
    ],
    '5' => [ // Physiotherapist
        "Orthopedic Rehabilitation",
        "Sports Injury Therapy",
        "Back & Neck Pain Relief",
        "Neuro Rehabilitation",
        "Post-Surgery Physical Therapy",
        "Joint Mobilization Therapy"
    ],
    '6' => [ // Zumba Dance Teacher
        "Zumba Classic Cardio",
        "Aqua Zumba",
        "Zumba Toning (Light Weights)",
        "Zumba Kids & Family",
        "Aerobic Dance Fitness",
        "Latin Rhythm Choreography"
    ],
    '7' => [ // Chiropractor
        "Spinal Alignment & Adjustment",
        "Cervical & Lumbar Decompression",
        "Posture Correction Therapy",
        "Sciatica Nerve Relief",
        "Joint Mobilization",
        "Musculoskeletal Pain Therapy"
    ],
    '8' => [ // Nutritionist
        "Weight Management Diet Plan",
        "Clinical Nutrition Consultation",
        "Sports & Athlete Nutrition",
        "Diabetes & Lifestyle Diet",
        "PCOS / Hormonal Diet Plan",
        "Detox & Gut Health Plan"
    ]
];

// 3. Conditional table routing based on Category ID or Name
$norm = strtolower(trim($categoryName));
$tableName = "";
$sql = "";

if ($categoryId === '1' || strpos($norm, 'massage') !== false) {
    // Table for Massage Therapist: User specified home_massage (find id, name)
    $tableName = "home_massage";
    $sql = "SELECT `id`, `name` FROM `home_massage`";
} elseif ($categoryId === '2' || strpos($norm, 'ayurveda') !== false || strpos($norm, 'ayurved') !== false) {
    // Table for Ayurveda Therapist: User specified ayurved_massages (find id, name)
    $tableName = "ayurved_massages";
    $sql = "SELECT `id`, `name` FROM `ayurved_massages`";
} elseif ($categoryId === '3' || strpos($norm, 'trainer') !== false) {
    // Table for Personal Trainer
    $tableName = "trainer_services";
    $sql = "SELECT * FROM `trainer_services` WHERE `status` = 1 ORDER BY `id` ASC";
} elseif ($categoryId === '4' || strpos($norm, 'yoga') !== false) {
    // Table for Yoga Instructor
    $tableName = "yoga_services";
    $sql = "SELECT * FROM `yoga_services` WHERE `status` = 1 ORDER BY `id` ASC";
} elseif ($categoryId === '5' || strpos($norm, 'zumba') !== false) {
    // Table for Zumba Dancer
    $tableName = "zumba_services";
    $sql = "SELECT * FROM `zumba_services` WHERE `status` = 1 ORDER BY `id` ASC";
} elseif ($categoryId === '6' || strpos($norm, 'physio') !== false) {
    // Table for Physiotherapist
    $tableName = "physiotherapy_services";
    $sql = "SELECT * FROM `physiotherapy_services` WHERE `status` = 1 ORDER BY `id` ASC";
} elseif ($categoryId === '7' || strpos($norm, 'chiro') !== false) {
    // Table for Chiropractor
    $tableName = "chiropractor_services";
    $sql = "SELECT * FROM `chiropractor_services` WHERE `status` = 1 ORDER BY `id` ASC";
} elseif ($categoryId === '8' || strpos($norm, 'nutri') !== false) {
    // Table for Nutritionist
    $tableName = "nutrition_services";
    $sql = "SELECT * FROM `nutrition_services` WHERE `status` = 1 ORDER BY `id` ASC";
} else {
    // Default fallback to general table
    $tableName = "category_services";
    $sql = "SELECT * FROM `category_services` WHERE `category_id` = '$safeCatId' AND `status` = 1 ORDER BY `id` ASC";
}

$services = [];
$serviceItems = [];
$seenNames = [];

// Try executing the conditional query
if (!empty($tableName)) {
    // 1. Try with status = 1
    $result = @mysqli_query($db, "SELECT * FROM `$tableName` WHERE `status` = 1 ORDER BY `id` ASC");
    // 2. If status column doesn't exist, try stasus = 1 (matching therapists_category naming)
    if (!$result || mysqli_num_rows($result) === 0) {
        $result = @mysqli_query($db, "SELECT * FROM `$tableName` WHERE `stasus` = 1 ORDER BY `id` ASC");
    }
    // 3. If that still produces nothing or table lacks status column, fetch all rows
    if (!$result || mysqli_num_rows($result) === 0) {
        $result = @mysqli_query($db, "SELECT * FROM `$tableName` ORDER BY `id` ASC");
    }
    // 4. In case table is a shared category_services table filtering by category_id
    if ((!$result || mysqli_num_rows($result) === 0) && !empty($safeCatId)) {
        $result = @mysqli_query($db, "SELECT * FROM `category_services` WHERE `category_id` = '$safeCatId' ORDER BY `id` ASC");
    }

    if ($result && mysqli_num_rows($result) > 0) {
        while ($row = mysqli_fetch_assoc($result)) {
            // Flexible column detection: name, service_name, title, service
            $sName = trim($row['name'] ?? ($row['service_name'] ?? ($row['title'] ?? ($row['service'] ?? ''))));
            $normalized = strtolower(preg_replace('/\s+/', ' ', $sName));
            if (!empty($sName) && !isset($seenNames[$normalized])) {
                $seenNames[$normalized] = true;
                $services[] = $sName;
                $serviceItems[] = [
                    "id"   => (string)($row['id'] ?? count($services)),
                    "name" => $sName
                ];
            }
        }
    }
}

// If no dynamic database records found, fall back to default catalog
if (empty($services)) {
    $fallbackList = $defaultCatalog[$categoryId] ?? null;
    if (!$fallbackList && !empty($norm)) {
        if (strpos($norm, 'massage') !== false) {
            $fallbackList = $defaultCatalog['1'];
        } elseif (strpos($norm, 'ayurveda') !== false) {
            $fallbackList = $defaultCatalog['2'];
        } elseif (strpos($norm, 'trainer') !== false) {
            $fallbackList = $defaultCatalog['3'];
        } elseif (strpos($norm, 'yoga') !== false) {
            $fallbackList = $defaultCatalog['4'];
        } elseif (strpos($norm, 'zumba') !== false) {
            $fallbackList = $defaultCatalog['5'];
        } elseif (strpos($norm, 'physio') !== false) {
            $fallbackList = $defaultCatalog['6'];
        } elseif (strpos($norm, 'chiro') !== false) {
            $fallbackList = $defaultCatalog['7'];
        } elseif (strpos($norm, 'nutri') !== false) {
            $fallbackList = $defaultCatalog['8'];
        }
    }
    if (!$fallbackList) {
        $fallbackList = $defaultCatalog['1'];
    }

    $services = $fallbackList;
    foreach ($services as $idx => $s) {
        $serviceItems[] = [
            "id"   => (string)($idx + 1),
            "name" => $s
        ];
    }
}

echo json_encode([
    "status"        => true,
    "success"       => true,
    "category_id"   => $categoryId,
    "category_name" => $categoryName,
    "table_used"    => $tableName,
    "count"         => count($services),
    "services"      => $services,
    "service_items" => $serviceItems
], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

mysqli_close($db);
?>

