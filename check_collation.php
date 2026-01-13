<?php
include_once 'Backend/config/database.php';

$database = new Database();
$db = $database->getConnection();

$tables = ['users', 'student_profiles'];
foreach ($tables as $table) {
    $stmt = $db->query("SHOW TABLE STATUS LIKE '$table'");
    $status = $stmt->fetch(PDO::FETCH_ASSOC);
    echo "Table: $table, Collation: " . $status['Collation'] . "\n";
}

// Check specific columns
$stmt = $db->query("SHOW FULL COLUMNS FROM users WHERE Field = 'full_name'");
$col = $stmt->fetch(PDO::FETCH_ASSOC);
echo "Column users.full_name Collation: " . $col['Collation'] . "\n";

$stmt = $db->query("SHOW FULL COLUMNS FROM student_profiles WHERE Field = 'full_name'");
$col = $stmt->fetch(PDO::FETCH_ASSOC);
echo "Column student_profiles.full_name Collation: " . $col['Collation'] . "\n";
?>