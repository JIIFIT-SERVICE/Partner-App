<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$hotelRegistrationId = isset($_GET['hotel_registration_id']) ? trim($_GET['hotel_registration_id']) : '';

$response = [
    "status"  => true,
    "success" => true,
    "message" => "Commission charge retrieved successfully",
    "hotel_registration_id" => $hotelRegistrationId,
    "data"    => [
        [
            "commission_charge" => 100.00
        ]
    ]
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
