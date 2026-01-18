package com.example.app.utils;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    // Thay đổi địa chỉ IP này thành địa chỉ IP máy tính của bạn khi chạy trên máy thật
    // public static final String BASE_URL = "http://10.0.2.2/Backend/api/"; // Cho máy ảo
    public static final String BASE_URL = "http://103.252.136.73:8080/api/"; // Cho máy thật

    public static Retrofit getClient() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static void overrideForTesting(String baseUrl) {
        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
