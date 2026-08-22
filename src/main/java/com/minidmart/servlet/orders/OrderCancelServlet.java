package com.minidmart.servlet.orders;

import com.minidmart.dao.OrderDao;
import com.minidmart.model.Order;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class OrderCancelServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtil.hasRole(request, "CUSTOMER")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int userId = SessionUtil.getUserId(request);
        int orderId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("orderId"), -1);
        String reason = request.getParameter("reason");
        if (reason != null) reason = reason.trim();

        if (!CsrfUtil.isValid(request)) {
            FlashUtil.error(request, "Your session expired. Please try again.");
            response.sendRedirect(request.getContextPath()
                    + (orderId > 0 ? "/orders/view?id=" + orderId : "/orders"));
            return;
        }

        try {
            Optional<Order> order = orderId < 0 ? Optional.empty() : orderDao.findById(orderId);
            if (order.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (order.get().getUserId() != userId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            try {
                orderDao.cancelOrder(orderId, reason);
                AuditLogger.log(userId, "ORDER_CANCELLED", "ORDER", orderId, reason, request);
                FlashUtil.success(request, "Order cancelled.");
            } catch (BusinessRuleException e) {
                FlashUtil.error(request, e.getMessage());
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        response.sendRedirect(request.getContextPath() + "/orders/view?id=" + orderId);
    }
}
