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

    // 1. Disable ONLY_FULL_GROUP_BY for this MySQL session so GROUP BY name with SELECT id is allowed
    @mysqli_query($db, "SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");

    $result = false;
    $tableUsed = "home_massage";

    // 2. Primary Query: exact query requested by user
    $sqlPrimary = "SELECT `id`, `name` FROM `home_massage` GROUP BY `name` ORDER BY MIN(`id`) ASC";
    $result = @mysqli_query($db, $sqlPrimary);

    // 3. Fallback Query 1: MIN(id) AS id with GROUP BY name (in case ONLY_FULL_GROUP_BY could not be disabled)
    if (!$result || mysqli_num_rows($result) === 0) {
        $sqlFallback1 = "SELECT MIN(`id`) AS `id`, `name` FROM `home_massage` GROUP BY `name` ORDER BY MIN(`id`) ASC";
        $result = @mysqli_query($db, $sqlFallback1);
    }

    // 4. Fallback Query 2: Fetch all rows ordered by id (no status filter)
    if (!$result || mysqli_num_rows($result) === 0) {
        $sqlFallback2 = "SELECT `id`, `name` FROM `home_massage` ORDER BY `id` ASC";
        $result = @mysqli_query($db, $sqlFallback2);
    }

    // 5. Fallback Query 3: SELECT * from home_massage (flexible column names)
    if (!$result || mysqli_num_rows($result) === 0) {
        $sqlFallback3 = "SELECT * FROM `home_massage` ORDER BY `id` ASC";
        $result = @mysqli_query($db, $sqlFallback3);
    }

    // 6. Fallback Query 4: In case table is named home_massages (plural)
    if (!$result || mysqli_num_rows($result) === 0) {
        $sqlPlural = "SELECT * FROM `home_massages` ORDER BY `id` ASC";
        $checkPlural = @mysqli_query($db, $sqlPlural);
        if ($checkPlural && mysqli_num_rows($checkPlural) > 0) {
            $result = $checkPlural;
            $tableUsed = "home_massages";
        }
    }

    // 7. Fallback Query 5: In case massage services are in category_services table (category_id = 1)
    if (!$result || mysqli_num_rows($result) === 0) {
        $sqlCatServices = "SELECT * FROM `category_services` WHERE `category_id` = '1' ORDER BY `id` ASC";
        $checkCat = @mysqli_query($db, $sqlCatServices);
        if ($checkCat && mysqli_num_rows($checkCat) > 0) {
            $result = $checkCat;
            $tableUsed = "category_services";
        }
    }

    $services = [];
    $homeMassageList = [];
    $seenNames = [];

    if ($result && mysqli_num_rows($result) > 0) {
        while ($row = mysqli_fetch_assoc($result)) {
            // Flexible column detection: name, service_name, title, service
            $rawName = trim($row['name'] ?? ($row['service_name'] ?? ($row['title'] ?? ($row['service'] ?? ''))));
            if ($rawName === '') {
                continue;
            }

            // Deduplicate in PHP (case-insensitive & normalized whitespace)
            $normalizedKey = mb_strtolower(preg_replace('/\s+/', ' ', $rawName), 'UTF-8');

            if (!isset($seenNames[$normalizedKey])) {
                $seenNames[$normalizedKey] = true;
                $serviceId = (string)($row['id'] ?? (count($services) + 1));

                $services[] = $rawName;
                $homeMassageList[] = [
                    "id"   => $serviceId,
                    "name" => $rawName
                ];
            }
        }
        mysqli_free_result($result);
    }

    // 8. Build Response - dynamic rows from home_massage database ONLY (strictly NO static fallback array!)
    $response = [
        "status"       => true,
        "success"      => true,
        "category"     => "Massage Therapist",
        "table"        => $tableUsed,
        "count"        => count($services),
        "services"     => $services,
        "home_massage" => $homeMassageList,
        "data"         => $homeMassageList
    ];

    // Diagnostic information if 0 rows returned
    if (empty($services)) {
        $err = mysqli_error($db);
        $totalRows = 0;
        $cntRes = @mysqli_query($db, "SELECT COUNT(*) AS total FROM `home_massage`");
        if ($cntRes && $cRow = mysqli_fetch_assoc($cntRes)) {
            $totalRows = intval($cRow['total']);
        }
        $response["message"] = "No services found in home_massage table";
        $response["debug"] = [
            "table_total_rows" => $totalRows,
            "mysql_error"      => !empty($err) ? $err : null
        ];
    }

    echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    mysqli_close($db);

} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode([
        "status"  => false,
        "success" => false,
        "message" => "An error occurred while fetching massage services",
        "error"   => $e->getMessage()
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
}
?>
