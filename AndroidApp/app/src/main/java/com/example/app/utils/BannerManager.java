package com.example.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.app.network.ApiService;
import com.example.app.models.Banner;
import com.example.app.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import java.util.concurrent.TimeUnit;

public class BannerManager {
    private static final String PREF_NAME = "BannerCache";
    private static final String KEY_BANNERS = "cached_banners";
    private static EventSource currentSource;
    private static android.os.Handler sseHandler = new android.os.Handler();
    private static BannerCallback lastCallback;
    private static Context lastContext;
    private static int failureCount = 0;
    private static long lastFailureTs = 0L;
    private static boolean pollingFallback = false;
    private static long sseStableStartTs = 0L;
    private static final long SSE_COOLDOWN_MS = 30000;

    public interface BannerCallback {
        void onBannersLoaded(List<Banner> banners);
    }

    public static void loadBanners(Context context, BannerCallback callback) {
        loadBanners(context, false, callback);
    }

    public static void loadBanners(Context context, boolean skipCache, BannerCallback callback) {
        // 1. Load from cache first if not skipped
        if (!skipCache) {
            List<Banner> cachedBanners = getCachedBanners(context);
            if (cachedBanners != null && !cachedBanners.isEmpty()) {
                callback.onBannersLoaded(cachedBanners);
            }
        }

        // 2. Fetch from API
        ApiService apiService = ApiClient.getInstance().getApiService();
        apiService.getActiveBanners().enqueue(new Callback<List<Banner>>() {
            @Override
            public void onResponse(Call<List<Banner>> call, Response<List<Banner>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Banner> newBanners = response.body();
                    android.util.Log.d("BannerManager", "Fetched " + newBanners.size() + " banners");
                    if (!newBanners.isEmpty()) {
                        android.util.Log.d("BannerManager", "First banner url: " + newBanners.get(0).getImageUrl());
                    }
                    
                    // Check if different from cache to avoid unnecessary UI updates if desired,
                    // but here we just update always for freshness.
                    saveBannersToCache(context, newBanners);
                    callback.onBannersLoaded(newBanners);
                }
            }

            @Override
            public void onFailure(Call<List<Banner>> call, Throwable t) {
                android.util.Log.e("BannerManager", "Failed to fetch banners", t);
            }
        });
    }

    public static void startPolling(android.os.Handler handler, Runnable runnable, long intervalMs) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runnable.run();
                handler.postDelayed(this, intervalMs);
            }
        }, intervalMs);
    }

    // Subscribe to Server-Sent Events to get real-time banner updates
    public static void subscribeRealtime(Context context, BannerCallback callback) {
        try {
            lastContext = context;
            lastCallback = callback;
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
            EventSource.Factory factory = EventSources.createFactory(client);
            String base = com.example.app.utils.UrlUtils.getFullUrl(context, "banners/stream.php");
            Request request = new Request.Builder().url(base).build();
            // Close previous source if any
            if (currentSource != null) {
                currentSource.cancel();
                currentSource = null;
            }
            currentSource = factory.newEventSource(request, new EventSourceListener() {
                @Override
                public void onOpen(EventSource eventSource, okhttp3.Response response) {
                    Log.d("BannerManager", "SSE open");
                    if (!pollingFallback) {
                        failureCount = 0;
                    } else {
                        sseStableStartTs = System.currentTimeMillis();
                        sseHandler.postDelayed(() -> {
                            if (pollingFallback && (System.currentTimeMillis() - sseStableStartTs) >= SSE_COOLDOWN_MS) {
                                Log.d("BannerManager", "SSE stable, leaving polling fallback");
                                pollingFallback = false;
                                failureCount = 0;
                            }
                        }, SSE_COOLDOWN_MS);
                    }
                }
                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    if ("banners".equals(type) && data != null) {
                        try {
                            Gson gson = new Gson();
                            java.lang.reflect.Type t = new com.google.gson.reflect.TypeToken<java.util.List<Banner>>(){}.getType();
                            List<Banner> banners = gson.fromJson(data, t);
                            saveBannersToCache(context, banners);
                            callback.onBannersLoaded(banners);
                        } catch (Exception e) {
                            Log.e("BannerManager", "Parse SSE data failed", e);
                        }
                    }
                }
                @Override
                public void onClosed(EventSource eventSource) {
                    Log.d("BannerManager", "SSE closed");
                    try { eventSource.cancel(); } catch (Exception ignored) {}
                    currentSource = null;
                    if (lastContext != null && lastCallback != null && !pollingFallback) {
                        int delay = Math.min(15000, 2000 * Math.max(1, failureCount));
                        sseHandler.postDelayed(() -> subscribeRealtime(lastContext, lastCallback), delay);
                    }
                }
                @Override
                public void onFailure(EventSource eventSource, Throwable t, okhttp3.Response response) {
                    if (response != null) {
                        try { response.close(); } catch (Exception ignored) {}
                    }
                    if (t instanceof java.net.SocketException && t.getMessage() != null && t.getMessage().contains("Socket closed")) {
                        Log.d("BannerManager", "SSE closed by server, will reconnect");
                        try { eventSource.cancel(); } catch (Exception ignored) {}
                        currentSource = null;
                        if (lastContext != null && lastCallback != null) {
                            sseHandler.postDelayed(() -> subscribeRealtime(lastContext, lastCallback), 2000);
                        }
                        return;
                    }
                    Log.e("BannerManager", "SSE failure", t);
                    try { eventSource.cancel(); } catch (Exception ignored) {}
                    currentSource = null;
                    long now = System.currentTimeMillis();
                    if (now - lastFailureTs > 60000) {
                        failureCount = 0;
                    }
                    lastFailureTs = now;
                    failureCount++;
                    if (failureCount >= 3) {
                        pollingFallback = true;
                        Log.w("BannerManager", "Switching to polling fallback due to repeated SSE failures");
                        if (currentSource != null) { try { currentSource.cancel(); } catch (Exception ignored) {} currentSource = null; }
                        startPolling(sseHandler, () -> loadBanners(lastContext, true, lastCallback), 5000);
                    } else if (lastContext != null && lastCallback != null && !pollingFallback) {
                        int delay = Math.min(15000, 2000 * failureCount);
                        sseHandler.postDelayed(() -> subscribeRealtime(lastContext, lastCallback), delay);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("BannerManager", "Subscribe SSE error", e);
        }
    }

    private static void saveBannersToCache(Context context, List<Banner> banners) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(banners);
        editor.putString(KEY_BANNERS, json);
        editor.apply();
    }

    private static List<Banner> getCachedBanners(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_BANNERS, null);
        if (json == null) return null;
        
        Gson gson = new Gson();
        Type type = new TypeToken<List<Banner>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
