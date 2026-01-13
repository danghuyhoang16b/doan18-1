Tôi đã tìm ra nguyên nhân và có kế hoạch khắc phục toàn diện.

**Nguyên nhân lỗi:**
1.  **Chặn quyền truy cập (Backend):** API `api/teacher/get_students.php` hiện chỉ cho phép tài khoản có quyền `teacher` truy cập. Khi Admin gọi API này, hệ thống trả về lỗi 401 (Unauthorized), dẫn đến thông báo "Không thể tải danh sách học sinh".
2.  **Chặn quyền nộp điểm danh (Backend):** Tương tự, API `api/attendance/submit.php` cũng chặn Admin, sẽ gây lỗi khi Admin nhấn "Lưu".

**Kế hoạch sửa chữa:**

1.  **Sửa Backend (PHP):**
    *   Cập nhật `api/teacher/get_students.php`: Cho phép cả `admin` và `teacher` truy cập.
    *   Cập nhật `api/attendance/submit.php`: Cho phép `admin` thực hiện điểm danh.

2.  **Cải thiện Frontend (Android - `AttendanceActivity.java`):**
    *   **Thêm Logging:** Ghi log chi tiết khi gọi API để dễ dàng debug (theo yêu cầu).
    *   **Cải thiện thông báo lỗi:** Thay vì thông báo chung chung "Không thể tải...", sẽ hiển thị mã lỗi cụ thể (ví dụ: "Lỗi 401: Truy cập bị từ chối") để người dùng/admin hiểu rõ vấn đề.
    *   **Hiển thị trạng thái:** (Tùy chọn) Thêm thông báo "Đang tải..." nếu cần thiết, nhưng với sửa lỗi backend thì dữ liệu sẽ tải nhanh.

**Chi tiết thay đổi Code:**

*   **`api/teacher/get_students.php` & `api/attendance/submit.php`:**
    ```php
    // Trước:
    if (!$decoded || $decoded['data']->role != 'teacher') { ... }
    
    // Sau:
    if (!$decoded || ($decoded['data']->role != 'teacher' && $decoded['data']->role != 'admin')) { ... }
    ```

*   **`AttendanceActivity.java`:**
    ```java
    // Trong onFailure hoặc else của onResponse
    Toast.makeText(this, "Lỗi tải danh sách: " + response.code(), Toast.LENGTH_SHORT).show();
    Log.e("Attendance", "Error loading students: " + response.message());
    ```

Tôi sẽ thực hiện các thay đổi này ngay lập tức.