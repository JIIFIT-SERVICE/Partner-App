<?php
// PhonePe S2S Callback Handler for Kit / Training Payment
error_reporting(0);
ini_set('display_errors', '0');

header('Content-Type: application/json; charset=UTF-8');

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
        echo json_encode(["status" => false, "message" => "Database connection failed"]);
        exit();
    }

    $rawInput = file_get_contents('php://input');
    $postData = !empty($_POST) ? $_POST : json_decode($rawInput, true);

    $base64Response = isset($postData['response']) ? $postData['response'] : '';
    $decoded = [];

    if (!empty($base64Response)) {
        $decoded = json_decode(base64_decode($base64Response), true);
    } elseif (is_array($postData)) {
        $decoded = $postData;
    }

    $code = $decoded['code'] ?? '';
    $isSuccess = ($decoded['success'] ?? false) || ($code === 'PAYMENT_SUCCESS');
    $data = $decoded['data'] ?? [];

    $merchantTransactionId = $data['merchantTransactionId'] ?? ($data['transactionId'] ?? '');
    $amountInPaise = isset($data['amount']) ? intval($data['amount']) : 0;
    $amountInRupees = $amountInPaise > 0 ? ($amountInPaise / 100.0) : 0.0;
    $merchantUserId = $data['merchantUserId'] ?? '';

    // Extract therapist ID
    // 1. From merchantUserId (format: "UID_{id}")
    $therapistId = '';
    if (strpos($merchantUserId, 'UID_') === 0) {
        $therapistId = substr($merchantUserId, 4);
    }

    // 2. Fallback: From merchantTransactionId (format: "KIT_{id}_{timestamp}")
    if (empty($therapistId) && strpos($merchantTransactionId, 'KIT_') === 0) {
        $parts = explode('_', $merchantTransactionId);
        if (count($parts) >= 2) {
            $therapistId = $parts[1];
        }
    }

    if ($isSuccess && !empty($therapistId)) {
        $safeId     = mysqli_real_escape_string($db, $therapistId);
        $safeAmount = mysqli_real_escape_string($db, strval($amountInRupees));
        $safeTxnId  = mysqli_real_escape_string($db, $merchantTransactionId);
        $now        = date('Y-m-d H:i:s');

        // Verify columns exist
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

        $updateSql = "UPDATE `therapists` SET 
            `kit_payment` = 'completed',
            `payment_amount` = '$safeAmount',
            `transaction_id` = '$safeTxnId',
            `payment_date` = '$now'
        WHERE `id` = '$safeId'";

        @mysqli_query($db, $updateSql);

        echo json_encode([
            "status"  => true,
            "message" => "Callback processed successfully",
            "id"      => $therapistId,
            "txn"     => $merchantTransactionId
        ]);
    } else {
        echo json_encode([
            "status"  => false,
            "message" => "Payment was not successful or therapist ID could not be identified",
            "code"    => $code
        ]);
    }

    @mysqli_close($db);

} catch (Throwable $e) {
    http_response_code(200);
    echo json_encode(["status" => false, "message" => $e->getMessage()]);
}
?>

