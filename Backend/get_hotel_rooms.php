<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$hotelId = isset($_GET['hotel_registration_id']) ? trim($_GET['hotel_registration_id']) : '';

$rooms = [
    [ "id" => "1",  "room_number" => "101" ],
    [ "id" => "2",  "room_number" => "102" ],
    [ "id" => "3",  "room_number" => "103" ],
    [ "id" => "4",  "room_number" => "104" ],
    [ "id" => "5",  "room_number" => "105" ],
    [ "id" => "6",  "room_number" => "112" ],
    [ "id" => "7",  "room_number" => "201" ],
    [ "id" => "8",  "room_number" => "202" ],
    [ "id" => "9",  "room_number" => "205" ],
    [ "id" => "10", "room_number" => "301" ],
    [ "id" => "11", "room_number" => "304" ],
    [ "id" => "12", "room_number" => "401" ],
    [ "id" => "13", "room_number" => "418" ]
];

$response = [
    "status"  => true,
    "success" => true,
    "message" => "Rooms fetched successfully",
    "hotel_id" => $hotelId,
    "data"    => $rooms
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
