Tôi đã phân tích hệ thống và xác định các vấn đề cần khắc phục để đảm bảo chức năng quản lý banner hoạt động ổn định và chính xác.

**Các vấn đề hiện tại:**
1.  **Backend (`upload.php`)**: Thiếu kiểm tra định dạng file (chỉ dựa vào đuôi file), không giới hạn dung lượng, và xử lý quyền thư mục chưa tốt.
2.  **Frontend (Android)**:
    *   `AdminBannerAdapter.java` đang fix cứng địa chỉ IP `10.0.2.2`, sẽ lỗi nếu chạy trên thiết bị thật hoặc đổi server.
    *   Thiếu trạng thái "Loading" khi upload ảnh, gây trải nghiệm người dùng kém.
    *   Chưa validate kích thước ảnh trước khi gửi lên server.

**Kế hoạch thực hiện:**

1.  **Nâng cấp Backend (`api/banners/upload.php`):**
    *   Thêm kiểm tra `MIME type` (bắt buộc là `image/jpeg` hoặc `image/png`).
    *   Giới hạn dung lượng file (ví dụ: tối đa 5MB).
    *   Chuẩn hóa đường dẫn trả về để đồng bộ với Frontend.
    *   Thêm log chi tiết khi upload thất bại.

2.  **Cải thiện Frontend (Android App):**
    *   **`AdminBannerActivity.java`**:
        *   Thêm `ProgressDialog` để hiển thị "Đang tải lên..." khi người dùng nhấn nút Thêm.
        *   Kiểm tra kích thước file trước khi upload (báo lỗi nếu > 5MB).
    *   **`AdminBannerAdapter.java`**:
        *   Loại bỏ IP cứng. Sử dụng `RetrofitClient.BASE_URL` để tạo đường dẫn ảnh động, giúp app hoạt động đúng trên mọi môi trường.
        *   Logic: Lấy `BASE_URL` (ví dụ `.../api/`), bỏ phần `api/`, ghép với đường dẫn ảnh (`uploads/banners/...`).

3.  **Kiểm thử:**
    *   Upload ảnh đúng định dạng -> Thành công -> Hiển thị ngay lập tức.
    *   Upload file không phải ảnh hoặc quá lớn -> Báo lỗi rõ ràng.

Tôi sẽ bắt đầu với việc cập nhật Backend trước, sau đó là Frontend.