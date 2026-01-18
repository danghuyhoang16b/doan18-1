package com.example.app.network;

import android.content.Context;
import android.util.Log;

import com.example.app.utils.SharedPrefsUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton API Client for managing Retrofit instance
 *
 * Features:
 * - Singleton pattern for consistent API access
 * - Automatic token injection via AuthInterceptor
 * - Configurable timeouts
 * - HTTP logging for debugging
 * - Lenient JSON parsing
 */
public class ApiClient {

    private static final String TAG = "ApiClient";

    private static volatile ApiClient instance;
    private static volatile ApiService apiService;

    private final Retrofit retrofit;
    private final Context applicationContext;

    /**
     * Private constructor - use getInstance() instead
     */
    private ApiClient(Context context) {
        this.applicationContext = context.getApplicationContext();

        // Create TokenProvider using SharedPrefsUtils
        TokenProvider tokenProvider = new TokenProvider() {
            @Override
            public String getToken() {
                return SharedPrefsUtils.getToken(applicationContext);
            }

            @Override
            public boolean isLoggedIn() {
                return getToken() != null;
            }
        };

        // Build OkHttpClient with interceptors
        OkHttpClient okHttpClient = buildOkHttpClient(tokenProvider);

        // Build Gson with lenient parsing
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        // Build Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Log.d(TAG, "ApiClient initialized with base URL: " + ApiConstants.BASE_URL);
    }

    /**
     * Build OkHttpClient with all required interceptors
     */
    private OkHttpClient buildOkHttpClient(TokenProvider tokenProvider) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // Set timeouts
        builder.connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS);

        // Add Auth Interceptor for automatic token injection
        builder.addInterceptor(new AuthInterceptor(tokenProvider));

        // Add logging interceptor for debugging (only in debug builds)
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                Log.d(TAG, message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);

        return builder.build();
    }

    /**
     * Initialize ApiClient with Application Context
     * Call this in Application.onCreate()
     *
     * @param context Application context
     */
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
    }

    /**
     * Get ApiClient instance
     * Must call init() before using this method
     *
     * @return ApiClient instance
     * @throws IllegalStateException if init() was not called
     */
    public static ApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApiClient not initialized. Call ApiClient.init(context) first.");
        }
        return instance;
    }

    /**
     * Get ApiService instance for making API calls
     * This is the main method to use for API calls
     *
     * @return ApiService instance
     */
    public ApiService getApiService() {
        if (apiService == null) {
            synchronized (ApiClient.class) {
                if (apiService == null) {
                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }

    /**
     * Get Retrofit instance (for advanced usage)
     *
     * @return Retrofit instance
     */
    public Retrofit getRetrofit() {
        return retrofit;
    }

    /**
     * Convenience method to get ApiService directly
     * Equivalent to ApiClient.getInstance().getApiService()
     *
     * @return ApiService instance
     */
    public static ApiService api() {
        return getInstance().getApiService();
    }

    /**
     * Get the base URL being used
     *
     * @return base URL string
     */
    public String getBaseUrl() {
        return ApiConstants.BASE_URL;
    }

    /**
     * Reset the ApiClient instance (useful for testing or logout)
     */
    public static synchronized void reset() {
        instance = null;
        apiService = null;
    }

    // =====================================================
    // BACKWARD COMPATIBILITY - For gradual migration
    // Remove these methods after full migration
    // =====================================================

    /**
     * @deprecated Use ApiClient.getInstance().getApiService() instead
     * Kept for backward compatibility during migration
     */
    @Deprecated
    public static Retrofit getClient(Context context) {
        if (instance == null) {
            init(context);
        }
        return instance.retrofit;
    }
}
