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
    "status"  => true,
    "success" => true,
    "message" => "Services fetched successfully",
    "sections" => [
        [
            "title"    => "Dry Massage (Oil-Free)",
            "subtitle" => "Fully clothed therapies focusing on acupressure and body flexibility",
            "items"    => [
                [
                    "id"              => "1",
                    "title"           => "Thai Traditional Body Massage",
                    "image"           => "https://images.unsplash.com/photo-1600334129128-685c5582fd35",
                    "saveBadge"       => "15% OFF",
                    "optionsCount"    => 2,
                    "price"           => "1499",
                    "originalPrice"   => "1799",
                    "discountPercent" => "15%",
                    "duration"        => "60 mins",
                    "type"            => "dry"
                ],
                [
                    "id"              => "2",
                    "title"           => "Head, Neck & Shoulder Relief",
                    "image"           => "https://images.unsplash.com/photo-1544161515-4ab6ce6db874",
                    "saveBadge"       => "Popular",
                    "optionsCount"    => 2,
                    "price"           => "999",
                    "originalPrice"   => "1199",
                    "discountPercent" => "10%",
                    "duration"        => "45 mins",
                    "type"            => "dry"
                ]
            ]
        ],
        [
            "title"    => "Oil Massage & Deep Rejuvenation",
            "subtitle" => "Warm herbal oils applied with rhythmic flowing pressure",
            "items"    => [
                [
                    "id"              => "3",
                    "title"           => "Deep Tissue Muscle Recovery Massage",
                    "image"           => "https://images.unsplash.com/photo-1519823551278-64ac92734fb1",
                    "saveBadge"       => "Top Rated",
                    "optionsCount"    => 2,
                    "price"           => "1899",
                    "originalPrice"   => "2399",
                    "discountPercent" => "20%",
                    "duration"        => "60 mins",
                    "type"            => "oil"
                ],
                [
                    "id"              => "4",
                    "title"           => "Swedish Gentle Relaxation Massage",
                    "image"           => "https://images.unsplash.com/photo-1515377905703-c4788e51af15",
                    "saveBadge"       => "Relaxing",
                    "optionsCount"    => 2,
                    "price"           => "1699",
                    "originalPrice"   => "1999",
                    "discountPercent" => "15%",
                    "duration"        => "60 mins",
                    "type"            => "oil"
                ]
            ]
        ],
        [
            "title"    => "Signature Packages",
            "subtitle" => "Comprehensive wellness combinations for ultimate relaxation",
            "items"    => [
                [
                    "id"              => "5",
                    "title"           => "Royal Rejuvenation Combo (Body + Reflexology)",
                    "image"           => "https://images.unsplash.com/photo-1540555700478-4be289fbecef",
                    "saveBadge"       => "Best Value",
                    "optionsCount"    => 1,
                    "price"           => "2699",
                    "originalPrice"   => "3499",
                    "discountPercent" => "25%",
                    "duration"        => "90 mins",
                    "type"            => "package"
                ]
            ]
        ]
    ]
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
exit();
