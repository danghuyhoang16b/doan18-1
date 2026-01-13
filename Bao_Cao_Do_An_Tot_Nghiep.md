# BÁO CÁO ĐỒ ÁN TỐT NGHIỆP
**ĐỀ TÀI: XÂY DỰNG ỨNG DỤNG QUẢN LÝ HỌC SINH TRUNG HỌC PHỔ THÔNG TRÊN NỀN TẢNG ANDROID**

---

## MỤC LỤC

1.  [CHƯƠNG 1: GIỚI THIỆU TỔNG QUAN](#chuong-1)
2.  [CHƯƠNG 2: PHÂN TÍCH YÊU CẦU VÀ THIẾT KẾ HỆ THỐNG](#chuong-2)
3.  [CHƯƠNG 3: KIẾN TRÚC VÀ CÔNG NGHỆ SỬ DỤNG](#chuong-3)
4.  [CHƯƠNG 4: CÀI ĐẶT VÀ TRIỂN KHAI](#chuong-4)
5.  [CHƯƠNG 5: KẾT QUẢ THỬ NGHIỆM](#chuong-5)
6.  [CHƯƠNG 6: ĐÁNH GIÁ VÀ HƯỚNG PHÁT TRIỂN](#chuong-6)
7.  [TÀI LIỆU THAM KHẢO](#tai-lieu-tham-khao)

---

<a name="chuong-1"></a>
## CHƯƠNG 1: GIỚI THIỆU TỔNG QUAN

### 1.1. Đặt vấn đề
Trong bối cảnh chuyển đổi số giáo dục, việc quản lý học sinh theo phương pháp truyền thống (sổ sách giấy tờ) bộc lộ nhiều hạn chế: khó tra cứu, dễ thất lạc dữ liệu, và thiếu sự tương tác tức thời giữa nhà trường và gia đình.

### 1.2. Mục tiêu đề tài
Xây dựng ứng dụng di động "Sổ liên lạc điện tử" giúp:
- **Nhà trường:** Quản lý hồ sơ, điểm số, nề nếp tập trung.
- **Giáo viên:** Nhập điểm, điểm danh, ghi nhận vi phạm nhanh chóng.
- **Phụ huynh/Học sinh:** Theo dõi kết quả học tập, thời khóa biểu và nhận thông báo tức thời.
- **Đội Sao đỏ:** Chấm điểm thi đua nề nếp trực tiếp trên ứng dụng.

### 1.3. Phạm vi nghiên cứu
- Ứng dụng client trên hệ điều hành Android.
- Web Service (API) xử lý nghiệp vụ phía Backend.
- Cơ sở dữ liệu lưu trữ thông tin trường học.

---

<a name="chuong-2"></a>
## CHƯƠNG 2: PHÂN TÍCH YÊU CẦU VÀ THIẾT KẾ HỆ THỐNG

### 2.1. Các tác nhân hệ thống (Actors)
1.  **Admin (Quản trị viên):** Quản lý người dùng, phân quyền, cấu hình hệ thống.
2.  **Giáo viên (Teacher):** Quản lý lớp chủ nhiệm, nhập điểm, xem lịch dạy.
3.  **Học sinh (Student):** Xem điểm, thời khóa biểu, tin tức.
4.  **Phụ huynh (Parent):** Theo dõi tình hình học tập của con.
5.  **Sao đỏ (Red Star):** Vai trò đặc biệt, thực hiện chấm điểm nề nếp các lớp.

### 2.2. Biểu đồ Use-Case
*(Vị trí chèn hình ảnh: Trang 5)*
- **UC Đăng nhập:** Xác thực người dùng, hỗ trợ đa vai trò.
- **UC Quản lý nề nếp:** Sao đỏ chấm điểm -> Hệ thống trừ điểm thi đua -> Giáo viên chủ nhiệm nhận thông báo.
- **UC Xem điểm/TKB:** Học sinh/Phụ huynh tra cứu dữ liệu thời gian thực.

### 2.3. Thiết kế Cơ sở dữ liệu (Database Design)
Hệ thống sử dụng MySQL với các bảng chính:
- **`users`**: Lưu thông tin đăng nhập, phân quyền (role: admin, teacher, student, parent, red_star).
- **`classes`**: Danh sách lớp học.
- **`schedule`**: Thời khóa biểu, liên kết Giáo viên - Lớp - Môn học.
- **`scores`**: Bảng điểm chi tiết (15p, 1 tiết, học kỳ).
- **`violations`**: Ghi nhận vi phạm nề nếp.
- **`news` / `notifications`**: Tin tức và thông báo đẩy.

*(Vị trí chèn biểu đồ lớp/ERD: Trang 10)*

---

<a name="chuong-3"></a>
## CHƯƠNG 3: KIẾN TRÚC VÀ CÔNG NGHỆ SỬ DỤNG

### 3.1. Mô hình Kiến trúc
Hệ thống áp dụng mô hình **Client-Server** thông qua giao thức RESTful API.
- **Client (Android):** Gửi HTTP Request (GET, POST).
- **Server (PHP):** Xử lý Logic, truy vấn MySQL, trả về JSON.

*(Vị trí chèn ảnh kiến trúc hệ thống)*

### 3.2. Công nghệ sử dụng
- **Android (Java):**
    - **Retrofit 2:** Thư viện Networking gọi API.
    - **Gson:** Chuyển đổi JSON sang Object.
    - **RecyclerView:** Hiển thị danh sách dữ liệu lớn.
    - **SharedPreferences:** Lưu phiên đăng nhập cục bộ.
- **Backend (PHP Native):**
    - **PDO:** Kết nối cơ sở dữ liệu an toàn, chống SQL Injection.
    - **JWT (JSON Web Token):** Xác thực bảo mật không trạng thái (Stateless).
    - **MySQL:** Hệ quản trị cơ sở dữ liệu quan hệ.

---

<a name="chuong-4"></a>
## CHƯƠNG 4: CÀI ĐẶT VÀ TRIỂN KHAI

### 4.1. Cấu hình môi trường
- **Server:** XAMPP (Apache + MySQL).
- **IDE:** Android Studio (Client), VS Code (Backend).
- **Công cụ kiểm thử:** Postman, thiết bị thật Android.

### 4.2. Giải thuật quan trọng
**Xử lý Logic Sao đỏ (Red Star):**
1.  Admin/Giáo viên tạo tài khoản Sao đỏ cho lớp.
2.  Hệ thống kiểm tra trùng lặp User.
3.  Tạo User mới với role `red_star` và set cờ `is_red_star = 1`.
4.  Gán thời hạn hoạt động (`duration_weeks`) để tự động khóa khi hết nhiệm kỳ.

*(Vị trí chèn biểu đồ luồng dữ liệu - Data Flow Diagram)*

---

<a name="chuong-5"></a>
## CHƯƠNG 5: KẾT QUẢ THỬ NGHIỆM

### 5.1. Kịch bản kiểm thử (Test Cases)
Đã thực hiện kiểm thử tự động (Automated Testing) cho các module Backend.
- **File test:** `backend/tests/unit_test.php`
- **Phạm vi:** Kết nối DB, Cấu trúc bảng User, Logic tạo tài khoản Sao đỏ.

### 5.2. Kết quả thực tế
*(Vị trí chèn ảnh màn hình giao diện: Trang 20 - Màn hình chính, Trang 50 - Kết quả Test)*

**Giao diện Đăng nhập:**
- Hỗ trợ chọn vai trò.
- Xử lý lỗi nhập liệu, hiển thị Captcha khi đăng nhập sai nhiều lần.
- Fix lỗi ANR (Application Not Responding) khi spam nút đăng nhập.

**Chức năng Sao đỏ:**
- Admin/Giáo viên có thể thêm/xóa sao đỏ.
- Sao đỏ đăng nhập thấy menu "Chấm nề nếp" riêng biệt.

### 5.3. Hiệu năng (Performance)
- Thời gian phản hồi API trung bình: < 200ms.
- Tải danh sách học sinh (40-50 item): Mượt mà, không giật lag.

*(Vị trí chèn biểu đồ hiệu năng: Trang 30)*

---

<a name="chuong-6"></a>
## CHƯƠNG 6: ĐÁNH GIÁ VÀ HƯỚNG PHÁT TRIỂN

### 6.1. Đánh giá
- **Ưu điểm:** Đáp ứng đầy đủ các yêu cầu quản lý cơ bản. Giao diện thân thiện, dễ sử dụng. Hệ thống phân quyền chặt chẽ.
- **Nhược điểm:** Chưa có chức năng chat realtime (hiện dùng API polling). Chưa tích hợp thanh toán học phí online.

### 6.2. Hướng phát triển
- Tích hợp Socket.IO cho tính năng Chat và Thông báo thời gian thực.
- Xây dựng phiên bản Web Portal cho Admin quản trị dễ dàng hơn.
- Ứng dụng AI để phân tích điểm số, dự báo học lực học sinh.

---

<a name="tai-lieu-tham-khao"></a>
## TÀI LIỆU THAM KHẢO
1.  Google Developers. *Android API Guides*.
2.  PHP Documentation. *PDO & Security*.
3.  Retrofit Documentation. *A type-safe HTTP client for Android*.

---
*Báo cáo được tạo tự động ngày 12/01/2026*
