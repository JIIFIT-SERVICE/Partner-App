<?php
error_reporting(0);
ini_set('display_errors', '0');

header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if (function_exists('mysqli_report')) {
    @mysqli_report(MYSQLI_REPORT_OFF);
}

try {
    require_once "../connection.php";

    if (function_exists('mysqli_report')) {
        @mysqli_report(MYSQLI_REPORT_OFF);
    }

    $db = isset($con) ? $con : (isset($conn) ? $conn : null);

    if (!$db || !($db instanceof mysqli)) {
        http_response_code(200);
        echo json_encode([
            "status"  => false,
            "success" => false,
            "message" => "Database connection failed"
        ]);
        exit();
    }

    $therapistId = isset($_POST['id']) ? trim($_POST['id']) : (isset($_GET['id']) ? trim($_GET['id']) : '');

    if ($therapistId === '') {
        echo json_encode([
            "status"  => false,
            "message" => "Missing therapist ID"
        ]);
        exit();
    }

    $safeId = mysqli_real_escape_string($db, $therapistId);
    $therapistQuery = "SELECT `id`, `categories`, `expert_category`, `selected_services` FROM `therapists` WHERE `id` = '$safeId' LIMIT 1";
    $therapistRes = @mysqli_query($db, $therapistQuery);

    if (!$therapistRes || mysqli_num_rows($therapistRes) === 0) {
        echo json_encode([
            "status"  => false,
            "message" => "Therapist not found"
        ]);
        exit();
    }

    $tRow = mysqli_fetch_assoc($therapistRes);
    $categoriesRaw = isset($tRow['categories']) && trim($tRow['categories']) !== '' 
        ? trim($tRow['categories']) 
        : (isset($tRow['expert_category']) ? trim($tRow['expert_category']) : '');

    $selectedServicesRaw = isset($tRow['selected_services']) ? trim($tRow['selected_services']) : '';

    $selectedList = [];
    if ($selectedServicesRaw !== '') {
        $parts = explode(',', $selectedServicesRaw);
        foreach ($parts as $p) {
            $cleaned = trim(str_replace(['✓', '✔', '√'], '', $p));
            if ($cleaned !== '') {
                $selectedList[] = $cleaned;
            }
        }
    }

    // Determine category flags
    $catLower = strtolower($categoriesRaw);
    $tokens = preg_split('/[\s,\[\]"\'\;]+/', $categoriesRaw, -1, PREG_SPLIT_NO_EMPTY);

    $isMassage = in_array('1', $tokens) || in_array('9', $tokens) || strpos($catLower, 'massage at home') !== false || strpos($catLower, 'massage therapist') !== false || (strpos($catLower, 'massage') !== false && strpos($catLower, 'ayurved') === false);
    $isAyurveda = in_array('2', $tokens) || strpos($catLower, 'ayurved') !== false;

    if (!$isMassage && !$isAyurveda) {
        $isMassage = true;
        $isAyurveda = true;
    }

    $allCatalogServices = [];

    // Fetch from home_massage
    if ($isMassage) {
        $hmRes = @mysqli_query($db, "SELECT `name` FROM `home_massage` GROUP BY `name` ORDER BY MIN(`id`) ASC");
        if (!$hmRes || mysqli_num_rows($hmRes) === 0) {
            $hmRes = @mysqli_query($db, "SELECT `name` FROM `home_massage` ORDER BY `id` ASC");
        }
        if ($hmRes) {
            while ($hmRow = mysqli_fetch_assoc($hmRes)) {
                $sName = trim($hmRow['name'] ?? '');
                if ($sName !== '' && !in_array($sName, $allCatalogServices)) {
                    $allCatalogServices[] = $sName;
                }
            }
        }
    }

    // Fetch from ayurved_massages
    if ($isAyurveda) {
        $ayRes = @mysqli_query($db, "SELECT `name` FROM `ayurved_massages` GROUP BY `name` ORDER BY MIN(`id`) ASC");
        if (!$ayRes || mysqli_num_rows($ayRes) === 0) {
            $ayRes = @mysqli_query($db, "SELECT `name` FROM `ayurved_massages` ORDER BY `id` ASC");
        }
        if ($ayRes) {
            while ($ayRow = mysqli_fetch_assoc($ayRes)) {
                $sName = trim($ayRow['name'] ?? '');
                if ($sName !== '' && !in_array($sName, $allCatalogServices)) {
                    $allCatalogServices[] = $sName;
                }
            }
        }
    }

    function normalizeName($str) {
        $clean = str_replace(['✓', '✔', '√', '–', '-', '&'], [' ', ' ', ' ', ' ', ' ', ' and '], $str);
        $clean = preg_replace('/[^a-zA-Z0-9 ]+/', ' ', $clean);
        $clean = preg_replace('/\s+/', ' ', $clean);
        return strtolower(trim($clean));
    }

    $massages = [];
    foreach ($allCatalogServices as $serviceName) {
        $normCatalog = normalizeName($serviceName);
        $isMatch = false;

        foreach ($selectedList as $sel) {
            $normSel = normalizeName($sel);
            if ($normCatalog === $normSel) {
                $isMatch = true;
                break;
            }
            if (strlen($normCatalog) >= 8 && strlen($normSel) >= 8) {
                if (strpos($normCatalog, $normSel) !== false || strpos($normSel, $normCatalog) !== false) {
                    $isMatch = true;
                    break;
                }
            }
        }

        $massages[] = [
            "name"        => $serviceName,
            "subtitle"    => $isMatch ? "(Live will check)" : "(Company will provide training)",
            "is_approved" => $isMatch,
            "status_text" => $isMatch ? "Approved" : "Training Required"
        ];
    }

    // Sort: Approved first, then by name
    usort($massages, function($a, $b) {
        if ($a['is_approved'] !== $b['is_approved']) {
            return $a['is_approved'] ? -1 : 1;
        }
        return strcmp($a['name'], $b['name']);
    });

    echo json_encode([
        "status"            => true,
        "therapist_id"      => $therapistId,
        "categories"        => $categoriesRaw,
        "selected_services" => $selectedServicesRaw,
        "massages"          => $massages
    ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);

} catch (Throwable $e) {
    http_response_code(200);
    echo json_encode([
        "status"  => false,
        "message" => "An error occurred: " . $e->getMessage()
    ]);
}
?>

