package com.minidmart.filter;

import com.minidmart.dao.CartDao;
import com.minidmart.util.SessionUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/** Makes the cart item count available to every JSP as request attribute "cartCount",
 *  so the navbar badge doesn't need its own DB call. Mapped to /* in web.xml. */
public class CartCountFilter implements Filter {

    private final CartDao cartDao = new CartDao();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        Integer userId = SessionUtil.getUserId(request);
        if (userId != null && "CUSTOMER".equals(SessionUtil.getRole(request))) {
            try {
                request.setAttribute("cartCount", cartDao.countItems(userId));
            } catch (Exception e) {
                request.setAttribute("cartCount", 0);
            }
        }
        chain.doFilter(req, res);
    }
}
