package com.example.app.network;

/**
 * API Configuration Constants
 * Centralized configuration for all network-related settings
 */
public final class ApiConstants {

    private ApiConstants() {
        // Private constructor to prevent instantiation
    }

    // ==================== BASE URL ====================
    /**
     * Production server URL
     */
    public static final String BASE_URL = "http://103.252.136.73:8080/api/";

    /**
     * Emulator URL (use this for Android Emulator testing)
     */
    public static final String BASE_URL_EMULATOR = "http://10.0.2.2/Backend/api/";

    // ==================== TIMEOUTS ====================
    /**
     * Connection timeout in seconds
     */
    public static final int CONNECT_TIMEOUT = 30;

    /**
     * Read timeout in seconds
     */
    public static final int READ_TIMEOUT = 30;

    /**
     * Write timeout in seconds
     */
    public static final int WRITE_TIMEOUT = 30;

    // ==================== HEADERS ====================
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACTION_CODE = "X-Action-Code";
    public static final String HEADER_ACTION_TICKET = "X-Action-Ticket";

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String BEARER_PREFIX = "Bearer ";

    // ==================== API ENDPOINTS ====================
    // Auth
    public static final String AUTH_LOGIN_TEACHER = "auth/login_teacher.php";
    public static final String AUTH_LOGIN_STUDENT = "auth/login_student.php";
    public static final String AUTH_LOGIN_PARENT = "auth/login_parent_password.php";
    public static final String AUTH_CHANGE_PASSWORD = "auth/change_password.php";
    public static final String AUTH_REQUEST_OTP = "auth/request_otp.php";
    public static final String AUTH_VERIFY_OTP = "auth/verify_otp.php";

    // Profile
    public static final String PROFILE_GET = "profile/get.php";
    public static final String PROFILE_UPDATE = "profile/update.php";
    public static final String PROFILE_UPLOAD_AVATAR = "profile/upload_avatar.php";
    public static final String PROFILE_IS_COMPLETE = "profile/is_profile_complete.php";
    public static final String PROFILE_IS_STUDENT_COMPLETE = "profile/is_student_complete.php";

    // Teacher
    public static final String TEACHER_GET_CLASSES = "teacher/get_classes.php";
    public static final String TEACHER_GET_STUDENTS = "teacher/get_students.php";
    public static final String TEACHER_REQUEST_CLASSES = "teacher/request_classes.php";

    // Admin Stats
    public static final String ADMIN_STATS_VIOLATIONS = "admin/stats/violations.php";
    public static final String ADMIN_STATS_VIOLATION_DETAILS = "admin/stats/violation_details.php";

    // Red Committee
    public static final String RED_COMMITTEE_LIST = "red_committee/list.php";
    public static final String RED_COMMITTEE_ADD = "red_committee/add.php";
    public static final String RED_COMMITTEE_REMOVE = "red_committee/remove.php";
    public static final String RED_COMMITTEE_CREATE_ACCOUNT = "red_committee/create_account.php";
    public static final String RED_COMMITTEE_LOGS = "red_committee/logs.php";

    // ==================== ERROR CODES ====================
    public static final int HTTP_OK = 200;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_PRECONDITION_REQUIRED = 428;
    public static final int HTTP_SERVER_ERROR = 500;
}
