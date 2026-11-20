<?php
error_reporting(0);
ini_set('display_errors', '0');

header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Disable automatic PHP 8.1+ mysqli exceptions so queries don't crash with 0-byte blank responses
if (function_exists('mysqli_report')) {
    @mysqli_report(MYSQLI_REPORT_OFF);
}

try {
    require_once "../connection.php";

    // Disable mysqli exceptions again in case connection.php enabled them
    if (function_exists('mysqli_report')) {
        @mysqli_report(MYSQLI_REPORT_OFF);
    }

    $db = isset($con) ? $con : (isset($conn) ? $conn : null);

    if (!$db || !($db instanceof mysqli)) {
        http_response_code(500);
        echo json_encode([
            "status"  => false,
            "success" => false,
            "message" => "Database connection failed"
        ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        exit();
    }

    // Read input parameters from POST, GET, or JSON body
    $rawInput = file_get_contents('php://input');
    $jsonData = !empty($rawInput) ? json_decode($rawInput, true) : [];

    $categoryId   = isset($_POST['category_id']) ? trim((string)$_POST['category_id']) : (isset($_GET['category_id']) ? trim((string)$_GET['category_id']) : (string)($jsonData['category_id'] ?? ''));
    $categoryName = isset($_POST['category_name']) ? trim((string)$_POST['category_name']) : (isset($_GET['category_name']) ? trim((string)$_GET['category_name']) : (string)($jsonData['category_name'] ?? ''));

    $safeCatId   = mysqli_real_escape_string($db, $categoryId);
    $safeCatName = mysqli_real_escape_string($db, $categoryName);
    $norm = mb_strtolower(trim($categoryName), 'UTF-8');

    // Strict flags: Massage and Ayurveda must NEVER use static fallbacks
    $isMassage = ($categoryId === '1' || $categoryId === '9' || (strpos($norm, 'massage') !== false && strpos($norm, 'ayurved') === false));
    $isAyurveda = ($categoryId === '2' || strpos($norm, 'ayurved') !== false);

    // Default static fallback catalog ONLY for non-massage / non-ayurveda categories (e.g. Trainer, Yoga, etc.)
    $defaultCatalog = [
        '3' => [ // Physiotherapist at home (ID 3 in therapists_category)
            "Orthopedic Rehabilitation",
            "Sports Injury Therapy",
            "Back & Neck Pain Relief",
            "Neuro Rehabilitation",
            "Post-Surgery Physical Therapy",
            "Joint Mobilization Therapy"
        ],
        '4' => [ // Personal trainer at home (ID 4 in therapists_category)
            "Strength & Muscle Building",
            "Weight Loss & Fat Burn",
            "Functional & HIIT Training",
            "Core & Mobility Conditioning",
            "Endurance & Cardio Training",
            "Posture & Flexibility Training"
        ],
        '5' => [ // Yoga at home (ID 5 in therapists_category)
            "Hatha Yoga",
            "Vinyasa Flow Yoga",
            "Pranayama & Meditation",
            "Ashtanga Yoga",
            "Pre & Post Natal Yoga",
            "Therapeutic & Restorative Yoga"
        ],
        '6' => [ // Zumba dance at home (ID 6 in therapists_category)
            "Zumba Classic Cardio",
            "Aqua Zumba",
            "Zumba Toning (Light Weights)",
            "Zumba Kids & Family",
            "Aerobic Dance Fitness",
            "Latin Rhythm Choreography"
        ],
        '7' => [ // Nutrionist at home (ID 7 in therapists_category)
            "Weight Management Diet Plan",
            "Clinical Nutrition Consultation",
            "Sports & Athlete Nutrition",
            "Diabetes & Lifestyle Diet",
            "PCOS / Hormonal Diet Plan",
            "Detox & Gut Health Plan"
        ],
        '8' => [ // Chiropractor at home (ID 8 in therapists_category)
            "Spinal Alignment & Adjustment",
            "Cervical & Lumbar Decompression",
            "Posture Correction Therapy",
            "Sciatica Nerve Relief",
            "Joint Mobilization",
            "Musculoskeletal Pain Therapy"
        ]
    ];

    $services = [];
    $serviceItems = [];
    $seenNames = [];
    $tableUsed = "";

    // 1. Disable ONLY_FULL_GROUP_BY for this session
    @mysqli_query($db, "SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");

    if ($isMassage) {
        // --- MASSAGE THERAPIST (home_massage) ---
        $tableUsed = "home_massage";

        // Primary Query: exact query requested by user
        $sql = "SELECT `id`, `name` FROM `home_massage` GROUP BY `name` ORDER BY MIN(`id`) ASC";
        $result = @mysqli_query($db, $sql);

        // Fallback 1: MIN(id) AS id with GROUP BY name
        if (!$result || mysqli_num_rows($result) === 0) {
            $result = @mysqli_query($db, "SELECT MIN(`id`) AS `id`, `name` FROM `home_massage` GROUP BY `name` ORDER BY MIN(`id`) ASC");
        }

        // Fallback 2: Select id, name ordered by id
        if (!$result || mysqli_num_rows($result) === 0) {
            $result = @mysqli_query($db, "SELECT `id`, `name` FROM `home_massage` ORDER BY `id` ASC");
        }

        // Fallback 3: Select * from home_massage
        if (!$result || mysqli_num_rows($result) === 0) {
            $result = @mysqli_query($db, "SELECT * FROM `home_massage` ORDER BY `id` ASC");
        }

        // Fallback 4: In case table is named home_massages (plural)
        if (!$result || mysqli_num_rows($result) === 0) {
            $checkPlural = @mysqli_query($db, "SELECT * FROM `home_massages` ORDER BY `id` ASC");
            if ($checkPlural && mysqli_num_rows($checkPlural) > 0) {
                $result = $checkPlural;
                $tableUsed = "home_massages";
            }
        }

        // Fallback 5: Check category_services where category_id = '1' or '9'
        if (!$result || mysqli_num_rows($result) === 0) {
            $catFilterId = (!empty($safeCatId) && in_array($safeCatId, ['1', '9'])) ? $safeCatId : '1';
            $checkCat = @mysqli_query($db, "SELECT * FROM `category_services` WHERE `category_id` = '$catFilterId' ORDER BY `id` ASC");
            if ($checkCat && mysqli_num_rows($checkCat) > 0) {
                $result = $checkCat;
                $tableUsed = "category_services";
            }
        }

    } elseif ($isAyurveda) {
        // --- AYURVEDA THERAPIST (ayurved_massages) ---
        $tableUsed = "ayurved_massages";

        // Primary Query
        $sql = "SELECT `id`, `name` FROM `ayurved_massages` GROUP BY `name` ORDER BY MIN(`id`) ASC";
        $result = @mysqli_query($db, $sql);

        // Fallback 1: MIN(id) AS id with GROUP BY name
        if (!$result || mysqli_num_rows($result) === 0) {
            $result = @mysqli_query($db, "SELECT MIN(`id`) AS `id`, `name` FROM `ayurved_massages` GROUP BY `name` ORDER BY MIN(`id`) ASC");
        }

        // Fallback 2: Select id, name ordered by id
        if (!$result || mysqli_num_rows($result) === 0) {
            $result = @mysqli_query($db, "SELECT `id`, `name` FROM `ayurved_massages` ORDER BY `id` ASC");
        }

        // Fallback 3: Select * from ayurved_massages
        if (!$result || mysqli_num_rows($result) === 0) {
            $result = @mysqli_query($db, "SELECT * FROM `ayurved_massages` ORDER BY `id` ASC");
        }

        // Fallback 4: Check category_services where category_id = '2'
        if (!$result || mysqli_num_rows($result) === 0) {
            $checkCat = @mysqli_query($db, "SELECT * FROM `category_services` WHERE `category_id` = '2' ORDER BY `id` ASC");
            if ($checkCat && mysqli_num_rows($checkCat) > 0) {
                $result = $checkCat;
                $tableUsed = "category_services";
            }
        }

    } else {
        // --- OTHER CATEGORIES (3: Physio, 4: Trainer, 5: Yoga, 6: Zumba, 7: Nutritionist, 8: Chiropractor) ---
        if ($categoryId === '3' || strpos($norm, 'physio') !== false) {
            $tableUsed = "physiotherapy_services";
        } elseif ($categoryId === '4' || strpos($norm, 'trainer') !== false || strpos($norm, 'fitness') !== false) {
            $tableUsed = "trainer_services";
        } elseif ($categoryId === '5' || strpos($norm, 'yoga') !== false) {
            $tableUsed = "yoga_services";
        } elseif ($categoryId === '6' || strpos($norm, 'zumba') !== false) {
            $tableUsed = "zumba_services";
        } elseif ($categoryId === '7' || strpos($norm, 'nutri') !== false) {
            $tableUsed = "nutrition_services";
        } elseif ($categoryId === '8' || strpos($norm, 'chiro') !== false) {
            $tableUsed = "chiropractor_services";
        } else {
            $tableUsed = "category_services";
        }

        // Try dedicated table
        $result = @mysqli_query($db, "SELECT * FROM `$tableUsed` ORDER BY `id` ASC");

        // If dedicated table is empty or missing, try shared category_services
        if ((!$result || mysqli_num_rows($result) === 0) && !empty($safeCatId)) {
            $result = @mysqli_query($db, "SELECT * FROM `category_services` WHERE `category_id` = '$safeCatId' ORDER BY `id` ASC");
            if ($result && mysqli_num_rows($result) > 0) {
                $tableUsed = "category_services";
            }
        }
    }

    // Process result rows
    if ($result && mysqli_num_rows($result) > 0) {
        while ($row = mysqli_fetch_assoc($result)) {
            $sName = trim($row['name'] ?? ($row['service_name'] ?? ($row['title'] ?? ($row['service'] ?? ''))));
            if ($sName === '') {
                continue;
            }

            $normalized = mb_strtolower(preg_replace('/\s+/', ' ', $sName), 'UTF-8');
            if (!isset($seenNames[$normalized])) {
                $seenNames[$normalized] = true;
                $serviceId = (string)($row['id'] ?? (count($services) + 1));

                $services[] = $sName;
                $serviceItems[] = [
                    "id"   => $serviceId,
                    "name" => $sName
                ];
            }
        }
        mysqli_free_result($result);
    }

    // Strict rule: For Massage and Ayurveda, NEVER apply static fallback catalog!
    if (empty($services) && !$isMassage && !$isAyurveda) {
        // Fallback for non-massage / non-ayurveda categories only
        $fallbackList = $defaultCatalog[$categoryId] ?? null;
        if (!$fallbackList && !empty($norm)) {
            if (strpos($norm, 'physio') !== false) {
                $fallbackList = $defaultCatalog['3'];
            } elseif (strpos($norm, 'trainer') !== false) {
                $fallbackList = $defaultCatalog['4'];
            } elseif (strpos($norm, 'yoga') !== false) {
                $fallbackList = $defaultCatalog['5'];
            } elseif (strpos($norm, 'zumba') !== false) {
                $fallbackList = $defaultCatalog['6'];
            } elseif (strpos($norm, 'nutri') !== false) {
                $fallbackList = $defaultCatalog['7'];
            } elseif (strpos($norm, 'chiro') !== false) {
                $fallbackList = $defaultCatalog['8'];
            }
        }

        if ($fallbackList) {
            $services = $fallbackList;
            foreach ($services as $idx => $s) {
                $serviceItems[] = [
                    "id"   => (string)($idx + 1),
                    "name" => $s
                ];
            }
        }
    }

    $response = [
        "status"        => true,
        "success"       => true,
        "category_id"   => $categoryId,
        "category_name" => $categoryName,
        "table_used"    => $tableUsed,
        "count"         => count($services),
        "services"      => $services,
        "service_items" => $serviceItems,
        "data"          => $serviceItems
    ];

    if ($isMassage) {
        $response["home_massage"] = $serviceItems;
        $response["category"] = "Massage Therapist";
    } elseif ($isAyurveda) {
        $response["ayurved_massages"] = $serviceItems;
        $response["category"] = "Ayurveda Therapist";
    }

    if (empty($services) && ($isMassage || $isAyurveda)) {
        $response["message"] = "No services found in database for this category";
    }

    echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    mysqli_close($db);

} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode([
        "status"  => false,
        "success" => false,
        "message" => "An error occurred while fetching category services",
        "error"   => $e->getMessage()
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
}
?>
