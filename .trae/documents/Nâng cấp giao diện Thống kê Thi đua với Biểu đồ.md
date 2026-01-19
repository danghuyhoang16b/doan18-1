# Chuyển đổi giao diện Xếp hạng thành Biểu đồ Thống kê

Bạn thấy "đâu khác đâu" vì tôi mới chỉ cập nhật logic chọn ngày (khi bấm vào ô nhập) và dữ liệu backend, chứ chưa vẽ biểu đồ lên màn hình. Giao diện vẫn là danh sách cũ.

Tôi sẽ thực hiện các thay đổi lớn sau để biến màn hình này thành **Dashboard Thống kê trực quan**:

## 1. Cập nhật Giao diện (Android)
### Màn hình Thống kê (`AdminRankingActivity`)
- **Thay đổi hoàn toàn Layout**:
    - Xóa danh sách cuộn (`RecyclerView`) đơn điệu.
    - Thêm **Biểu đồ cột (Bar Chart)** lớn: Hiển thị so sánh điểm thi đua giữa các lớp (Lớp nào bị trừ nhiều điểm nhất sẽ cột cao nhất).
    - Thêm **Biểu đồ tròn (Pie Chart)** (nếu cần): Tỷ lệ các loại vi phạm phổ biến (Đi học muộn, Đồng phục...).
- **Cải tiến bộ lọc thời gian**:
    - Thay ô nhập liệu bằng giao diện chọn ngày rõ ràng hơn (icon lịch, text hiển thị "Tháng 10/2025" thay vì "2025/10").

## 2. Tích hợp Thư viện Biểu đồ
- **MPAndroidChart**: Dự án đã có sẵn thư viện này (tôi vừa kiểm tra file `build.gradle`), nên tôi sẽ tận dụng ngay để vẽ biểu đồ đẹp mắt, có thể phóng to/thu nhỏ và chạm vào cột để xem chi tiết.

## 3. Kết nối Dữ liệu Backend
- Sử dụng API `get_competition_stats.php` (vừa nâng cấp) để lấy dữ liệu thực tế:
    - Danh sách lớp và tổng điểm trừ -> Vẽ lên Bar Chart.
    - Danh sách vi phạm phổ biến -> Vẽ lên Pie Chart hoặc Top List.

Sau khi hoàn tất, bạn sẽ thấy sự "lột xác" hoàn toàn: thay vì nhìn những con số khô khan, bạn sẽ thấy biểu đồ trực quan thể hiện tình hình thi đua của toàn trường.
