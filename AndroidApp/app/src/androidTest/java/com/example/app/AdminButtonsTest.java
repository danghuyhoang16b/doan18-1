package com.example.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.ComponentName;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.intent.Intents;

import com.example.app.activities.AdminBannerActivity;
import com.example.app.activities.AdminHomeActivity;
import com.example.app.activities.AdminRedCommitteeActivity;
import com.example.app.activities.AdminViolationStatsActivity;
import com.example.app.utils.SharedPrefsUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminButtonsTest {
    @Before
    public void setUpRole() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        com.example.app.models.User u = new com.example.app.models.User();
        u.setRole("admin");
        u.setFullName("Admin Test");
        SharedPrefsUtils.saveUser(ctx, u);
        SharedPrefsUtils.saveToken(ctx, "dummy");
        Intents.init();
    }

    @After
    public void tearDown() {
        UiTestReport.writeReport();
        Intents.release();
    }

    @Test
    public void testManageBannersCard() {
        try {
            androidx.test.core.app.ActivityScenario.launch(AdminHomeActivity.class);
            onView(withId(R.id.btnManageBanners)).perform(click());
            intended(hasComponent(new ComponentName(InstrumentationRegistry.getInstrumentation().getTargetContext(), AdminBannerActivity.class)));
            UiTestReport.record("btnManageBanners", "AdminBannerActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("btnManageBanners", "AdminBannerActivity", false, t.getMessage());
        }
    }

    @Test
    public void testViolationStatsCard() {
        try {
            androidx.test.core.app.ActivityScenario.launch(AdminHomeActivity.class);
            onView(withId(R.id.btnSystemStats)).perform(click());
            intended(hasComponent(new ComponentName(InstrumentationRegistry.getInstrumentation().getTargetContext(), AdminViolationStatsActivity.class)));
            UiTestReport.record("btnSystemStats", "AdminViolationStatsActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("btnSystemStats", "AdminViolationStatsActivity", false, t.getMessage());
        }
    }

    @Test
    public void testRedCommitteeCard() {
        try {
            androidx.test.core.app.ActivityScenario.launch(AdminHomeActivity.class);
            onView(withId(R.id.btnManageRedCommittee)).perform(click());
            intended(hasComponent(new ComponentName(InstrumentationRegistry.getInstrumentation().getTargetContext(), AdminRedCommitteeActivity.class)));
            UiTestReport.record("btnManageRedCommittee", "AdminRedCommitteeActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("btnManageRedCommittee", "AdminRedCommitteeActivity", false, t.getMessage());
        }
    }
}
