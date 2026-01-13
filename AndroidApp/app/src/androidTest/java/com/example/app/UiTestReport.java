package com.example.app;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UiTestReport {
    public static class Item {
        public String buttonName;
        public String activityName;
        public boolean passed;
        public String error;
    }
    private static final List<Item> items = new ArrayList<>();

    public static synchronized void record(String buttonName, String activityName, boolean passed, String error) {
        Item i = new Item();
        i.buttonName = buttonName;
        i.activityName = activityName;
        i.passed = passed;
        i.error = error;
        items.add(i);
        Log.d("UiTestReport", (passed ? "PASS " : "FAIL ") + buttonName + " -> " + activityName + (error != null ? (" | " + error) : ""));
    }

    public static synchronized void writeReport() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        StringBuilder sb = new StringBuilder();
        sb.append("# Báo cáo kiểm thử UI\n\n");
        for (Item i: items) {
            sb.append("- ").append(i.passed ? "[PASS] " : "[FAIL] ")
              .append(i.buttonName).append(" | Activity: ").append(i.activityName);
            if (i.error != null) sb.append(" | Error: ").append(i.error);
            sb.append("\n");
        }
        try {
            File f = new File(ctx.getCacheDir(), "report_ui_buttons.md");
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
            Log.d("UiTestReport", "Report written to: " + f.getAbsolutePath());
        } catch (Exception e) {
            Log.e("UiTestReport", "Write report failed", e);
        }
    }
}
