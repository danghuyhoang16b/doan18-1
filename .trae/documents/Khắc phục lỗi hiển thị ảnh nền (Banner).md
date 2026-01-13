Tôi đã tìm ra nguyên nhân và có kế hoạch khắc phục sự cố không cập nhật giao diện sau khi thay thế ảnh nền.

**Nguyên nhân:**
1.  **Frontend (Android):**
    *   Trong hàm `loadPreview`, code đang cố gắng tự tải ảnh bằng `HttpURLConnection` và `BitmapFactory` trên một luồng riêng (`Executors.newSingleThreadExecutor`).
    *   Vấn đề chính: Hàm `loadPreview` nhận đầu vào là `url` (có thể là tên file hoặc URL đầy đủ). Nếu là tên file (ví dụ "banner_1"), `new URL(url)` sẽ ném ra ngoại lệ `MalformedURLException` và khối `catch` bắt lỗi này nhưng không làm gì cả (`ignored`). Kết quả là ảnh không được tải và `ImageView` giữ nguyên trạng thái cũ hoặc trống.
    *   Code thiếu logic ghép `BASE_URL` cho trường hợp `url` chỉ là tên file.

2.  **Backend (PHP):**
    *   API `upload_background.php` trả về JSON chứa `url` đầy đủ của ảnh mới upload.
    *   Tuy nhiên, API `get_background.php` (dùng trong `loadCurrentBackground`) có thể chỉ trả về tên file (lưu trong DB) mà không phải URL đầy đủ, dẫn đến lỗi ở Frontend như đã phân tích.

**Kế hoạch sửa chữa:**

1.  **Frontend (`AdminDashboardActivity.java`):**
    *   Thay thế cơ chế tải ảnh thủ công bằng thư viện **Glide** (đã dùng trong dự án). Glide tự động xử lý caching, luồng, và lỗi tốt hơn.
    *   Cập nhật logic `loadPreview`:
        *   Kiểm tra nếu `url` không bắt đầu bằng "http", tự động ghép với `RetrofitClient.BASE_URL` (đã loại bỏ phần "api/").
        *   Sử dụng `Glide.with(this).load(fullUrl).into(target)`.
        *   Thêm `diskCacheStrategy(DiskCacheStrategy.NONE)` và `skipMemoryCache(true)` để đảm bảo ảnh mới nhất được tải (tránh cache ảnh cũ cùng tên).

2.  **Backend (`upload_background.php`):**
    *   Hiện tại logic backend đã ổn (trả về URL đầy đủ). Không cần sửa đổi lớn, nhưng cần đảm bảo `get_background.php` trả về dữ liệu nhất quán. (Sẽ kiểm tra thêm nếu cần, nhưng ưu tiên sửa Frontend để xử lý cả 2 trường hợp).

3.  **Kiểm thử:**
    *   Thay thế ảnh -> Glide tải ảnh mới (bỏ qua cache) -> Giao diện cập nhật ngay lập tức.

Tôi sẽ tiến hành sửa code Android ngay bây giờ.