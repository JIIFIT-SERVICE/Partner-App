<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Read inputs from POST or JSON body
$guestName    = isset($_POST['guest_name']) ? trim($_POST['guest_name']) : 'Guest';
$guestMobile  = isset($_POST['guest_mobile']) ? trim($_POST['guest_mobile']) : '';
$roomNumber   = isset($_POST['room_number']) ? trim($_POST['room_number']) : '101';
$bookingDate  = isset($_POST['booking_date']) ? trim($_POST['booking_date']) : date('Y-m-d');
$bookingTime  = isset($_POST['booking_time']) ? trim($_POST['booking_time']) : '07:00 PM';
$expertName   = isset($_POST['expert_name']) ? trim($_POST['expert_name']) : 'Rohan Sharma';
$totalPayable = isset($_POST['total_payable']) ? trim($_POST['total_payable']) : '1499.00';

// Generate dynamic or consistent order ID and 4-digit OTP
$generatedOrderId = (string) rand(800, 999);
$generatedOtp     = (string) rand(1000, 9999);

// Handle optional voice note upload if present
$uploadedVoiceNote = null;
if (isset($_FILES['voice_note']) && $_FILES['voice_note']['error'] === UPLOAD_ERR_OK) {
    $uploadDir = __DIR__ . '/uploads/voice_notes/';
    if (!is_dir($uploadDir)) {
        mkdir($uploadDir, 0777, true);
    }
    $fileName = time() . '_' . basename($_FILES['voice_note']['name']);
    $targetPath = $uploadDir . $fileName;
    if (move_uploaded_file($_FILES['voice_note']['tmp_name'], $targetPath)) {
        $uploadedVoiceNote = 'uploads/voice_notes/' . $fileName;
    }
}

$response = [
    "success"     => true,
    "status"      => true,
    "message"     => "Hotel booking submitted successfully",
    "orderId"     => $generatedOrderId,
    "booking_otp" => $generatedOtp,
    "booking"     => [
        "orderId"       => $generatedOrderId,
        "booking_otp"   => $generatedOtp,
        "guest_name"    => $guestName,
        "guest_mobile"  => $guestMobile,
        "room_number"   => $roomNumber,
        "booking_date"  => $bookingDate,
        "booking_time"  => $bookingTime,
        "expert_name"   => $expertName,
        "total_payable" => $totalPayable,
        "voice_note"    => $uploadedVoiceNote,
        "status"        => "PENDING"
    ]
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
