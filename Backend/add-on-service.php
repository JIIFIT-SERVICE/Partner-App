<?php
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$response = [
    "success" => true,
    "status"  => true,
    "message" => "Add-on services retrieved successfully",
    "data"    => [
        [
            "id"             => "addon_1",
            "name"           => "Aromatherapy Lavender Essential Oil",
            "duration"       => "0",
            "price"          => "299",
            "discount_price" => "249",
            "image"          => "https://images.unsplash.com/photo-1608571423902-eed4a5ad8108",
            "description"    => "Pure calming lavender oil soothing senses and stress."
        ],
        [
            "id"             => "addon_2",
            "name"           => "Hot Herbal Compress Therapy",
            "duration"       => "15",
            "price"          => "499",
            "discount_price" => "399",
            "image"          => "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881",
            "description"    => "Warm steamed herbal pouches applied to release deep seated tension."
        ],
        [
            "id"             => "addon_3",
            "name"           => "Hydrating Foot Scrub & Soak",
            "duration"       => "15",
            "price"          => "399",
            "discount_price" => "299",
            "image"          => "https://images.unsplash.com/photo-1519415510236-718bdfcd89c8",
            "description"    => "Exfoliating sea-salt and peppermint foot soak."
        ]
    ]
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
