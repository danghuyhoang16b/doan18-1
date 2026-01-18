package com.example.app.api;

import com.example.app.models.ChangePasswordRequest;
import com.example.app.models.CompetitionStatsResponse;
import com.example.app.models.ConductRule;
import com.example.app.models.LoginResponse;
import com.example.app.models.ParentLoginRequest;
import com.example.app.models.TeacherLoginRequest;
import com.example.app.models.StudentLoginRequest;
import com.example.app.models.PhoneRequest;
import com.example.app.models.VerifyOtpRequest;
import com.example.app.models.CaptchaResponse;
import com.example.app.models.AttendanceSubmitRequest;
import com.example.app.models.ClassModel;
import com.example.app.models.ClassRequest;
import com.example.app.models.Conduct;
import com.example.app.models.Contact;
import com.example.app.models.ConversationRequest;
import com.example.app.models.CreateNotificationRequest;
import com.example.app.models.Message;
import com.example.app.models.News;
import com.example.app.models.Notification;
import com.example.app.models.AdminBackgroundRequest;
import com.example.app.models.AdminBackgroundResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import com.example.app.models.ScheduleItem;
import com.example.app.models.Score;
import com.example.app.models.StatisticsResponse;
import com.example.app.models.SendMessageRequest;
import com.example.app.models.Student;
import com.example.app.models.TokenRequest;
import com.example.app.models.ViolationRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/login_teacher.php")
    Call<LoginResponse> loginTeacher(@Body TeacherLoginRequest body);

    @POST("auth/login_student.php")
    Call<LoginResponse> loginStudent(@Body StudentLoginRequest body);
    
    @POST("auth/login_parent_password.php")
    Call<LoginResponse> loginParentPassword(@Body ParentLoginRequest body);

    @POST("auth/request_otp.php")
    Call<LoginResponse> requestOtp(@Body PhoneRequest body);

    @POST("auth/verify_otp.php")
    Call<LoginResponse> verifyOtp(@Body VerifyOtpRequest body);

    @GET("captcha/get_challenge.php")
    Call<CaptchaResponse> getCaptcha();

    @GET("news/get_latest.php")
    Call<List<News>> getLatestNews();

    @GET("news/get_all.php")
    Call<List<News>> getAllNews();

    @GET("schedule/get_weekly.php")
    Call<List<ScheduleItem>> getWeeklySchedule(@Header("Authorization") String bearer, @Query("class_id") Integer classId);

    // Attendance APIs
    @GET("teacher/get_classes.php")
    Call<List<ClassModel>> getTeacherClasses(@Header("Authorization") String bearer);

    @GET("teacher/get_students.php")
    Call<List<Student>> getClassStudents(@Header("Authorization") String bearer, @Query("class_id") Integer classId);

    @POST("attendance/submit.php")
    Call<Void> submitAttendance(@Header("Authorization") String bearer, @Body AttendanceSubmitRequest body);

    // Report APIs
    @GET("reports/get_academic.php")
    Call<List<Score>> getAcademicReport(@Header("Authorization") String bearer);

    @GET("reports/get_conduct.php")
    Call<List<Conduct>> getConductReport(@Header("Authorization") String bearer);

    @GET("reports/get_statistics.php")
    Call<StatisticsResponse> getStatistics(@Header("Authorization") String bearer);

    // Message APIs
    @GET("messages/get_contacts.php")
    Call<List<Contact>> getContacts(@Header("Authorization") String bearer);

    @GET("messages/get_conversation.php")
    Call<List<Message>> getConversation(@Header("Authorization") String bearer, @Query("contact_id") Integer contactId);

    // Banner APIs
    @GET("banners/get_active.php")
    Call<List<com.example.app.models.Banner>> getActiveBanners();

    @Multipart
    @POST("banners/upload.php")
    Call<ResponseBody> uploadBanner(
        @Part("token") RequestBody token,
        @Part MultipartBody.Part image,
        @Part("title") RequestBody title,
        @Part("cta_text") RequestBody ctaText,
        @Part("link_url") RequestBody linkUrl
    );

    @Multipart
    @POST("banners/upload.php")
    Call<ResponseBody> uploadBannerV2(
            @Header("Authorization") String bearer,
            @Part("token") RequestBody token,
            @Part MultipartBody.Part image,
            @Part("title") RequestBody title,
            @Part("cta_text") RequestBody ctaText,
            @Part("link_url") RequestBody linkUrl,
            @Part("priority") RequestBody priority
    );

    @POST("banners/delete.php")
    Call<ResponseBody> deleteBanner(@Body com.example.app.models.BannerIdRequest body);

    @POST("banners/toggle_active.php")
    Call<ResponseBody> toggleBannerActive(@Header("Authorization") String bearer, @Body com.example.app.models.BannerToggleRequest body);

    @POST("messages/send.php")
    Call<Void> sendMessage(@Header("Authorization") String bearer, @Body SendMessageRequest body);

    // Notification APIs
    @POST("notifications/create.php")
    Call<Void> createNotification(@Header("Authorization") String bearer, @Body CreateNotificationRequest body);

    @GET("notifications/get_all.php")
    Call<List<Notification>> getAllNotifications(@Header("Authorization") String bearer);

    @POST("auth/change_password.php")
    Call<Void> changePassword(@Header("Authorization") String bearer, @Body ChangePasswordRequest body);

    // Discipline APIs
    @GET("violations/get_rules.php")
    Call<List<ConductRule>> getConductRules(@Header("Authorization") String bearer);

    @POST("violations/submit.php")
    Call<Void> submitViolation(@Header("Authorization") String bearer, @Body ViolationRequest body);

    @GET("reports/get_competition_stats.php")
    Call<CompetitionStatsResponse> getCompetitionStats(@Header("Authorization") String bearer);

    @GET("violations/get_points.php")
    Call<ResponseBody> getPoints(@Header("Authorization") String bearer, @Query("student_id") Integer studentId);

    @GET("violations/list_students_by_class.php")
    Call<ResponseBody> listStudentsByClass(@Header("Authorization") String bearer, @Query("class_id") Integer classId);

    // Admin APIs
    @POST("admin/set_background.php")
    Call<Void> setBackground(@Header("Authorization") String bearer, @Body AdminBackgroundRequest body);
    @GET("admin/get_background.php")
    Call<AdminBackgroundResponse> getBackground(@Header("Authorization") String bearer);
    @Multipart
    @POST("admin/upload_background.php")
    Call<ResponseBody> uploadBackground(@Header("Authorization") String bearer,
                                        @Part MultipartBody.Part image,
                                        @Part("target") RequestBody target,
                                        @Part("token") RequestBody token);

    @POST("admin/violations/set_thresholds.php")
    Call<ResponseBody> setDisciplineThresholds(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);
    @POST("admin/teacher/create.php")
    Call<ResponseBody> createTeacher(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);
    @POST("admin/schedule/set.php")
    Call<ResponseBody> setSchedule(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);
    @GET("schedule/get_by_teacher.php")
    Call<ResponseBody> getScheduleByTeacher(@Header("Authorization") String bearer);
    @POST("schedule/auto_generate.php")
    Call<ResponseBody> autoGenerateSchedule(@Header("Authorization") String bearer, @Body java.util.Map<String, String> body);
    @POST("admin/banner/set_content.php")
    Call<ResponseBody> setBannerContent(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);
    @GET("admin/backup.php")
    Call<ResponseBody> backupData(@Header("Authorization") String bearer);

    // Scores
    @GET("scores/list_students.php")
    Call<ResponseBody> listStudentsForScore(@Header("Authorization") String bearer, @Query("class_id") Integer classId);
    @POST("scores/submit.php")
    Call<ResponseBody> submitScores(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);
    @GET("classes/list_all.php")
    Call<ResponseBody> getAllClasses();
    @GET("subjects/list_all.php")
    Call<ResponseBody> getAllSubjects();
    @GET("classes/of_student.php")
    Call<ResponseBody> getStudentClass(@Header("Authorization") String bearer);
    @POST("conduct/submit_result.php")
    Call<ResponseBody> submitConductResults(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);
    @POST("teacher/request_classes.php")
    Call<ResponseBody> requestTeacherClasses(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);
    @GET("teacher/request_list.php")
    Call<ResponseBody> getTeacherRequests(@Header("Authorization") String bearer);
    @GET("admin/teacher_request/list.php")
    Call<ResponseBody> getAdminRequests(@Header("Authorization") String bearer);
    @POST("admin/teacher_request/approve.php")
    Call<ResponseBody> approveTeacherRequest(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);
    // Profile APIs
    @GET("profile/get.php")
    Call<com.example.app.models.ProfileResponse> getProfile(@Header("Authorization") String bearer);

    @POST("admin/import_image_db.php")
    Call<ResponseBody> importImage(@Header("Authorization") String bearer, @Body com.example.app.models.ImportImageRequest body);

    @Multipart
    @POST("profile/upload_avatar.php")
    Call<ResponseBody> uploadAvatar(@Header("Authorization") String bearer, @Part MultipartBody.Part image);

    @POST("profile/update.php")
    Call<ResponseBody> updateProfile(@Header("Authorization") String bearer, @Body com.example.app.models.UserUpdateRequest body);
    @POST("student/set_class.php")
    Call<ResponseBody> setStudentClass(@Header("Authorization") String bearer, @Body java.util.Map<String, Integer> body);
    @GET("profile/is_student_complete.php")
    Call<ResponseBody> isStudentComplete(@Header("Authorization") String bearer);

    @GET("profile/is_profile_complete.php")
    Call<ResponseBody> isProfileComplete(@Header("Authorization") String bearer);

    @POST("profile/update_student_full.php")
    Call<ResponseBody> updateStudentFull(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);

    @POST("admin/users/list.php")
    Call<ResponseBody> getUsers(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);
    @POST("admin/users/create.php")
    Call<ResponseBody> createUser(@Header("Authorization") String token, @Body com.example.app.models.User user);

    @POST("admin/users/update.php")
    Call<ResponseBody> updateUser(@Header("Authorization") String token, @Body com.example.app.models.User user);

    @GET("admin/users/get.php")
    Call<com.example.app.models.User> getUser(@Header("Authorization") String token, @Query("id") Integer id);
    @retrofit2.http.HTTP(method = "DELETE", path = "admin/users/delete.php", hasBody = true)
    Call<ResponseBody> deleteUser(@Header("Authorization") String bearer, @Body java.util.Map<String, Integer> body);

    @GET("admin/stats/violations.php")
    Call<List<com.example.app.models.StatItem>> getViolationStats(@Header("Authorization") String bearer, @Query("type") String type);

    @GET("admin/stats/violation_details.php")
    Call<ResponseBody> getViolationDetails(@Header("Authorization") String bearer, @Query("start_date") String startDate, @Query("end_date") String endDate);

    @GET("ranking/get_weekly.php")
    Call<ResponseBody> getWeeklyRanking(@Header("Authorization") String bearer, @Query("class_id") Integer classId, @Query("label") String label);
    @GET("ranking/get_monthly.php")
    Call<ResponseBody> getMonthlyRanking(@Header("Authorization") String bearer, @Query("class_id") Integer classId, @Query("label") String label);
    @GET("ranking/get_semester.php")
    Call<ResponseBody> getSemesterRanking(@Header("Authorization") String bearer, @Query("class_id") Integer classId, @Query("year") Integer year, @Query("semester") String semester);

    @GET("parent/list_children.php")
    Call<ResponseBody> listChildren(@Header("Authorization") String bearer);
    @GET("parent/get_child_ranking.php")
    Call<ResponseBody> getChildRanking(@Header("Authorization") String bearer, @Query("student_id") Integer studentId, @Query("type") String type, @Query("label") String label, @Query("year") Integer year, @Query("semester") String semester);
    @GET("parent/get_child_violations.php")
    Call<ResponseBody> getChildViolations(@Header("Authorization") String bearer, @Query("student_id") Integer studentId, @Query("type") String type, @Query("label") String label, @Query("year") Integer year, @Query("semester") String semester);

    // Security Challenge
    @GET("security/challenge.php")
    Call<ResponseBody> getActionChallenge(@Header("Authorization") String bearer);

    // Red Committee APIs
    @GET("red_committee/list.php")
    Call<ResponseBody> listRedCommittee(@Header("Authorization") String bearer, @Query("class_id") Integer classId, @Query("area") String area);
    @POST("red_committee/add.php")
    Call<ResponseBody> addRedCommittee(@Header("Authorization") String bearer, @Header("X-Action-Code") String code, @Header("X-Action-Ticket") String ticket, @Body java.util.Map<String, Object> body);
    @POST("red_committee/remove.php")
    Call<ResponseBody> removeRedCommittee(@Header("Authorization") String bearer, @Header("X-Action-Code") String code, @Header("X-Action-Ticket") String ticket, @Body java.util.Map<String, Object> body);
    @POST("red_committee/create_account.php")
    Call<ResponseBody> createRedStarAccount(@Header("Authorization") String bearer, @Header("X-Action-Code") String code, @Header("X-Action-Ticket") String ticket, @Body java.util.Map<String, Object> body);
    @GET("red_committee/logs.php")
    Call<ResponseBody> getRedCommitteeLogs(@Header("Authorization") String bearer, @Query("class_id") Integer classId);

    @POST("red_committee/update.php")
    Call<ResponseBody> updateRedCommittee(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);

    @POST("teacher/students/create.php")
    Call<ResponseBody> addStudentToClass(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);

    @POST("teacher/students/update.php")
    Call<ResponseBody> updateStudentInClass(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);

    @POST("teacher/students/delete.php")
    Call<ResponseBody> removeStudentFromClass(@Header("Authorization") String bearer, @Body java.util.Map<String, Object> body);
}
