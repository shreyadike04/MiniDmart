package com.minidmart.util;

import javax.servlet.http.HttpServletRequest;

/** Post-redirect-get flash messages: stash a message in the session right before
 *  a sendRedirect, and FlashFilter promotes it to a one-time request attribute
 *  on the very next request, then discards it. */
public final class FlashUtil {

    private FlashUtil() {
    }

    public static void success(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashSuccess", message);
    }

    public static void error(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashError", message);
    }
}
