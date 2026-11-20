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

    $rawInput = file_get_contents('php://input');
    $jsonData = json_decode($rawInput, true);

    $therapistId = isset($_POST['id']) ? trim($_POST['id']) : (
        isset($_POST['therapist_id']) ? trim($_POST['therapist_id']) : (
            isset($_GET['id']) ? trim($_GET['id']) : (
                $jsonData['id'] ?? ($jsonData['therapist_id'] ?? '')
            )
        )
    );

    $amount = isset($_POST['payment_amount']) ? trim($_POST['payment_amount']) : (
        isset($_POST['amount']) ? trim($_POST['amount']) : (
            isset($_GET['payment_amount']) ? trim($_GET['payment_amount']) : (
                $jsonData['payment_amount'] ?? ($jsonData['amount'] ?? '')
            )
        )
    );

    $transactionId = isset($_POST['transaction_id']) ? trim($_POST['transaction_id']) : (
        isset($_POST['merchantTransactionId']) ? trim($_POST['merchantTransactionId']) : (
            isset($_GET['transaction_id']) ? trim($_GET['transaction_id']) : (
                $jsonData['transaction_id'] ?? ($jsonData['merchantTransactionId'] ?? '')
            )
        )
    );

    $paymentDate = isset($_POST['payment_date']) ? trim($_POST['payment_date']) : (
        $jsonData['payment_date'] ?? date('Y-m-d H:i:s')
    );

    if (empty($therapistId)) {
        echo json_encode([
            "status"  => false,
            "message" => "Missing therapist ID"
        ]);
        exit();
    }

    $safeId     = mysqli_real_escape_string($db, $therapistId);
    $safeAmount = mysqli_real_escape_string($db, $amount);
    $safeTxnId  = mysqli_real_escape_string($db, $transactionId);
    $safeDate   = mysqli_real_escape_string($db, $paymentDate);

    // Verify columns exist in therapists table and add them if not present
    $colsRes = @mysqli_query($db, "SHOW COLUMNS FROM `therapists`");
    $existingCols = [];
    if ($colsRes) {
        while ($cRow = mysqli_fetch_assoc($colsRes)) {
            $existingCols[] = strtolower($cRow['Field']);
        }
    }

    if (!in_array('kit_payment', $existingCols)) {
        @mysqli_query($db, "ALTER TABLE `therapists` ADD COLUMN `kit_payment` VARCHAR(50) DEFAULT 'pending'");
    }
    if (!in_array('payment_amount', $existingCols)) {
        @mysqli_query($db, "ALTER TABLE `therapists` ADD COLUMN `payment_amount` DECIMAL(10,2) DEFAULT NULL");
    }
    if (!in_array('transaction_id', $existingCols)) {
        @mysqli_query($db, "ALTER TABLE `therapists` ADD COLUMN `transaction_id` VARCHAR(100) DEFAULT NULL");
    }
    if (!in_array('payment_date', $existingCols)) {
        @mysqli_query($db, "ALTER TABLE `therapists` ADD COLUMN `payment_date` DATETIME DEFAULT NULL");
    }

    // Update therapist table with kit_payment = 'completed'
    $updateSql = "UPDATE `therapists` SET 
        `kit_payment` = 'completed',
        `payment_amount` = '$safeAmount',
        `transaction_id` = '$safeTxnId',
        `payment_date` = '$safeDate'
    WHERE `id` = '$safeId'";

    $result = @mysqli_query($db, $updateSql);

    if ($result) {
        echo json_encode([
            "status"         => true,
            "success"        => true,
            "message"        => "Kit payment status updated successfully",
            "id"             => $therapistId,
            "kit_payment"    => "completed",
            "payment_amount" => $safeAmount,
            "transaction_id" => $safeTxnId,
            "payment_date"   => $safeDate
        ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    } else {
        echo json_encode([
            "status"  => false,
            "success" => false,
            "message" => "Failed to update kit payment: " . mysqli_error($db)
        ]);
    }

    @mysqli_close($db);

} catch (Throwable $e) {
    http_response_code(200);
    echo json_encode([
        "status"  => false,
        "success" => false,
        "message" => "Server error: " . $e->getMessage()
    ]);
}
?>

