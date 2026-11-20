<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$serviceId   = isset($_GET['service_id']) ? trim($_GET['service_id']) : '3';
$serviceType = isset($_GET['service_type']) ? trim($_GET['service_type']) : 'oil';

$response = [
    "success"      => true,
    "status"       => true,
    "service_id"   => $serviceId,
    "service_type" => $serviceType,
    "service_name" => "Deep Tissue Muscle Recovery Massage",
    "data"         => [
        [
            "id"              => $serviceId . "_60",
            "types"           => $serviceType,
            "name"            => "Deep Tissue Muscle Recovery Massage",
            "features"        => "Focused pressure on inner layers of muscles and connective tissues. Ideal for chronic aches, stiff neck, lower back tightness, and sore shoulders.",
            "image"           => "https://images.unsplash.com/photo-1519823551278-64ac92734fb1",
            "cover"           => "https://images.unsplash.com/photo-1519823551278-64ac92734fb1",
            "technique"       => "Slow, deliberate strokes using deep finger pressure and firm elbow friction.",
            "pressure"        => "Firm / High Pressure",
            "oil"             => "Warm Cold-Pressed Sesame & Eucalyptus Oil",
            "benefits"        => "Reduces chronic pain, releases tension knots, enhances mobility.",
            "recommended_for" => "Travel fatigue, sore muscles, post-workout recovery.",
            "duration"        => "60",
            "price"           => "2399",
            "discount_price"  => "1899",
            "discount"        => "20%"
        ],
        [
            "id"              => $serviceId . "_90",
            "types"           => $serviceType,
            "name"            => "Deep Tissue Muscle Recovery Massage (Extended)",
            "features"        => "Complete full body coverage with extra focus on tight back, hamstrings, and shoulders.",
            "image"           => "https://images.unsplash.com/photo-1519823551278-64ac92734fb1",
            "cover"           => "https://images.unsplash.com/photo-1519823551278-64ac92734fb1",
            "technique"       => "Firm pressure, trigger point therapy, and gentle stretching.",
            "pressure"        => "Firm / High Pressure",
            "oil"             => "Warm Cold-Pressed Sesame & Eucalyptus Oil",
            "benefits"        => "Deep nervous system relaxation, total muscular restoration.",
            "recommended_for" => "Severe body tension, executive stress.",
            "duration"        => "90",
            "price"           => "3199",
            "discount_price"  => "2599",
            "discount"        => "20%"
        ]
    ]
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
