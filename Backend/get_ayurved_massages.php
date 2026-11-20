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

    // 1. Disable ONLY_FULL_GROUP_BY for this session so GROUP BY name with SELECT id is 100% allowed
    @mysqli_query($db, "SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");

    // 2. Primary Query: exact query requested (status filter removed)
    $sql = "SELECT `id`, `name` FROM `ayurved_massages` GROUP BY `name` ORDER BY MIN(`id`) ASC";
    $result = @mysqli_query($db, $sql);

    // 3. Fallback Query 1: MIN(id) AS id with GROUP BY name (in case sql_mode could not be altered)
    if (!$result || mysqli_num_rows($result) === 0) {
        $sqlFallback = "SELECT MIN(`id`) AS `id`, `name` FROM `ayurved_massages` GROUP BY `name` ORDER BY MIN(`id`) ASC";
        $result = @mysqli_query($db, $sqlFallback);
    }

    // 4. Fallback Query 2: If GROUP BY failed or returned 0, fetch all rows ordered by id
    if (!$result || mysqli_num_rows($result) === 0) {
        $sqlAll = "SELECT `id`, `name` FROM `ayurved_massages` ORDER BY `id` ASC";
        $result = @mysqli_query($db, $sqlAll);
    }

    // 5. Fallback Query 3: Select * from ayurved_massages
    if (!$result || mysqli_num_rows($result) === 0) {
        $sqlWildcard = "SELECT * FROM `ayurved_massages` ORDER BY `id` ASC";
        $result = @mysqli_query($db, $sqlWildcard);
    }

    $services = [];
    $ayurvedList = [];
    $seenNames = [];

    if ($result && mysqli_num_rows($result) > 0) {
        while ($row = mysqli_fetch_assoc($result)) {
            $rawName = trim($row['name'] ?? ($row['service_name'] ?? ($row['title'] ?? ($row['service'] ?? ''))));
            if ($rawName === '') {
                continue;
            }

            // Deduplicate in PHP (case-insensitive & whitespace trimmed)
            $normalizedKey = mb_strtolower(preg_replace('/\s+/', ' ', $rawName), 'UTF-8');

            if (!isset($seenNames[$normalizedKey])) {
                $seenNames[$normalizedKey] = true;
                $serviceId = (string)($row['id'] ?? (count($services) + 1));
                $services[] = $rawName;
                $ayurvedList[] = [
                    "id"   => $serviceId,
                    "name" => $rawName
                ];
            }
        }
        mysqli_free_result($result);
    }

    // 6. Build Response - dynamic rows from ayurved_massages table only (strictly NO static fallback array!)
    $response = [
        "status"           => true,
        "success"          => true,
        "category"         => "Ayurveda Therapist",
        "table"            => "ayurved_massages",
        "count"            => count($services),
        "services"         => $services,
        "ayurved_massages" => $ayurvedList,
        "data"             => $ayurvedList
    ];

    // If table returned 0 rows, include debug diagnostics so user knows why
    if (empty($services)) {
        $err = mysqli_error($db);
        $cntRes = @mysqli_query($db, "SELECT COUNT(*) AS total FROM `ayurved_massages`");
        $totalRows = 0;
        if ($cntRes && $cRow = mysqli_fetch_assoc($cntRes)) {
            $totalRows = intval($cRow['total']);
        }
        $response["message"] = "No services found in ayurved_massages table";
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
        "message" => "An error occurred while fetching ayurveda services",
        "error"   => $e->getMessage()
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
}
?>
