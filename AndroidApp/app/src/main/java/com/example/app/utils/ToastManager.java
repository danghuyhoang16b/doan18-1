package com.example.app.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastManager {
    private static Toast current;
    private static long windowStart = 0L;
    private static int countInWindow = 0;
    private static final long WINDOW_MS = 4000L;
    private static final int MAX_TOASTS = 3;
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void show(Context ctx, String msg) {
        long now = System.currentTimeMillis();
        if (now - windowStart > WINDOW_MS) {
            windowStart = now;
            countInWindow = 0;
        }
        if (countInWindow >= MAX_TOASTS) {
            return;
        }
        handler.post(() -> {
            if (current != null) {
                current.cancel();
            }
            current = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
            current.show();
        });
        countInWindow++;
    }
}
