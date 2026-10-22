<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$decoded_id = isset($_GET['decoded_id']) ? trim($_GET['decoded_id']) : '';

// Default sample booking data for demonstration
$response = [
    "success" => true,
    "message" => "Booking retrieved successfully",
    "data" => [
        "bookingId" => "834",
        "who_take_status" => 1,
        "reciver_name" => "Megha Sharma",
        "relationship" => "Hotel Guest",
        "order_date" => date('Y-m-d'),
        "start_time" => "07:00 PM",
        "therapist_name" => "Rohan Sharma",
        "order_items" => "Deep Tissue In-Room Massage (1 x 1499.00), Foot Reflexology (1 x 499.00)",
        "total_price" => "2097.90",
        "discunt_price" => "100.00",
        "GST" => "99.90",
        "payment_mode" => "UPI / Cash after massage",
        "booking_otp" => "4829",
        "r_wp_num" => "9876543210"
    ]
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
