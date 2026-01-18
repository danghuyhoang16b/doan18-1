package com.example.app;

import android.app.Application;
import android.util.Log;

import com.example.app.network.ApiClient;

/**
 * Application class for initializing global components
 *
 * This class is called when the app starts, before any Activity is created.
 * Used for:
 * - Initializing ApiClient with application context
 * - Setting up any global configurations
 */
public class SchoolApp extends Application {

    private static final String TAG = "SchoolApp";

    private static SchoolApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize ApiClient with application context
        ApiClient.init(this);

        Log.d(TAG, "SchoolApp initialized");
    }

    /**
     * Get application instance
     * Useful for accessing application context from anywhere
     */
    public static SchoolApp getInstance() {
        return instance;
    }
}
