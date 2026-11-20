<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$hotelId    = isset($_GET['id']) ? trim($_GET['id']) : '';
$selectDate = isset($_GET['selectdate']) ? strtolower(trim($_GET['selectdate'])) : 'today';

$todayDate     = date('Y-m-d');
$tomorrowDate  = date('Y-m-d', strtotime('+1 day'));
$yesterdayDate = date('Y-m-d', strtotime('-1 day'));

// Full sample bookings list
$allBookings = [
    [
        "id"             => "834",
        "customer_name"  => "Megha Sharma",
        "start_time"     => "07:00 PM",
        "end_time"       => "08:00 PM",
        "landmark"       => "Room 112",
        "payment_status" => "PENDING",
        "order_date"     => $todayDate,
        "in_room"        => true,
        "room_number"    => "112",
        "massage_types"  => "massage at hotel",
        "expert_number"  => "+91 98765 43210",
        "expert_name"    => "Rohan Sharma",
        "duration"       => "60",
        "exper_photo"    => "https://images.unsplash.com/photo-1534528741775-53994a69daeb"
    ],
    [
        "id"             => "823",
        "customer_name"  => "Charu Khanna",
        "start_time"     => "01:00 PM",
        "end_time"       => "02:30 PM",
        "landmark"       => "Room 304",
        "payment_status" => "COMPLETED",
        "order_date"     => $todayDate,
        "in_room"        => true,
        "room_number"    => "304",
        "massage_types"  => "massage at hotel",
        "expert_number"  => "+91 98765 43211",
        "expert_name"    => "Pooja Verma",
        "duration"       => "90",
        "exper_photo"    => "https://images.unsplash.com/photo-1517841905240-472988babdf9"
    ],
    [
        "id"             => "845",
        "customer_name"  => "Vikramaditya Roy",
        "start_time"     => "09:30 AM",
        "end_time"       => "10:30 AM",
        "landmark"       => "Room 418",
        "payment_status" => "CONFIRMED",
        "order_date"     => $tomorrowDate,
        "in_room"        => true,
        "room_number"    => "418",
        "massage_types"  => "yoga at hotel",
        "expert_number"  => "+91 98765 43212",
        "expert_name"    => "Amit Patel",
        "duration"       => "60",
        "exper_photo"    => "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d"
    ],
    [
        "id"             => "812",
        "customer_name"  => "Ananya Verma",
        "start_time"     => "04:00 PM",
        "end_time"       => "05:00 PM",
        "landmark"       => "Room 205",
        "payment_status" => "CANCELLED",
        "order_date"     => $yesterdayDate,
        "in_room"        => true,
        "room_number"    => "205",
        "massage_types"  => "ayurveda at hotel",
        "expert_number"  => "+91 98765 43213",
        "expert_name"    => "Sneha Kulkarni",
        "duration"       => "60",
        "exper_photo"    => "https://images.unsplash.com/photo-1544005313-94ddf0286df2"
    ],
    [
        "id"             => "798",
        "customer_name"  => "Rohan Kapoor",
        "start_time"     => "03:30 PM",
        "end_time"       => "04:30 PM",
        "landmark"       => "Room 102",
        "payment_status" => "COMPLETED",
        "order_date"     => date('Y-m-d', strtotime('-2 days')),
        "in_room"        => true,
        "room_number"    => "102",
        "massage_types"  => "personal trainer at hotel",
        "expert_number"  => "+91 98765 43214",
        "expert_name"    => "Karan Singh",
        "duration"       => "60",
        "exper_photo"    => "https://images.unsplash.com/photo-1500648767791-00dcc994a43e"
    ]
];

// Filter by selectdate if specified
$filteredBookings = [];
if ($selectDate === 'all') {
    $filteredBookings = $allBookings;
} elseif ($selectDate === 'today') {
    $filteredBookings = array_values(array_filter($allBookings, fn($b) => $b['order_date'] === $todayDate));
} elseif ($selectDate === 'tomorrow') {
    $filteredBookings = array_values(array_filter($allBookings, fn($b) => $b['order_date'] === $tomorrowDate));
} elseif ($selectDate === 'yesterday') {
    $filteredBookings = array_values(array_filter($allBookings, fn($b) => $b['order_date'] === $yesterdayDate));
} else {
    // Specific date format YYYY-MM-DD
    $filteredBookings = array_values(array_filter($allBookings, fn($b) => $b['order_date'] === $selectDate));
    if (empty($filteredBookings)) {
        // Fallback default so screen doesn't break
        $filteredBookings = array_slice($allBookings, 0, 2);
    }
}

$response = [
    "status"  => true,
    "success" => true,
    "message" => "Bookings fetched successfully",
    "count"   => count($filteredBookings),
    "data"    => $filteredBookings
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
