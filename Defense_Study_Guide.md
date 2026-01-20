# TÀI LIỆU ÔN TẬP & CÂU HỎI PHẢN BIỆN (DEFENSE STUDY GUIDE)

## PHẦN 1: TỔNG HỢP KIẾN THỨC CỐT LÕI (KEY CONCEPTS)

### 1. Kiến trúc hệ thống (System Architecture)
- **Mô hình:** Client-Server.
- **Giao thức:** HTTP/HTTPS (Sử dụng phương thức GET, POST).
- **Định dạng dữ liệu:** JSON (JavaScript Object Notation).
- **Cơ chế hoạt động:**
  1. **Android App** (Dùng thư viện Retrofit) gửi Request kèm Token (nếu cần).
  2. **Server PHP** nhận Request -> Kiểm tra Token (Middleware) -> Gọi Database -> Xử lý Logic -> Trả về JSON.
  3. **Android App** nhận JSON -> Parse (GSON) -> Hiển thị lên màn hình (RecyclerView, TextView, Chart).

### 2. Công nghệ & Thư viện (Tech Stack)
- **Android (Java):**
  - **Retrofit:** Thư viện mạng mạnh mẽ nhất hiện nay cho Android, dùng để gọi API.
  - **GSON:** Chuyển đổi chuỗi JSON thành Java Object và ngược lại.
  - **MPAndroidChart:** Thư viện vẽ biểu đồ (Cột, Tròn) chuyên nghiệp.
  - **Glide:** Thư viện tải và hiển thị ảnh từ URL (có cache giúp mượt mà).
  - **SharedPreferences:** Lưu trữ nhỏ cục bộ (Lưu Token đăng nhập, thông tin user).
- **Backend (PHP):**
  - **PDO (PHP Data Objects):** Thư viện kết nối CSDL an toàn, chống lỗi SQL Injection.
  - **JWT (JSON Web Token):** Cơ chế tạo "chìa khóa" phiên đăng nhập. Không cần lưu Session trên server, giúp server nhẹ hơn (Stateless).

---

## PHẦN 2: CÂU HỎI PHẢN BIỆN THƯỜNG GẶP (Q&A)

### Nhóm câu hỏi về Công nghệ & Kỹ thuật

**Câu 1: Tại sao em chọn PHP mà không phải NodeJS hay Java Spring Boot?**
*Trả lời:*
- PHP là ngôn ngữ phổ biến, dễ triển khai (Deploy) trên hầu hết các hosting giá rẻ/miễn phí, phù hợp với quy mô trường học.
- PHP xử lý tốt các tác vụ CRUD (Thêm/Sửa/Xóa) truyền thống của hệ thống quản lý.
- Em muốn tự xây dựng mô hình MVC trên PHP để hiểu sâu về cách hoạt động của Backend thay vì dùng Framework có sẵn quá nhiều.

**Câu 2: Cơ chế đăng nhập và bảo mật của em hoạt động như thế nào?**
*Trả lời:*
- Em sử dụng **JWT (JSON Web Token)**.
- Khi user đăng nhập đúng, Server trả về một chuỗi Token mã hóa.
- App lưu Token này vào máy.
- Mọi request sau đó (như xem điểm, xem lớp), App đều gửi kèm Token này ở Header (`Authorization: Bearer <token>`).
- Server sẽ giải mã Token để biết ai đang gửi yêu cầu và có quyền hay không.

**Câu 3: Làm sao để ngăn chặn SQL Injection?**
*Trả lời:*
- Em sử dụng **PDO** và **Prepared Statements** trong PHP.
- Thay vì cộng chuỗi SQL trực tiếp (dễ bị hack), em dùng các tham số (`:username`, `:id`) để bind dữ liệu. Database sẽ hiểu đó là dữ liệu, không phải câu lệnh thực thi.

**Câu 4: Ứng dụng xử lý thế nào khi mất mạng (Offline)?**
*Trả lời:*
- Hiện tại ứng dụng yêu cầu kết nối mạng để lấy dữ liệu mới nhất.
- Tuy nhiên, các thư viện như **Glide** có cơ chế Cache ảnh, nên ảnh đã tải rồi vẫn xem được khi mất mạng.
- (Nếu có làm thêm): Em có thể phát triển thêm tính năng lưu cache dữ liệu JSON vào SQLite/Room để xem offline trong tương lai.

### Nhóm câu hỏi về Chức năng & Nghiệp vụ

**Câu 5: Tính năng "Xếp hạng" được tính toán như thế nào?**
*Trả lời:*
- Dựa trên bảng `violations` (vi phạm) và `conduct_rules` (quy tắc).
- Mỗi lỗi vi phạm có điểm trừ tương ứng.
- Hệ thống dùng câu lệnh SQL `SUM(points)` để tính tổng điểm trừ của lớp/học sinh trong khoảng thời gian (tuần/tháng).
- Sau đó sắp xếp (`ORDER BY`) từ thấp đến cao (ít điểm trừ nhất xếp đầu).

**Câu 6: Nếu hai giáo viên cùng chấm điểm/sửa dữ liệu một lúc thì sao?**
*Trả lời:*
- Database MySQL hỗ trợ cơ chế Transaction và khóa dòng (row locking) cơ bản.
- Tuy nhiên, với quy mô trường học, xác suất xung đột thấp. Server sẽ xử lý tuần tự các request đến (First Come First Served). Dữ liệu người lưu sau sẽ được ghi nhận cuối cùng.

**Câu 7: Em đã kiểm thử (Test) ứng dụng như thế nào?**
*Trả lời:*
- Em kiểm thử theo quy trình: Unit Test (từng hàm API bằng Postman) -> Integration Test (Test tích hợp trên App) -> User Acceptance Test (Nhờ bạn bè dùng thử).
- Em đã test các trường hợp biên như: nhập sai pass, mất mạng, dữ liệu rỗng (như lỗi 500 vừa rồi em đã fix).

---

## PHẦN 3: THUẬT NGỮ CẦN NẮM VỮNG (GLOSSARY)

1.  **API (Application Programming Interface):** Cổng giao tiếp giữa App và Server.
2.  **Endpoint:** Một đường dẫn URL cụ thể (vd: `/api/login.php`).
3.  **JSON:** Định dạng văn bản nhẹ để trao đổi dữ liệu.
4.  **Request/Response:** Yêu cầu gửi đi và Phản hồi trả về.
5.  **Status Code:** Mã trạng thái HTTP (200: OK, 404: Not Found, 500: Server Error, 401: Unauthorized).
6.  **Activity (Android):** Một màn hình giao diện trong Android.
7.  **Adapter (Android):** Cầu nối để đổ dữ liệu từ List vào RecyclerView.
8.  **Callback:** Hàm được gọi lại khi một tác vụ (như gọi API) hoàn thành.

---

## PHẦN 4: LỜI KHUYÊN KHI THUYẾT TRÌNH & TRẢ LỜI
1.  **Tự tin:** Nói to, rõ ràng, nhìn vào hội đồng.
2.  **Trung thực:** Cái gì làm được thì nói, cái gì chưa làm được thì nhận là "hướng phát triển". Đừng chém gió sai kiến thức.
3.  **Demo mượt mà:** Chuẩn bị sẵn dữ liệu đẹp trong Database (đừng để danh sách trống trơn). Đảm bảo mạng 4G/Wifi ổn định khi Demo.
4.  **Nhấn mạnh vào tính năng Sao đỏ & Thống kê:** Đây là điểm sáng tạo nhất của đồ án so với các app quản lý thông thường.
