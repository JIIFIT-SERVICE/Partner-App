<?php
header('Content-Type: application/json; charset=utf-8');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

if (isset($_SERVER['REQUEST_METHOD']) && $_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$query = isset($_GET['query']) ? trim($_GET['query']) : (isset($_POST['query']) ? trim($_POST['query']) : '');

// Minimum 2 characters required for search
if (empty($query) || mb_strlen($query, 'UTF-8') < 2) {
    echo json_encode([
        "status" => true,
        "cities" => []
    ]);
    exit();
}

// --------------------------------------------------------------------------
// 1. FAST FILE CACHE (Avoid hitting rate-limited external APIs repeatedly)
// --------------------------------------------------------------------------
$cacheKey = preg_replace('/[^a-zA-Z0-9_-]/', '', strtolower(trim($query)));
$cacheDir = sys_get_temp_dir();
$cacheFile = $cacheDir . DIRECTORY_SEPARATOR . 'jifiit_city_' . md5($cacheKey) . '.json';
$cacheTtl = 86400; // 24 hours

if (file_exists($cacheFile) && (time() - filemtime($cacheFile) < $cacheTtl)) {
    $cachedContent = @file_get_contents($cacheFile);
    if (!empty($cachedContent)) {
        $cachedJson = @json_decode($cachedContent, true);
        if (isset($cachedJson['cities']) && !empty($cachedJson['cities'])) {
            echo $cachedContent;
            exit();
        }
    }
}

$cleanQuery = urlencode($query);
$qLower = mb_strtolower($query, 'UTF-8');
$results = [];
$seen = [];

// Helper to add city uniquely
$addCity = function($cityName, $countryName, $displayName = '') use (&$results, &$seen) {
    $cityName = trim($cityName);
    $countryName = trim($countryName);
    if (empty($cityName)) return;

    if (empty($displayName)) {
        $displayName = !empty($countryName) ? "$cityName, $countryName" : $cityName;
    }
    $key = mb_strtolower($displayName, 'UTF-8');
    if (!isset($seen[$key])) {
        $seen[$key] = true;
        $results[] = [
            "city_name"    => $cityName,
            "country_name" => $countryName,
            "display_name" => $displayName
        ];
    }
};

// --------------------------------------------------------------------------
// 2. CURATED COMPREHENSIVE CITY DICTIONARY (250+ Indian Cities + Top World Cities)
// Guaranteed instant match in <1ms without any network dependency or rate-limits
// --------------------------------------------------------------------------
$builtInCities = [
    // --- India: Tier 1, State Capitals, Union Territories & Major Hubs ---
    ["Delhi", "India"], ["New Delhi", "India"], ["Mumbai", "India"], ["Kolkata", "India"],
    ["Bengaluru", "India"], ["Bangalore", "India"], ["Hyderabad", "India"], ["Chennai", "India"],
    ["Ahmedabad", "India"], ["Pune", "India"], ["Surat", "India"], ["Jaipur", "India"],
    ["Lucknow", "India"], ["Kanpur", "India"], ["Nagpur", "India"], ["Indore", "India"],
    ["Thane", "India"], ["Bhopal", "India"], ["Visakhapatnam", "India"], ["Pimpri-Chinchwad", "India"],
    ["Patna", "India"], ["Vadodara", "India"], ["Ghaziabad", "India"], ["Ludhiana", "India"],
    ["Agra", "India"], ["Nashik", "India"], ["Faridabad", "India"], ["Meerut", "India"],
    ["Rajkot", "India"], ["Kalyan", "India"], ["Varanasi", "India"], ["Srinagar", "India"],
    ["Aurangabad", "India"], ["Chhatrapati Sambhajinagar", "India"], ["Dhanbad", "India"],
    ["Amritsar", "India"], ["Navi Mumbai", "India"], ["Prayagraj", "India"], ["Allahabad", "India"],
    ["Howrah", "India"], ["Ranchi", "India"], ["Gwalior", "India"], ["Jabalpur", "India"],
    ["Coimbatore", "India"], ["Vijayawada", "India"], ["Jodhpur", "India"], ["Madurai", "India"],
    ["Raipur", "India"], ["Kota", "India"], ["Chandigarh", "India"], ["Guwahati", "India"],
    ["Solapur", "India"], ["Hubli", "India"], ["Dharwad", "India"], ["Bareilly", "India"],
    ["Moradabad", "India"], ["Mysore", "India"], ["Mysuru", "India"], ["Gurgaon", "India"],
    ["Gurugram", "India"], ["Noida", "India"], ["Greater Noida", "India"], ["Aligarh", "India"],
    ["Jalandhar", "India"], ["Tiruchirappalli", "India"], ["Trichy", "India"], ["Bhubaneswar", "India"],
    ["Salem", "India"], ["Mira-Bhayandar", "India"], ["Warangal", "India"], ["Thiruvananthapuram", "India"],
    ["Trivandrum", "India"], ["Bhiwandi", "India"], ["Saharanpur", "India"], ["Guntur", "India"],
    ["Amravati", "India"], ["Bikaner", "India"], ["Jamshedpur", "India"], ["Bhilai", "India"],
    ["Cuttack", "India"], ["Firozabad", "India"], ["Kochi", "India"], ["Cochin", "India"],
    ["Nellore", "India"], ["Bhavnagar", "India"], ["Dehradun", "India"], ["Durgapur", "India"],
    ["Asansol", "India"], ["Rourkela", "India"], ["Nanded", "India"], ["Kolhapur", "India"],
    ["Ajmer", "India"], ["Akola", "India"], ["Gulbarga", "India"], ["Kalaburagi", "India"],
    ["Jamnagar", "India"], ["Ujjain", "India"], ["Loni", "India"], ["Siliguri", "India"],
    ["Jhansi", "India"], ["Ulhasnagar", "India"], ["Jammu", "India"], ["Sangli", "India"],
    ["Mangalore", "India"], ["Mangaluru", "India"], ["Erode", "India"], ["Belgaum", "India"],
    ["Belagavi", "India"], ["Ambattur", "India"], ["Tirunelveli", "India"], ["Malegaon", "India"],
    ["Gaya", "India"], ["Udaipur", "India"], ["Kakinada", "India"], ["Davanagere", "India"],
    ["Kozhikode", "India"], ["Calicut", "India"], ["Maheshtala", "India"], ["Rajpur Sonarpur", "India"],
    ["Rajahmundry", "India"], ["Bokaro", "India"], ["South Dumdum", "India"], ["Bellary", "India"],
    ["Patiala", "India"], ["Agartala", "India"], ["Bhagalpur", "India"], ["Muzaffarnagar", "India"],
    ["Bhatpara", "India"], ["Panihati", "India"], ["Latur", "India"], ["Dhule", "India"],
    ["Rohtak", "India"], ["Korba", "India"], ["Bhilwara", "India"], ["Brahmapur", "India"],
    ["Muzaffarpur", "India"], ["Ahmednagar", "India"], ["Ahilya Nagar", "India"], ["Mathura", "India"],
    ["Kollam", "India"], ["Avadi", "India"], ["Kadapa", "India"], ["Kamarhati", "India"],
    ["Sambalpur", "India"], ["Bilaspur", "India"], ["Shahjahanpur", "India"], ["Satara", "India"],
    ["Bijapur", "India"], ["Vijayapura", "India"], ["Rampur", "India"], ["Shimoga", "India"],
    ["Shivamogga", "India"], ["Chandrapur", "India"], ["Junagadh", "India"], ["Thrissur", "India"],
    ["Alwar", "India"], ["Bardhaman", "India"], ["Kulti", "India"], ["Nizamabad", "India"],
    ["Parbhani", "India"], ["Tumkur", "India"], ["Tumakuru", "India"], ["Khammam", "India"],
    ["Ozhukarai", "India"], ["Bihar Sharif", "India"], ["Panipat", "India"], ["Darbhanga", "India"],
    ["Bally", "India"], ["Aizawl", "India"], ["Dewas", "India"], ["Ichalkaranji", "India"],
    ["Karnal", "India"], ["Bathinda", "India"], ["Jalna", "India"], ["Eluru", "India"],
    ["Barasat", "India"], ["Purnia", "India"], ["Satna", "India"], ["Mau", "India"],
    ["Sonipat", "India"], ["Farrukhabad", "India"], ["Sagar", "India"], ["Durg", "India"],
    ["Imphal", "India"], ["Ratlam", "India"], ["Hapur", "India"], ["Arrah", "India"],
    ["Karimnagar", "India"], ["Anantapur", "India"], ["Etawah", "India"], ["Ambernath", "India"],
    ["North Dumdum", "India"], ["Bharatpur", "India"], ["Begusarai", "India"], ["Gandhidham", "India"],
    ["Baranagar", "India"], ["Tiruvottiyur", "India"], ["Puducherry", "India"], ["Pondicherry", "India"],
    ["Sikar", "India"], ["Thoothukudi", "India"], ["Tuticorin", "India"], ["Rewa", "India"],
    ["Mirzapur", "India"], ["Raichur", "India"], ["Pali", "India"], ["Ramagundam", "India"],
    ["Haridwar", "India"], ["Vijayanagaram", "India"], ["Katihar", "India"], ["Nagercoil", "India"],
    ["Sri Ganganagar", "India"], ["Karawal Nagar", "India"], ["Mango", "India"], ["Thanjavur", "India"],
    ["Bulandshahr", "India"], ["Uluberia", "India"], ["Katni", "India"], ["Sambhal", "India"],
    ["Singrauli", "India"], ["Nadiad", "India"], ["Secunderabad", "India"], ["Naihati", "India"],
    ["Yamunanagar", "India"], ["Bidhannagar", "India"], ["Pallavaram", "India"], ["Bidar", "India"],
    ["Munger", "India"], ["Panchkula", "India"], ["Burhanpur", "India"], ["Kharagpur", "India"],
    ["Dindigul", "India"], ["Gandhinagar", "India"], ["Hospet", "India"], ["Hosapete", "India"],
    ["Malda", "India"], ["Ongole", "India"], ["Deoghar", "India"], ["Chapra", "India"],
    ["Haldia", "India"], ["Khandwa", "India"], ["Nandyal", "India"], ["Morena", "India"],
    ["Amroha", "India"], ["Anand", "India"], ["Bhind", "India"], ["Bhiwani", "India"],
    ["Navsari", "India"], ["Mahbubnagar", "India"], ["Shimla", "India"], ["Gangtok", "India"],
    ["Itanagar", "India"], ["Kohima", "India"], ["Shillong", "India"], ["Port Blair", "India"],
    ["Panaji", "India"], ["Margao", "India"], ["Vasco da Gama", "India"], ["Silvassa", "India"],
    ["Daman", "India"], ["Diu", "India"], ["Leh", "India"], ["Kavaratti", "India"],

    // --- Middle East & Gulf ---
    ["Dubai", "United Arab Emirates"], ["Abu Dhabi", "United Arab Emirates"], ["Sharjah", "United Arab Emirates"],
    ["Ajman", "United Arab Emirates"], ["Ras Al Khaimah", "United Arab Emirates"], ["Doha", "Qatar"],
    ["Riyadh", "Saudi Arabia"], ["Jeddah", "Saudi Arabia"], ["Mecca", "Saudi Arabia"], ["Medina", "Saudi Arabia"],
    ["Dammam", "Saudi Arabia"], ["Khobar", "Saudi Arabia"], ["Muscat", "Oman"], ["Kuwait City", "Kuwait"],
    ["Manama", "Bahrain"], ["Amman", "Jordan"], ["Beirut", "Lebanon"],

    // --- United Kingdom & Europe ---
    ["London", "United Kingdom"], ["Birmingham", "United Kingdom"], ["Manchester", "United Kingdom"],
    ["Glasgow", "United Kingdom"], ["Edinburgh", "United Kingdom"], ["Liverpool", "United Kingdom"],
    ["Bristol", "United Kingdom"], ["Leeds", "United Kingdom"], ["Dublin", "Ireland"],
    ["Paris", "France"], ["Marseille", "France"], ["Lyon", "France"], ["Toulouse", "France"], ["Nice", "France"],
    ["Berlin", "Germany"], ["Munich", "Germany"], ["Frankfurt", "Germany"], ["Hamburg", "Germany"],
    ["Cologne", "Germany"], ["Stuttgart", "Germany"], ["Dusseldorf", "Germany"],
    ["Rome", "Italy"], ["Milan", "Italy"], ["Naples", "Italy"], ["Turin", "Italy"], ["Florence", "Italy"], ["Venice", "Italy"],
    ["Madrid", "Spain"], ["Barcelona", "Spain"], ["Valencia", "Spain"], ["Seville", "Spain"],
    ["Lisbon", "Portugal"], ["Porto", "Portugal"],
    ["Amsterdam", "Netherlands"], ["Rotterdam", "Netherlands"], ["The Hague", "Netherlands"],
    ["Brussels", "Belgium"], ["Antwerp", "Belgium"],
    ["Vienna", "Austria"], ["Salzburg", "Austria"],
    ["Zurich", "Switzerland"], ["Geneva", "Switzerland"], ["Basel", "Switzerland"],
    ["Warsaw", "Poland"], ["Krakow", "Poland"], ["Prague", "Czechia"], ["Budapest", "Hungary"],
    ["Athens", "Greece"], ["Istanbul", "Turkey"], ["Ankara", "Turkey"],
    ["Moscow", "Russia"], ["Saint Petersburg", "Russia"],

    // --- North America ---
    ["New York", "United States"], ["Los Angeles", "United States"], ["Chicago", "United States"],
    ["Houston", "United States"], ["Phoenix", "United States"], ["Philadelphia", "United States"],
    ["San Antonio", "United States"], ["San Diego", "United States"], ["Dallas", "United States"],
    ["San Jose", "United States"], ["Austin", "United States"], ["San Francisco", "United States"],
    ["Seattle", "United States"], ["Denver", "United States"], ["Washington", "United States"],
    ["Boston", "United States"], ["Las Vegas", "United States"], ["Miami", "United States"],
    ["Atlanta", "United States"], ["Toronto", "Canada"], ["Montreal", "Canada"], ["Vancouver", "Canada"],
    ["Calgary", "Canada"], ["Ottawa", "Canada"], ["Edmonton", "Canada"],
    ["Mexico City", "Mexico"], ["Guadalajara", "Mexico"], ["Monterrey", "Mexico"], ["Cancun", "Mexico"],

    // --- Asia-Pacific & Australasia ---
    ["Singapore", "Singapore"], ["Kuala Lumpur", "Malaysia"], ["George Town", "Malaysia"],
    ["Bangkok", "Thailand"], ["Chiang Mai", "Thailand"], ["Phuket", "Thailand"],
    ["Jakarta", "Indonesia"], ["Bali", "Indonesia"], ["Surabaya", "Indonesia"],
    ["Manila", "Philippines"], ["Cebu", "Philippines"],
    ["Ho Chi Minh City", "Vietnam"], ["Hanoi", "Vietnam"], ["Da Nang", "Vietnam"],
    ["Tokyo", "Japan"], ["Osaka", "Japan"], ["Yokohama", "Japan"], ["Kyoto", "Japan"], ["Nagoya", "Japan"],
    ["Seoul", "South Korea"], ["Busan", "South Korea"], ["Incheon", "South Korea"],
    ["Hong Kong", "Hong Kong"], ["Taipei", "Taiwan"],
    ["Beijing", "China"], ["Shanghai", "China"], ["Guangzhou", "China"], ["Shenzhen", "China"],
    ["Sydney", "Australia"], ["Melbourne", "Australia"], ["Brisbane", "Australia"], ["Perth", "Australia"],
    ["Adelaide", "Australia"], ["Auckland", "New Zealand"], ["Wellington", "New Zealand"],
    ["Colombo", "Sri Lanka"], ["Dhaka", "Bangladesh"], ["Kathmandu", "Nepal"],

    // --- Africa & South America ---
    ["Cairo", "Egypt"], ["Johannesburg", "South Africa"], ["Cape Town", "South Africa"], ["Nairobi", "Kenya"],
    ["Lagos", "Nigeria"], ["Casablanca", "Morocco"],
    ["Sao Paulo", "Brazil"], ["Rio de Janeiro", "Brazil"], ["Buenos Aires", "Argentina"],
    ["Santiago", "Chile"], ["Bogota", "Colombia"], ["Lima", "Peru"]
];

// Match exact prefix first, then general substring in built-in dictionary
$prefixMatches = [];
$subMatches = [];
foreach ($builtInCities as $c) {
    $cName = $c[0];
    $cCountry = $c[1];
    $cDisp = "$cName, $cCountry";
    $cNameLower = mb_strtolower($cName, 'UTF-8');
    $cDispLower = mb_strtolower($cDisp, 'UTF-8');

    if (strpos($cNameLower, $qLower) === 0) {
        $prefixMatches[] = [$cName, $cCountry, $cDisp];
    } elseif (strpos($cDispLower, $qLower) !== false) {
        $subMatches[] = [$cName, $cCountry, $cDisp];
    }
}

foreach ($prefixMatches as $item) {
    $addCity($item[0], $item[1], $item[2]);
    if (count($results) >= 15) break;
}
if (count($results) < 15) {
    foreach ($subMatches as $item) {
        $addCity($item[0], $item[1], $item[2]);
        if (count($results) >= 15) break;
    }
}

// --------------------------------------------------------------------------
// 3. DATABASE MATCHES (Registered Therapists living_city_name & city table)
// --------------------------------------------------------------------------
try {
    @include_once __DIR__ . "/../connection.php";
    $db = isset($con) ? $con : (isset($conn) ? $conn : null);
    if ($db && ($db instanceof mysqli)) {
        $escaped = mysqli_real_escape_string($db, $query);

        // Fetch distinct registered living cities
        $dbRes = @mysqli_query($db, "
            SELECT DISTINCT living_city_name 
            FROM therapists 
            WHERE living_city_name LIKE '%$escaped%' 
              AND living_city_name IS NOT NULL 
              AND TRIM(living_city_name) != '' 
            LIMIT 10
        ");
        if ($dbRes) {
            while ($row = mysqli_fetch_assoc($dbRes)) {
                $raw = trim($row['living_city_name']);
                if (!empty($raw)) {
                    $parts = explode(',', $raw);
                    $cName = trim($parts[0]);
                    $country = count($parts) > 1 ? trim($parts[1]) : 'India';
                    $addCity($cName, $country, $raw);
                }
            }
            @mysqli_free_result($dbRes);
        }

        // Fetch from legacy city / cities table if present
        $dbCityRes = @mysqli_query($db, "
            SELECT name FROM city WHERE name LIKE '%$escaped%' LIMIT 10
        ");
        if ($dbCityRes) {
            while ($cRow = mysqli_fetch_assoc($dbCityRes)) {
                $cName = trim($cRow['name']);
                if (!empty($cName)) {
                    $addCity($cName, 'India', "$cName, India");
                }
            }
            @mysqli_free_result($dbCityRes);
        }
    }
} catch (Throwable $e) {
    // Gracefully ignore database connection issues; standalone mode continues
}

// --------------------------------------------------------------------------
// 4. EXTERNAL API GEOCODING (Photon API with English Language & Short Timeout)
// --------------------------------------------------------------------------
function fetchHttpSafe($url, $timeout = 2.5) {
    if (!function_exists('curl_init')) {
        $ctx = stream_context_create([
            'http' => [
                'timeout' => $timeout,
                'header'  => "User-Agent: JifiitApp-CitySearch/2.0 (contact@jiifit.com)\r\n"
            ]
        ]);
        return @file_get_contents($url, false, $ctx);
    }
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_USERAGENT, 'JifiitApp-CitySearch/2.0 (contact@jiifit.com)');
    curl_setopt($ch, CURLOPT_TIMEOUT, $timeout);
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 2);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    return ($httpCode >= 200 && $httpCode < 300) ? $response : false;
}

// Query Photon API (worldwide OSM with English language support)
$photonUrl = "https://photon.komoot.io/api/?q=" . $cleanQuery . "&lang=en&osm_tag=place:city&osm_tag=place:town&osm_tag=place:municipality&limit=15";
$photonResponse = fetchHttpSafe($photonUrl, 2.5);

if ($photonResponse) {
    $data = @json_decode($photonResponse, true);
    if (isset($data['features']) && is_array($data['features'])) {
        foreach ($data['features'] as $f) {
            $p = isset($f['properties']) ? $f['properties'] : [];
            $cityName = isset($p['name']) ? trim($p['name']) : '';
            $countryName = isset($p['country']) ? trim($p['country']) : '';
            if (empty($cityName)) continue;

            $displayName = !empty($countryName) ? "$cityName, $countryName" : $cityName;
            $addCity($cityName, $countryName, $displayName);
            if (count($results) >= 20) break;
        }
    }
}

// --------------------------------------------------------------------------
// 5. SECONDARY EXTERNAL FALLBACK: OpenStreetMap Nominatim
// --------------------------------------------------------------------------
if (count($results) < 5) {
    $nomUrl = "https://nominatim.openstreetmap.org/search?q=" . $cleanQuery . "&format=json&addressdetails=1&featuretype=city&limit=10&accept-language=en";
    $nomResponse = fetchHttpSafe($nomUrl, 2.0);
    if ($nomResponse) {
        $nomData = @json_decode($nomResponse, true);
        if (is_array($nomData)) {
            foreach ($nomData as $item) {
                $addr = isset($item['address']) ? $item['address'] : [];
                $cityName = !empty($addr['city']) ? $addr['city'] : (!empty($addr['town']) ? $addr['town'] : (!empty($addr['municipality']) ? $addr['municipality'] : (!empty($item['name']) ? $item['name'] : '')));
                $countryName = !empty($addr['country']) ? $addr['country'] : '';
                if (empty($cityName)) continue;

                $displayName = !empty($countryName) ? "$cityName, $countryName" : $cityName;
                $addCity($cityName, $countryName, $displayName);
                if (count($results) >= 20) break;
            }
        }
    }
}

// Limit maximum suggestions to 15 cleanest results
if (count($results) > 15) {
    $results = array_slice($results, 0, 15);
}

$payload = json_encode([
    "status" => true,
    "cities" => $results
], JSON_UNESCAPED_UNICODE);

// Write to cache for fast future retrieval
if (!empty($results)) {
    @file_put_contents($cacheFile, $payload, LOCK_EX);
}

echo $payload;
?>
