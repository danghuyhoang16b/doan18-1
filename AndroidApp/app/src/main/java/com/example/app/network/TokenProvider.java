package com.example.app.network;

/**
 * Interface for providing authentication token
 * This allows for easy testing and decoupling from SharedPreferences
 */
public interface TokenProvider {
    /**
     * Get the current authentication token
     * @return the JWT token or null if not logged in
     */
    String getToken();

    /**
     * Check if user is logged in
     * @return true if token exists
     */
    boolean isLoggedIn();
}
