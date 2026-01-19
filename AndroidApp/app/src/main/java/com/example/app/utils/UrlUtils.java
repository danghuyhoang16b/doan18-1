package com.example.app.utils;

import android.content.Context;

public class UrlUtils {
    public static String getFullUrl(Context context, String path) {
        if (path == null || path.isEmpty()) return "";
        if (path.startsWith("http")) return path;

        // Ưu tiên dùng ApiConstants.BASE_URL để lấy root domain
        // Nếu BASE_URL = "http://103.252.136.73:8080/api/" -> root = "http://103.252.136.73:8080/"
        String baseUrl = com.example.app.network.ApiConstants.BASE_URL;

        // Bỏ đuôi 'api/' nếu có để lấy root domain chứa thư mục uploads
        if (baseUrl.endsWith("api/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 4);
        }
        // Đảm bảo có dấu / ở cuối
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        // Bỏ dấu / ở đầu path nếu có để tránh double slash
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // Nếu path chưa có prefix 'Backend/' (do code cũ có thể lưu db thiếu) thì thêm vào
        // Tuy nhiên, theo logic upload_avatar.php thì nó trả về full URL rồi.
        // Hàm này chủ yếu dùng khi DB chỉ lưu tên file (vd: "avatar_123.jpg")
        
        // Cập nhật lại logic:
        // Nếu path chỉ là tên file (vd "avatar_123.jpg"), ta nối thêm đường dẫn đầy đủ
        if (!path.contains("/")) {
            path = "uploads/avatars/" + path;
        }

        return baseUrl + path;
    }
}
