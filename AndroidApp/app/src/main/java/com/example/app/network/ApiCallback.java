package com.example.app.network;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Generic API Callback wrapper for consistent error handling
 *
 * Usage:
 * <pre>
 * apiService.someCall().enqueue(new ApiCallback<MyResponse>() {
 *     @Override
 *     public void onSuccess(MyResponse data) {
 *         // Handle success
 *     }
 *
 *     @Override
 *     public void onError(String message, int code) {
 *         // Handle error
 *     }
 * });
 * </pre>
 *
 * @param <T> The response type
 */
public abstract class ApiCallback<T> implements Callback<T> {

    private static final String TAG = "ApiCallback";

    /**
     * Called when API call is successful
     *
     * @param data The response data
     */
    public abstract void onSuccess(T data);

    /**
     * Called when API call fails
     *
     * @param message Error message
     * @param code    HTTP status code (0 if network error)
     */
    public abstract void onError(String message, int code);

    /**
     * Called when API call finishes (success or error)
     * Override this to hide loading indicators
     */
    public void onComplete() {
        // Optional: Override to handle completion
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        try {
            if (response.isSuccessful()) {
                T body = response.body();
                onSuccess(body);
            } else {
                String errorMessage = parseErrorMessage(response);
                Log.e(TAG, "API Error: " + response.code() + " - " + errorMessage);
                onError(errorMessage, response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing response", e);
            onError("Lỗi xử lý dữ liệu: " + e.getMessage(), 0);
        } finally {
            onComplete();
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        Log.e(TAG, "Network error", t);
        String message = getNetworkErrorMessage(t);
        onError(message, 0);
        onComplete();
    }

    /**
     * Parse error message from response body
     */
    private String parseErrorMessage(Response<T> response) {
        String defaultMessage = getDefaultErrorMessage(response.code());

        try {
            ResponseBody errorBody = response.errorBody();
            if (errorBody != null) {
                String errorString = errorBody.string();
                if (errorString != null && !errorString.isEmpty()) {
                    JSONObject json = new JSONObject(errorString);
                    return json.optString("message", defaultMessage);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error body", e);
        }

        return defaultMessage;
    }

    /**
     * Get default error message based on HTTP status code
     */
    private String getDefaultErrorMessage(int code) {
        switch (code) {
            case ApiConstants.HTTP_BAD_REQUEST:
                return "Yêu cầu không hợp lệ";
            case ApiConstants.HTTP_UNAUTHORIZED:
                return "Phiên đăng nhập hết hạn";
            case ApiConstants.HTTP_FORBIDDEN:
                return "Bạn không có quyền thực hiện thao tác này";
            case ApiConstants.HTTP_NOT_FOUND:
                return "Không tìm thấy dữ liệu";
            case ApiConstants.HTTP_CONFLICT:
                return "Dữ liệu đã tồn tại";
            case ApiConstants.HTTP_PRECONDITION_REQUIRED:
                return "Yêu cầu xác thực thao tác";
            case ApiConstants.HTTP_SERVER_ERROR:
                return "Lỗi máy chủ";
            default:
                return "Lỗi: " + code;
        }
    }

    /**
     * Get user-friendly network error message
     */
    private String getNetworkErrorMessage(Throwable t) {
        if (t instanceof java.net.UnknownHostException) {
            return "Không thể kết nối đến máy chủ";
        } else if (t instanceof java.net.SocketTimeoutException) {
            return "Kết nối quá thời gian chờ";
        } else if (t instanceof java.net.ConnectException) {
            return "Không thể kết nối mạng";
        } else if (t instanceof IOException) {
            return "Lỗi kết nối: " + t.getMessage();
        }
        return "Lỗi không xác định: " + t.getMessage();
    }

    // =====================================================
    // CONVENIENCE FACTORY METHODS
    // =====================================================

    /**
     * Create a simple callback that only handles success
     */
    public static <T> ApiCallback<T> simple(SuccessHandler<T> successHandler, ErrorHandler errorHandler) {
        return new ApiCallback<T>() {
            @Override
            public void onSuccess(T data) {
                if (successHandler != null) {
                    successHandler.handle(data);
                }
            }

            @Override
            public void onError(String message, int code) {
                if (errorHandler != null) {
                    errorHandler.handle(message, code);
                }
            }
        };
    }

    /**
     * Functional interface for success handling
     */
    public interface SuccessHandler<T> {
        void handle(T data);
    }

    /**
     * Functional interface for error handling
     */
    public interface ErrorHandler {
        void handle(String message, int code);
    }
}
