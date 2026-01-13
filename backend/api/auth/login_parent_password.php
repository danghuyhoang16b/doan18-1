<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
include_once '../../config/database.php';
include_once '../../config/jwt.php';
$database = new Database();
$db = $database->getConnection();
$jwt = new JWT();
$ip = $_SERVER['REMOTE_ADDR'] ?? null;
$data = json_decode(file_get_contents("php://input"), true);
function log_attempt($db,$u,$s,$r,$ip){$q=$db->prepare("INSERT INTO login_attempts(username,success,reason,ip) VALUES(:u,:s,:r,:i)");$q->execute([':u'=>$u,':s'=>$s,':r'=>$r,':i'=>$ip]);}
if(!$data || !isset($data['student_code']) || !isset($data['password'])){echo json_encode(['message'=>'Thiếu dữ liệu']);http_response_code(400);exit;}
$studentCode = trim($data['student_code']);
$password = $data['password'];
if(!preg_match('/^HS-\d{5}$/',$studentCode)){echo json_encode(['message'=>'Mã học sinh không hợp lệ']);http_response_code(400);exit;}
$stu = $db->prepare("SELECT id FROM users WHERE username=:u AND role='student' LIMIT 1");
$stu->execute([':u'=>$studentCode]);
if($stu->rowCount()===0){log_attempt($db,$studentCode,0,'student_not_found',$ip);echo json_encode(['message'=>'Không tìm thấy học sinh']);http_response_code(401);exit;}
$student = $stu->fetch(PDO::FETCH_ASSOC);
$par = $db->prepare("SELECT u.id,u.username,u.password,u.full_name,u.role,u.phone_verified FROM parent_student_links p JOIN users u ON p.parent_id=u.id WHERE p.student_id=:sid AND u.role='parent' LIMIT 1");
$par->execute([':sid'=>$student['id']]);
if($par->rowCount()===0){log_attempt($db,$studentCode,0,'parent_not_found',$ip);echo json_encode(['message'=>'Không tìm thấy phụ huynh liên kết']);http_response_code(401);exit;}
$parent = $par->fetch(PDO::FETCH_ASSOC);
if(intval($parent['phone_verified'])!==1){log_attempt($db,$parent['username'],0,'phone_not_verified',$ip);echo json_encode(['message'=>'Số điện thoại chưa xác minh']);http_response_code(403);exit;}
if(!password_verify($password,$parent['password'])){log_attempt($db,$parent['username'],0,'wrong_password',$ip);echo json_encode(['message'=>'Sai mật khẩu phụ huynh']);http_response_code(401);exit;}
log_attempt($db,$parent['username'],1,'success',$ip);
$db->prepare("UPDATE users SET last_login=NOW() WHERE id=:id")->execute([':id'=>$parent['id']]);
$token = $jwt->encode(['sub'=>$parent['id'],'username'=>$parent['username'],'role'=>$parent['role']]);
echo json_encode(['message'=>'Đăng nhập thành công','token'=>$token,'user'=>['id'=>$parent['id'],'username'=>$parent['username'],'full_name'=>$parent['full_name'],'role'=>$parent['role']]]);
http_response_code(200);
?>
