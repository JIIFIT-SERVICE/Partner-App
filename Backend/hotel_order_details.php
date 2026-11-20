<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Support GET id or POST JSON {"booking_id": id}
$bookingId = isset($_GET['id']) ? trim($_GET['id']) : '';

if (!$bookingId) {
    $rawInput = file_get_contents('php://input');
    if ($rawInput) {
        $json = json_decode($rawInput, true);
        if (isset($json['booking_id'])) {
            $bookingId = trim($json['booking_id']);
        }
    }
}

if (!$bookingId) {
    $bookingId = '834';
}

$response = [
    "status" => "success",
    "message" => "Order details retrieved successfully",
    "data" => [
        "booking_id"                   => $bookingId,
        "customer_id"                  => "CUST-108",
        "order_date"                   => date('Y-m-d'),
        "massage_types"                => "massage at hotel",
        "start_time"                   => "07:00 PM",
        "end_time"                     => "08:00 PM",
        "note"                         => "Please bring lavender essential oil. Quiet and relaxing session.",
        "full_address"                 => "Room 112, 1st Floor, Royal Palace Hotel",
        "need_bed"                     => 1,
        "call_after_booking"           => 1,
        "ring_bell"                    => 1,
        "do_not_call_before_reaching"  => 0,
        "user_status"                  => "Active",
        "status"                       => "PENDING",
        "otp_status"                   => "Unverified",
        "payment_status"               => "PENDING",
        "mobile"                       => "+91 98765 43210",
        "total_price"                  => "1674.00",
        "payment_mode"                 => "UPI / Cash after service",
        "who_take"                     => "Guest",
        "reciver_name"                 => "Megha Sharma",
        "r_wp_num"                     => "9876543210",
        "relationship"                 => "Hotel Guest",
        "photoPath"                    => "https://images.unsplash.com/photo-1544005313-94ddf0286df2",
        "otp"                          => "4829",
        "expert_name"                  => "Rohan Sharma",
        "expert_number"                => "+91 98765 43210"
    ]
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
