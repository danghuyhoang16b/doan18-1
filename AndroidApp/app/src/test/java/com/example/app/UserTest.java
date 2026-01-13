package com.example.app;

import org.junit.Test;
import static org.junit.Assert.*;
import com.example.app.models.User;

public class UserTest {

    @Test
    public void testUserRoleAndRedStar() {
        User user = new User();
        user.setRole("student");
        user.setIsRedStar(1);
        
        assertEquals("student", user.getRole());
        assertTrue("Student should be Red Star", user.isRedStar());
        
        user.setIsRedStar(0);
        assertFalse("Student should NOT be Red Star", user.isRedStar());
    }

    @Test
    public void testUserLockStatus() {
        User user = new User();
        user.setIsLocked(1);
        assertTrue(user.isLocked());
        
        user.setIsLocked(0);
        assertFalse(user.isLocked());
    }
}
