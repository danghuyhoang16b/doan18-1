package com.example.app.utils;

import android.content.Context;

public class UrlUtils {
    public static String getFullUrl(Context context, String path) {
        if (path == null || path.isEmpty()) return "";
        if (path.startsWith("http")) return path;

        String baseUrl = SharedPrefsUtils.getBaseUrl(context);
        // Fallback if not set in prefs
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = RetrofitClient.BASE_URL; // Fallback to hardcoded
        }

        // Remove 'api/' suffix if present to get root
        if (baseUrl.endsWith("api/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 4);
        }
        // Ensure trailing slash
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        // Remove leading slash from path if present to avoid double slash
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return baseUrl + path;
    }
}
