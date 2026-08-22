package com.minidmart.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** Reads the small, fixed set of session attributes set at login by LoginServlet:
 *  userId (Integer), role (String: CUSTOMER/STAFF/ADMIN), fullName (String), email (String). */
public final class SessionUtil {

    private SessionUtil() {
    }

    public static Integer getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Integer) session.getAttribute("userId");
    }

    public static String getRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute("role");
    }

    public static String getFullName(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute("fullName");
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return getUserId(request) != null;
    }

    public static boolean hasRole(HttpServletRequest request, String... roles) {
        String role = getRole(request);
        if (role == null) return false;
        for (String r : roles) {
            if (r.equals(role)) return true;
        }
        return false;
    }
}
