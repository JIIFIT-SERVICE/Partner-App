<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$rawInput = file_get_contents('php://input');
$data = json_decode($rawInput, true);

$gender = isset($data['client_gender']) ? strtolower(trim($data['client_gender'])) : 'male';

$therapists = [
    [
        "id"            => "TH-101",
        "name"          => "Rohan Sharma",
        "experience"    => "5 years",
        "profile_photo" => "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
        "role"          => "Senior Massage Therapist",
        "howe_town"     => "Mumbai",
        "lifetime_exp"  => "5+ Years Luxury Hotel Experience",
        "rating"        => "4.9",
        "rating_count"  => "142",
        "availability"  => "Available for in-room service"
    ],
    [
        "id"            => "TH-102",
        "name"          => "Pooja Verma",
        "experience"    => "4 years",
        "profile_photo" => "https://images.unsplash.com/photo-1517841905240-472988babdf9",
        "role"          => "Ayurveda & Spa Specialist",
        "howe_town"     => "Delhi",
        "lifetime_exp"  => "4+ Years Certified Therapist",
        "rating"        => "4.8",
        "rating_count"  => "98",
        "availability"  => "Available for in-room service"
    ],
    [
        "id"            => "TH-103",
        "name"          => "Amit Patel",
        "experience"    => "6 years",
        "profile_photo" => "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
        "role"          => "Deep Tissue & Sports Recovery Expert",
        "howe_town"     => "Bangalore",
        "lifetime_exp"  => "6+ Years Wellness Coach",
        "rating"        => "5.0",
        "rating_count"  => "210",
        "availability"  => "Available for in-room service"
    ],
    [
        "id"            => "TH-104",
        "name"          => "Sneha Kulkarni",
        "experience"    => "3 years",
        "profile_photo" => "https://images.unsplash.com/photo-1544005313-94ddf0286df2",
        "role"          => "Holistic Wellness & Yoga Therapist",
        "howe_town"     => "Pune",
        "lifetime_exp"  => "3+ Years Luxury Resort Therapist",
        "rating"        => "4.7",
        "rating_count"  => "64",
        "availability"  => "Available for in-room service"
    ]
];

$response = [
    "success"    => true,
    "status"     => true,
    "message"    => "Therapists fetched successfully",
    "therapists" => $therapists
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
