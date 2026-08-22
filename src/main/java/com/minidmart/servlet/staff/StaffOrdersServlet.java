package com.minidmart.servlet.staff;

import com.minidmart.dao.OrderDao;
import com.minidmart.model.FulfillmentType;
import com.minidmart.model.Order;
import com.minidmart.model.OrderStatus;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class StaffOrdersServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String statusParam = request.getParameter("status");
        try {
            if (!ValidationUtil.isBlank(idParam)) {
                int orderId = ValidationUtil.parsePositiveIntOrDefault(idParam, -1);
                Optional<Order> orderOpt = orderId < 0 ? Optional.empty() : orderDao.findById(orderId);
                if (orderOpt.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                Order order = orderOpt.get();
                request.setAttribute("order", order);
                request.setAttribute("nextStatus", nextStatus(order.getStatus(), order.getFulfillmentType()));
                request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
            } else {
                if (!ValidationUtil.isBlank(statusParam)) {
                    request.setAttribute("orders", orderDao.listByStatus(OrderStatus.valueOf(statusParam)));
                } else {
                    request.setAttribute("orders", orderDao.listAll());
                }
                request.setAttribute("selectedStatus", statusParam);
                request.setAttribute("statuses", OrderStatus.values());
            }
            request.setAttribute("pageTitle", "Orders - Staff - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/staff/orders.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValid(request)) {
            FlashUtil.error(request, "Your session expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/staff/orders");
            return;
        }
        int orderId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("orderId"), -1);
        String newStatusParam = request.getParameter("newStatus");

        try {
            Optional<Order> orderOpt = orderId < 0 ? Optional.empty() : orderDao.findById(orderId);
            if (orderOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Order order = orderOpt.get();
            OrderStatus oldStatus = order.getStatus();
            OrderStatus expectedNext = nextStatus(oldStatus, order.getFulfillmentType());

            OrderStatus newStatus;
            try {
                newStatus = ValidationUtil.isBlank(newStatusParam) ? null : OrderStatus.valueOf(newStatusParam);
            } catch (IllegalArgumentException e) {
                newStatus = null;
            }

            if (expectedNext == null || newStatus != expectedNext) {
                FlashUtil.error(request, "Invalid status transition.");
            } else {
                orderDao.updateStatus(orderId, newStatus);
                AuditLogger.log(SessionUtil.getUserId(request), "ORDER_STATUS_CHANGED", "ORDER", orderId,
                        oldStatus + "->" + newStatus, request);
                FlashUtil.success(request, "Order status updated to " + newStatus + ".");
            }
            response.sendRedirect(request.getContextPath() + "/staff/orders?id=" + orderId);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /** Legal forward path: PLACED -> CONFIRMED -> PREPARING -> (READY_FOR_PICKUP | OUT_FOR_DELIVERY) -> COMPLETED. */
    private OrderStatus nextStatus(OrderStatus current, FulfillmentType type) {
        switch (current) {
            case PLACED: return OrderStatus.CONFIRMED;
            case CONFIRMED: return OrderStatus.PREPARING;
            case PREPARING: return type == FulfillmentType.PICKUP ? OrderStatus.READY_FOR_PICKUP : OrderStatus.OUT_FOR_DELIVERY;
            case READY_FOR_PICKUP:
            case OUT_FOR_DELIVERY:
                return OrderStatus.COMPLETED;
            default:
                return null;
        }
    }
}
