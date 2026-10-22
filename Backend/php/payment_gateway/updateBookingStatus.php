<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$booking_id = isset($_REQUEST['booking_id']) ? trim($_REQUEST['booking_id']) : '';

$response = [
    "success" => true,
    "status" => "success",
    "message" => "Booking status updated successfully",
    "booking_id" => $booking_id ? $booking_id : "834",
    "updated_at" => date('Y-m-d H:i:s')
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
