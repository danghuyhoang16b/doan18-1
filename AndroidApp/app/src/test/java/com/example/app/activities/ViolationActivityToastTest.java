package com.example.app.activities;

import static org.junit.Assert.assertEquals;

import android.view.View;
import android.widget.Spinner;

import androidx.test.core.app.ApplicationProvider;
import com.example.app.R;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowToast;

public class ViolationActivityToastTest {
    @Test
    public void submitWithoutSelections_showsWarningToast() {
        com.example.app.utils.SharedPrefsUtils.saveToken(ApplicationProvider.getApplicationContext(), "dummy");
        ActivityController<ViolationActivity> c = Robolectric.buildActivity(ViolationActivity.class).create().start();
        ViolationActivity a = c.get();
        Spinner spRules = a.findViewById(R.id.spinnerRules);
        if (spRules != null) spRules.setSelection(0); // May be empty; the activity checks nulls
        a.findViewById(R.id.btnSubmit).performClick();
        String latest = ShadowToast.getTextOfLatestToast();
        assertEquals("Vui lòng chọn đầy đủ thông tin", latest);
    }
}
