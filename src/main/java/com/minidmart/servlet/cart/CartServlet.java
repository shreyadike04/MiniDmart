package com.minidmart.servlet.cart;

import com.minidmart.dao.CartDao;
import com.minidmart.model.CartItem;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CartServlet extends HttpServlet {

    private final CartDao cartDao = new CartDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtil.hasRole(request, "CUSTOMER")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int userId = SessionUtil.getUserId(request);
        try {
            List<CartItem> items = cartDao.listItems(userId);
            BigDecimal subtotal = BigDecimal.ZERO;
            for (CartItem item : items) {
                subtotal = subtotal.add(item.getLineTotal());
            }
            request.setAttribute("items", items);
            request.setAttribute("subtotal", subtotal);
            request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
            request.setAttribute("pageTitle", "Your Cart - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/cart/cart.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtil.hasRole(request, "CUSTOMER")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!CsrfUtil.isValid(request)) {
            FlashUtil.error(request, "Your session expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        int userId = SessionUtil.getUserId(request);
        String pathInfo = request.getPathInfo();
        int productId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("productId"), -1);

        try {
            if (productId < 0) {
                FlashUtil.error(request, "Invalid product.");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }
            if ("/add".equals(pathInfo)) {
                int quantity = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("quantity"), 1);
                int newQty = cartDao.addItem(userId, productId, quantity);
                if (newQty <= 0) {
                    FlashUtil.error(request, "That product is out of stock.");
                } else if (newQty < quantity) {
                    FlashUtil.success(request, "Only " + newQty + " available — added " + newQty + " to cart.");
                } else {
                    FlashUtil.success(request, "Added to cart.");
                }
                response.sendRedirect(resolveReturnUrl(request));
            } else if ("/update".equals(pathInfo)) {
                int quantity = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("quantity"), 0);
                cartDao.updateQuantity(userId, productId, quantity);
                response.sendRedirect(request.getContextPath() + "/cart");
            } else if ("/remove".equals(pathInfo)) {
                cartDao.removeItem(userId, productId);
                FlashUtil.success(request, "Removed from cart.");
                response.sendRedirect(request.getContextPath() + "/cart");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private String resolveReturnUrl(HttpServletRequest request) {
        String redirect = request.getParameter("redirect");
        String contextPath = request.getContextPath();
        if (redirect != null && redirect.startsWith("/") && !redirect.startsWith("//")) {
            return contextPath + redirect;
        }
        return contextPath + "/cart";
    }
}
