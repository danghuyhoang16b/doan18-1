# Tài liệu Tích hợp API Backend - VPS

## 1. Thông tin chung
- **Base URL**: `http://103.252.136.73:8080/api/`
- **Image URL Base**: `http://103.252.136.73:8080/uploads/avatars/`
- **Server**: VPS (PHP Backend)

## 2. Các thay đổi đã thực hiện
### Cấu hình
- Đã cập nhật `RetrofitClient.java` để trỏ về địa chỉ VPS thay vì localhost/máy ảo.

### Frontend (Android)
- **LoginActivity**:
  - Thêm `ProgressDialog` (Loading state) khi đăng nhập.
  - Xử lý lỗi 4xx/5xx và hiển thị thông báo Toast phù hợp.
  - Xử lý Captcha và kiểm tra trạng thái hồ sơ học sinh.
- **HomeActivity**:
  - Cập nhật logic tải ảnh avatar từ server.
  - Thêm logging cho lỗi tải tin tức và lịch học.
- **StudentProfileActivity**:
  - Cập nhật URL tải ảnh avatar.
  - Thêm trường `is_red_star` vào `ProfileResponse`.
- **Adapters**:
  - `AdminUserAdapter` & `AttendanceAdapter`: Cập nhật đường dẫn base cho việc tải ảnh.

### Backend Verification
- Đã kiểm thử các endpoint chính thông qua script `backend/run_tests.py` và `backend/check_models.py`.
- Kết quả: Tất cả các endpoint hoạt động bình thường và trả về cấu trúc dữ liệu khớp với Model trong Android.

## 3. Hướng dẫn cho Developer
- **Thêm API mới**:
  1. Khai báo endpoint trong `ApiService.java`.
  2. Tạo Model tương ứng trong package `models`.
  3. Sử dụng `RetrofitClient.getClient().create(ApiService.class)` để gọi.
- **Lưu ý bảo mật**:
  - Token được lưu trong `SharedPrefsUtils`.
  - Luôn gửi kèm header `Authorization: Bearer <token>` cho các request yêu cầu xác thực.
- **Xử lý ảnh**:
  - Nếu ảnh trả về là đường dẫn tương đối (ví dụ `avatar.jpg`), cần ghép với Base URL ảnh.
  - Nếu ảnh là đường dẫn tuyệt đối (`http...`), sử dụng trực tiếp.

## 4. Danh sách API chính đang sử dụng
- `POST auth/login_teacher.php`: Đăng nhập giáo viên/admin.
- `POST auth/login_student.php`: Đăng nhập học sinh.
- `GET news/get_latest.php`: Lấy tin tức mới nhất.
- `GET banners/get_active.php`: Lấy banner quảng cáo.
- `POST profile/get.php`: Lấy thông tin cá nhân.
- `POST teacher/get_classes.php`: Lấy danh sách lớp (cho giáo viên).
