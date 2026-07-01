package com.cafepos.util;

import com.cafepos.model.User;

/**
 * Lightweight application-level session context.
 * Holds the currently logged-in user so all panels can access it.
 */
public final class AppContext {

    private static User currentUser;

    private AppContext() {}

    public static User getCurrentUser()        { return currentUser; }
    public static void setCurrentUser(User u)  { currentUser = u; }
    public static void clearSession()          { currentUser = null; }
    public static boolean isLoggedIn()         { return currentUser != null; }
}
