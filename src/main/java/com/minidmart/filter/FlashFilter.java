package com.minidmart.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** Promotes one-shot flashSuccess/flashError session messages (set via FlashUtil
 *  before a redirect) to request attributes, then removes them from the session
 *  so a page refresh doesn't repeat the message. Mapped to /* in web.xml. */
public class FlashFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object success = session.getAttribute("flashSuccess");
            Object error = session.getAttribute("flashError");
            if (success != null) {
                request.setAttribute("flashSuccess", success);
                session.removeAttribute("flashSuccess");
            }
            if (error != null) {
                request.setAttribute("flashError", error);
                session.removeAttribute("flashError");
            }
        }
        chain.doFilter(req, res);
    }
}
