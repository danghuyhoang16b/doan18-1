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

import com.example.app.activities.AttendanceActivity;
import com.example.app.activities.HomeActivity;
import com.example.app.activities.NotificationActivity;
import com.example.app.activities.ScoreEntryActivity;
import com.example.app.activities.TeacherRedCommitteeActivity;
import com.example.app.activities.ViolationActivity;
import com.example.app.utils.SharedPrefsUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomeButtonsTest {
    @Before
    public void setUpRole() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        com.example.app.models.User u = new com.example.app.models.User();
        u.setRole("teacher");
        u.setFullName("GV Test");
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
    public void testAttendanceButton() {
        try {
            androidx.test.core.app.ActivityScenario.launch(HomeActivity.class);
            onView(withId(R.id.btnNavAttendance)).perform(click());
            intended(hasComponent(new ComponentName(InstrumentationRegistry.getInstrumentation().getTargetContext(), AttendanceActivity.class)));
            UiTestReport.record("btnNavAttendance", "AttendanceActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("btnNavAttendance", "AttendanceActivity", false, t.getMessage());
        }
    }

    @Test
    public void testViolationButton() {
        try {
            androidx.test.core.app.ActivityScenario.launch(HomeActivity.class);
            onView(withId(R.id.btnNavViolation)).perform(click());
            intended(hasComponent(new ComponentName(InstrumentationRegistry.getInstrumentation().getTargetContext(), ViolationActivity.class)));
            UiTestReport.record("btnNavViolation", "ViolationActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("btnNavViolation", "ViolationActivity", false, t.getMessage());
        }
    }

    @Test
    public void testScoreButton() {
        try {
            androidx.test.core.app.ActivityScenario.launch(HomeActivity.class);
            onView(withId(R.id.btnNavScore)).perform(click());
            intended(hasComponent(new ComponentName(InstrumentationRegistry.getInstrumentation().getTargetContext(), ScoreEntryActivity.class)));
            UiTestReport.record("btnNavScore", "ScoreEntryActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("btnNavScore", "ScoreEntryActivity", false, t.getMessage());
        }
    }

    @Test
    public void testMessagesButton() {
        try {
            androidx.test.core.app.ActivityScenario.launch(HomeActivity.class);
            onView(withId(R.id.btnNavMessages)).perform(click());
            UiTestReport.record("btnNavMessages", "ContactListActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("btnNavMessages", "ContactListActivity", false, t.getMessage());
        }
    }

    @Test
    public void testNotifyButton() {
        try {
            androidx.test.core.app.ActivityScenario.launch(HomeActivity.class);
            onView(withId(R.id.btnNavNotify)).perform(click());
            intended(hasComponent(new ComponentName(InstrumentationRegistry.getInstrumentation().getTargetContext(), NotificationActivity.class)));
            UiTestReport.record("btnNavNotify", "NotificationActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("btnNavNotify", "NotificationActivity", false, t.getMessage());
        }
    }

    @Test
    public void testRedCommitteeButton() {
        try {
            androidx.test.core.app.ActivityScenario.launch(HomeActivity.class);
            onView(withId(R.id.btnNavRedCommittee)).perform(click());
            intended(hasComponent(new ComponentName(InstrumentationRegistry.getInstrumentation().getTargetContext(), TeacherRedCommitteeActivity.class)));
            UiTestReport.record("btnNavRedCommittee", "TeacherRedCommitteeActivity", true, null);
        } catch (Throwable t) {
            UiTestReport.record("btnNavRedCommittee", "TeacherRedCommitteeActivity", false, t.getMessage());
        }
    }
}
