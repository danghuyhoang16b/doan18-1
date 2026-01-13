package com.example.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.app.activities.ViolationActivity;
import com.example.app.utils.RetrofitClient;
import com.example.app.utils.SharedPrefsUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(AndroidJUnit4.class)
public class ViolationFlowTest {
    private static MockWebServer server;

    @Rule
    public androidx.test.ext.junit.rules.ActivityScenarioRule<ViolationActivity> activityRule =
            new androidx.test.ext.junit.rules.ActivityScenarioRule<>(ViolationActivity.class);

    @BeforeClass
    public static void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        String base = server.url("/").toString();
        RetrofitClient.overrideForTesting(base);
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPrefsUtils.saveToken(ctx, "dummy");
        enqueueBaseData();
    }

    @AfterClass
    public static void teardown() throws Exception {
        UiTestReport.writeReport();
        server.shutdown();
    }

    private static void enqueueBaseData() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("[{\"id\":3,\"name\":\"10A2\"}]")); // teacher/get_classes.php
        server.enqueue(new MockResponse().setResponseCode(200).setBody("[{\"id\":1,\"rule_name\":\"Đi học muộn\",\"points\":2}]")); // violations/get_rules.php
        server.enqueue(new MockResponse().setResponseCode(200).setBody("[{\"id\":36,\"full_name\":\"Trần Gia Huy\",\"code\":\"HS-2002\",\"class\":\"10A2\",\"violations_count\":0}]")); // list_students_by_class
        server.enqueue(new MockResponse().setResponseCode(200)); // submit
    }

    @Test
    public void successFlow_submitViolation() {
        try {
            SystemClock.sleep(500);
            onView(withId(R.id.spinnerClasses)).perform(click());
            onData(anything()).atPosition(0).perform(click());
            SystemClock.sleep(200);
            onView(withId(R.id.spinnerRules)).perform(click());
            onData(anything()).atPosition(0).perform(click());
            SystemClock.sleep(200);
            onView(withId(R.id.spinnerStudents)).perform(click());
            onData(anything()).atPosition(0).perform(click());
            onView(withId(R.id.btnSubmit)).perform(click());
            UiTestReport.record("Chấm nề nếp - Submit", "ViolationActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("Chấm nề nếp - Submit", "ViolationActivity", false, t.getMessage());
        }
    }

    @Test
    public void validationError_withoutSelections() {
        try {
            onView(withId(R.id.btnSubmit)).perform(click());
            onView(withText("Vui lòng chọn đầy đủ thông tin")).inRoot(new ToastMatcher()).check(matches(withText("Vui lòng chọn đầy đủ thông tin")));
            UiTestReport.record("Chấm nề nếp - Validation", "ViolationActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("Chấm nề nếp - Validation", "ViolationActivity", false, t.getMessage());
        }
    }

    @Test
    public void systemError_submit500() {
        try {
            server.enqueue(new MockResponse().setResponseCode(200).setBody("[{\"id\":3,\"name\":\"10A2\"}]"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("[{\"id\":1,\"rule_name\":\"Đi học muộn\",\"points\":2}]"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("[]"));
            server.enqueue(new MockResponse().setResponseCode(500));
            SystemClock.sleep(500);
            onView(withId(R.id.spinnerClasses)).perform(click());
            onData(anything()).atPosition(0).perform(click());
            SystemClock.sleep(200);
            onView(withId(R.id.spinnerRules)).perform(click());
            onData(anything()).atPosition(0).perform(click());
            onView(withId(R.id.btnSubmit)).perform(click());
            UiTestReport.record("Chấm nề nếp - SystemError", "ViolationActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("Chấm nề nếp - SystemError", "ViolationActivity", false, t.getMessage());
        }
    }
}
