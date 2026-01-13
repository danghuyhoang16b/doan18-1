Tôi đã xác định được nguyên nhân lỗi biên dịch.

**Nguyên nhân:**
Tôi đã cập nhật `ApiService` để yêu cầu token xác thực (tham số `bearer`) cho hàm `getViolationStats`, nhưng chưa cập nhật mã nguồn gọi hàm này trong `AdminViolationStatsActivity.java`. Do đó, trình biên dịch báo lỗi thiếu tham số.

**Kế hoạch sửa lỗi:**
1.  **Cập nhật `AdminViolationStatsActivity.java`**:
    *   Lấy token từ bộ nhớ bằng `SharedPrefsUtils.getToken(this)`.
    *   Truyền token vào hàm `getViolationStats` theo đúng định dạng `"Bearer " + token`.

**Chi tiết thay đổi:**
```java
// Trước:
apiService.getViolationStats(type).enqueue(...)

// Sau:
String token = SharedPrefsUtils.getToken(this);
apiService.getViolationStats("Bearer " + token, type).enqueue(...)
```

Tôi sẽ thực hiện sửa đổi này ngay sau khi bạn xác nhận.