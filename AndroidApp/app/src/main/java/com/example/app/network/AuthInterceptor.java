package com.example.app.network;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp Interceptor that automatically adds Authorization header to requests
 *
 * This interceptor:
 * - Adds Bearer token to all authenticated requests
 * - Skips adding token to public endpoints (login, register, etc.)
 */
public class AuthInterceptor implements Interceptor {

    private final TokenProvider tokenProvider;

    // Endpoints that don't require authentication
    private static final String[] PUBLIC_ENDPOINTS = {
            "auth/login",
            "auth/request_otp",
            "auth/verify_otp",
            "captcha/get_challenge",
            "news/get_latest",
            "news/get_all",
            "banners/get_active",
            "classes/list_all",
            "subjects/list_all"
    };

    public AuthInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String url = originalRequest.url().toString();

        // Skip adding token for public endpoints
        if (isPublicEndpoint(url)) {
            return chain.proceed(originalRequest);
        }

        // Skip if request already has Authorization header
        if (originalRequest.header(ApiConstants.HEADER_AUTHORIZATION) != null) {
            return chain.proceed(originalRequest);
        }

        // Add token if available
        String token = tokenProvider.getToken();
        if (token != null && !token.isEmpty()) {
            Request newRequest = originalRequest.newBuilder()
                    .header(ApiConstants.HEADER_AUTHORIZATION, ApiConstants.BEARER_PREFIX + token)
                    .build();
            return chain.proceed(newRequest);
        }

        return chain.proceed(originalRequest);
    }

    /**
     * Check if the URL is a public endpoint that doesn't require authentication
     */
    private boolean isPublicEndpoint(String url) {
        for (String endpoint : PUBLIC_ENDPOINTS) {
            if (url.contains(endpoint)) {
                return true;
            }
        }
        return false;
    }
}
