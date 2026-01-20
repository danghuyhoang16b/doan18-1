# TÀI LIỆU NỘI DUNG SLIDE THUYẾT TRÌNH ĐỒ ÁN TỐT NGHIỆP
## Đề tài: Ứng dụng Quản lý Trường học (School Management System)

---

### Slide 1: Trang Chào (Title Slide)
**Nội dung trên Slide:**
- **Tên đề tài:** Xây dựng Hệ thống Quản lý Trường học trên nền tảng Android và Web
- **Sinh viên thực hiện:** [Tên Của Bạn]
- **Mã sinh viên:** [Mã SV]
- **Giảng viên hướng dẫn:** [Tên GVHD]
- **Logo trường/Logo khoa**

**Kịch bản thuyết trình (Script):**
"Kính thưa Hội đồng bảo vệ, thưa toàn thể các thầy cô và các bạn. Em tên là [Tên Bạn]. Sau đây, em xin phép được trình bày về đồ án tốt nghiệp của mình với đề tài: 'Xây dựng Hệ thống Quản lý Trường học trên nền tảng Android và Web'. Đây là giải pháp công nghệ nhằm hiện đại hóa quy trình quản lý, kết nối chặt chẽ giữa Nhà trường, Giáo viên, Phụ huynh và Học sinh."

---

### Slide 2: Đặt vấn đề & Lý do chọn đề tài
**Nội dung trên Slide:**
- **Thực trạng:**
    - Quản lý thủ công (sổ sách, Excel) tốn thời gian, dễ sai sót.
    - Thông tin liên lạc giữa nhà trường và phụ huynh chậm trễ.
    - Khó khăn trong việc theo dõi thi đua, nề nếp tức thời.
- **Giải pháp:**
    - Số hóa toàn bộ dữ liệu.
    - Ứng dụng di động tiện lợi, mọi lúc mọi nơi.
    - Cập nhật thông tin thời gian thực (Real-time).

**Hình ảnh minh họa:** Ảnh so sánh giữa sổ sách chồng chất và một người dùng cầm điện thoại quản lý nhẹ nhàng.

**Kịch bản thuyết trình:**
"Xuất phát từ thực tế, việc quản lý nề nếp, điểm số và thông báo tại các trường phổ thông hiện nay vẫn còn phụ thuộc nhiều vào sổ sách giấy tờ hoặc các công cụ rời rạc. Điều này dẫn đến việc cập nhật thông tin chậm trễ và khó khăn trong việc tra cứu. Chính vì vậy, em đã quyết định xây dựng hệ thống này nhằm giải quyết ba bài toán chính: Số hóa dữ liệu quản lý, Tăng cường kênh liên lạc gia đình - nhà trường, và Cung cấp công cụ theo dõi thi đua trực quan, chính xác."

---

### Slide 3: Mục tiêu Đồ án
**Nội dung trên Slide:**
- **Mục tiêu tổng quát:** Xây dựng hệ thống quản lý trường học tập trung.
- **Mục tiêu cụ thể:**
    1. **Quản lý học sinh:** Hồ sơ, điểm số, hạnh kiểm.
    2. **Quản lý nề nếp (Sao đỏ):** Chấm điểm thi đua, báo cáo vi phạm qua App.
    3. **Kết nối:** Tin nhắn, Thông báo tức thời (Notification).
    4. **Thống kê:** Báo cáo trực quan (Biểu đồ) cho Ban giám hiệu/Giáo viên.

**Kịch bản thuyết trình:**
"Mục tiêu cốt lõi của đồ án là tạo ra một hệ thống tập trung. Cụ thể, hệ thống cho phép quản lý toàn diện hồ sơ học sinh, điểm số. Đặc biệt, em tập trung vào tính năng 'Sổ đầu bài điện tử' và 'Sao đỏ chấm thi đua' ngay trên điện thoại, giúp loại bỏ hoàn toàn việc cộng trừ điểm thủ công. Bên cạnh đó là hệ thống báo cáo thống kê tự động giúp Ban giám hiệu có cái nhìn tổng quan ngay lập tức."

---

### Slide 4: Công nghệ sử dụng
**Nội dung trên Slide:**
- **Mobile App (Client):**
    - Ngôn ngữ: **Java** (Android Native).
    - IDE: Android Studio.
    - Thư viện: **Retrofit** (API), **MPAndroidChart** (Biểu đồ), **Glide** (Xử lý ảnh).
- **Backend (Server):**
    - Ngôn ngữ: **PHP** (Mô hình MVC tự xây dựng).
    - Database: **MySQL/MariaDB**.
    - API: RESTful API (JSON).
    - Authentication: **JWT** (JSON Web Token).

**Hình ảnh minh họa:** Logo của Android, Java, PHP, MySQL, Retrofit.

**Kịch bản thuyết trình:**
"Về mặt công nghệ, em lựa chọn xây dựng ứng dụng Native Android bằng ngôn ngữ Java để đảm bảo hiệu năng cao và trải nghiệm người dùng tốt nhất. Phía Server, em sử dụng PHP thuần kết hợp cơ sở dữ liệu MySQL, giao tiếp với ứng dụng qua chuẩn RESTful API. Để bảo mật, hệ thống sử dụng cơ chế xác thực JWT, đảm bảo an toàn cho dữ liệu người dùng."

