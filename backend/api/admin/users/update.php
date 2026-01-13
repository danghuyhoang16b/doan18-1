<?php
include_once '../../../config/database.php';
include_once '../../../config/jwt.php';
include_once '../../../lib/UserRepository.php';

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

$database = new Database();
$db = $database->getConnection();
$repo = new UserRepository($db);

$data = json_decode(file_get_contents("php://input"));
$headers = getallheaders();
$token = isset($headers['Authorization']) ? str_replace('Bearer ', '', $headers['Authorization']) : "";

// Fallback if header is missing but passed in body (rarely used but possible)
if (empty($token) && isset($data->token)) {
    $token = $data->token;
}

$decoded = validateJWT($token);
if (!$decoded || $decoded['data']->role != 'admin') {
    http_response_code(401);
    echo json_encode(["message" => "Unauthorized access."]);
    exit;
}

if (empty($data->id)) {
    http_response_code(400);
    echo json_encode(["message" => "User ID required."]);
    exit;
}

$fields = [
    "full_name" => isset($data->full_name) ? $data->full_name : null,
    "email" => isset($data->email) ? $data->email : null,
    "phone" => isset($data->phone) ? $data->phone : null,
    "role" => isset($data->role) ? $data->role : null
];
if (!empty($data->password)) { $fields["password"] = $data->password; }
if ($repo->updateUser(intval($data->id), $fields)) {
    echo json_encode(["message" => "User updated successfully."]);
} else {
    http_response_code(503);
    echo json_encode(["message" => "Unable to update user."]);
}
?>
