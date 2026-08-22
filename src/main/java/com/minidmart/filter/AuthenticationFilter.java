package com.minidmart.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Requires a logged-in user for anything under a protected path. Mapped in
 * web.xml (no annotations) to /cart/*, /checkout/*, /orders/*, /returns/*,
 * /profile/*, /staff/*, /admin/*.
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            String requested = request.getRequestURI().substring(request.getContextPath().length());
            response.sendRedirect(request.getContextPath() + "/login?redirect="
                    + java.net.URLEncoder.encode(requested, "UTF-8"));
            return;
        }
        chain.doFilter(req, res);
    }
}
