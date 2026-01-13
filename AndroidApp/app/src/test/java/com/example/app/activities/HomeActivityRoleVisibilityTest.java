package com.example.app.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.widget.Button;

import androidx.test.core.app.ApplicationProvider;

import com.example.app.R;
import com.example.app.utils.SharedPrefsUtils;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public class HomeActivityRoleVisibilityTest {
    @Test
    public void teacherRole_showsTeacherButtons() {
        com.example.app.models.User u = new com.example.app.models.User();
        u.setRole("teacher");
        u.setFullName("Test GV");
        SharedPrefsUtils.saveUser(ApplicationProvider.getApplicationContext(), u);
        ActivityController<HomeActivity> c = Robolectric.buildActivity(HomeActivity.class).create().start().resume().visible();
        HomeActivity a = c.get();
        Button btnScore = a.findViewById(R.id.btnNavScore);
        Button btnRed = a.findViewById(R.id.btnNavRedCommittee);
        assertNotNull(btnScore);
        assertEquals(android.view.View.VISIBLE, btnScore.getVisibility());
        assertNotNull(btnRed);
        assertEquals(android.view.View.VISIBLE, btnRed.getVisibility());
    }
}
