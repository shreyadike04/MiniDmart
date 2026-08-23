package com.minidmart.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Baseline security headers on every response. Mapped to /* in web.xml. */
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");
        // 'unsafe-inline' is needed for both script-src and style-src: several JSPs
        // use small inline <script> blocks (e.g. the exchange-type toggle) and
        // inline onclick="" confirm() dialogs, plus inline style="" attributes.
        // Without it the browser silently drops all of that — a strict nonce-based
        // CSP would be the production-grade fix but is out of scope here.
        // fonts.googleapis.com/fonts.gstatic.com are allowlisted for the Google
        // Fonts webfont linked in header.jsp (stylesheet + the actual font file).
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; img-src 'self' data: https:; "
                        + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                        + "font-src 'self' https://fonts.gstatic.com; "
                        + "script-src 'self' 'unsafe-inline'");
        chain.doFilter(req, res);
    }
}