---

### Slide 5: Kiến trúc Hệ thống
**Nội dung trên Slide:**
- **Mô hình Client-Server:**
    - **Client (Android):** Giao diện người dùng, gửi Request.
    - **Server (PHP):** Xử lý Logic, Truy vấn Database, Trả về JSON.
    - **Database:** Lưu trữ tập trung.
- **Sơ đồ luồng dữ liệu cơ bản:** User -> Android App -> API (Internet) -> PHP Controller -> Database.

**Hình ảnh minh họa:** Sơ đồ khối kiến trúc hệ thống (Vẽ sơ đồ đơn giản: App Mobile <--> Đám mây API <--> Server/DB).

**Kịch bản thuyết trình:**
"Hệ thống hoạt động theo mô hình Client-Server truyền thống. Ứng dụng Android đóng vai trò là Client, gửi các yêu cầu HTTP đến Server. Server PHP sẽ tiếp nhận, xử lý logic nghiệp vụ, truy xuất dữ liệu từ MySQL và trả kết quả về dưới dạng JSON. Ứng dụng sau đó sẽ phân tích và hiển thị dữ liệu này lên giao diện trực quan cho người dùng."

---

### Slide 6: Các Chức năng Nổi bật (Demo)
**Nội dung trên Slide:**
1. **Phân quyền đa dạng:** Admin, Giáo viên, Học sinh, Phụ huynh, Sao đỏ.
2. **Chấm nề nếp (Sao đỏ):** Chọn lớp, chọn lỗi vi phạm, trừ điểm tự động.
3. **Thống kê & Xếp hạng:** Biểu đồ xếp hạng lớp, xếp hạng học sinh (Real-time).
4. **Thời khóa biểu & Điểm danh:** Tự động hóa lịch học.
5. **Tin tức & Thông báo:** Gửi thông báo đẩy (Push Notification) toàn trường.

**Hình ảnh minh họa:** Screenshot các màn hình chính: Màn hình Dashboard Giáo viên, Màn hình Chấm điểm Sao đỏ, Biểu đồ thống kê.

**Kịch bản thuyết trình:**
"Điểm mạnh của hệ thống là khả năng phân quyền sâu. Đặc biệt là tính năng dành cho Đội cờ đỏ: thay vì ghi chép giấy, các em có thể chấm điểm trực tiếp trên App, hệ thống tự động trừ điểm thi đua của lớp. Giáo viên chủ nhiệm có thể xem ngay lập tức xếp hạng của lớp mình và danh sách học sinh vi phạm thông qua các biểu đồ trực quan mà không cần chờ đến cuối tuần tổng kết."

---

### Slide 7: Kết quả đạt được
**Nội dung trên Slide:**
- Hệ thống hoạt động ổn định trên môi trường thực tế.
- Giao diện thân thiện, dễ sử dụng cho cả giáo viên lớn tuổi.
- Tốc độ phản hồi nhanh (< 1 giây cho các tác vụ chính).
- Đã giải quyết được bài toán thống kê thi đua tự động.

**Hình ảnh minh họa:** Ảnh chụp ứng dụng đang chạy trên điện thoại thật, hoặc video demo ngắn (nếu có).

**Kịch bản thuyết trình:**
"Sau quá trình phát triển và kiểm thử, ứng dụng đã hoạt động ổn định với đầy đủ các tính năng đề ra. Giao diện được thiết kế tối giản, tập trung vào trải nghiệm người dùng, giúp giáo viên và học sinh dễ dàng làm quen. Quan trọng nhất, hệ thống đã tự động hóa hoàn toàn quy trình tính điểm thi đua, giúp tiết kiệm đến 90% thời gian tổng hợp báo cáo cho nhà trường."

---

### Slide 8: Hướng phát triển
**Nội dung trên Slide:**
- Tích hợp điểm danh bằng khuôn mặt (AI) hoặc QR Code.
- Phát triển phiên bản Web Portal dành cho Admin quản trị dễ dàng hơn.
- Tích hợp thanh toán học phí trực tuyến (VNPay/Momo).
- Chat nhóm cho từng lớp học.

**Kịch bản thuyết trình:**
"Trong tương lai, em dự kiến sẽ nâng cấp hệ thống với các tính năng thông minh hơn như điểm danh bằng nhận diện khuôn mặt AI để tránh gian lận, và tích hợp cổng thanh toán để phụ huynh có thể đóng học phí trực tuyến ngay trên ứng dụng. Em cũng mong muốn phát triển phiên bản Web hoàn chỉnh hơn để hỗ trợ công tác quản trị."

---

### Slide 9: Kết thúc & Cảm ơn
**Nội dung trên Slide:**
- **Lời cảm ơn:** Cảm ơn thầy cô hướng dẫn và hội đồng.
- **Q&A:** Mời hội đồng đặt câu hỏi.
- **Thông tin liên hệ.**

**Kịch bản thuyết trình:**
"Trên đây là toàn bộ nội dung đồ án của em. Em xin chân thành cảm ơn thầy/cô [Tên GVHD] đã tận tình hướng dẫn em hoàn thành đồ án này. Em rất mong nhận được những ý kiến đóng góp và câu hỏi từ Hội đồng để hoàn thiện sản phẩm hơn nữa. Em xin cảm ơn!"
