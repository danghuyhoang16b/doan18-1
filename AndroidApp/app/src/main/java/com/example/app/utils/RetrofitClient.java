package com.example.app.utils;

import android.content.Context;
import android.util.Log;

import com.example.app.network.ApiClient;
import com.example.app.network.ApiConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @deprecated Use {@link ApiClient} instead.
 * This class is kept for backward compatibility during migration.
 *
 * Migration guide:
 * - Old: ApiClient.getInstance().getApiService()
 * - New: ApiClient.api() or ApiClient.getInstance().getApiService()
 */
@Deprecated
public class RetrofitClient {
    private static final String TAG = "RetrofitClient";

    private static Retrofit retrofit = null;

    /**
     * @deprecated Use {@link ApiConstants#BASE_URL} instead
     */
    @Deprecated
    public static final String BASE_URL = ApiConstants.BASE_URL;

    /**
     * Get Retrofit client instance
     *
     * @deprecated Use {@link ApiClient#api()} instead
     * @return Retrofit instance
     */
    @Deprecated
    public static Retrofit getClient() {
        if (retrofit == null) {
            Log.w(TAG, "RetrofitClient.getClient() is deprecated. Use ApiClient.api() instead.");
            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConstants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    /**
     * Override for testing purposes
     *
     * @deprecated Use ApiClient for testing
     */
    @Deprecated
    public static void overrideForTesting(String baseUrl) {
        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
