package com.minidmart.servlet.orders;

import com.minidmart.dao.OrderDao;
import com.minidmart.model.Order;
import com.minidmart.util.CsrfUtil;
import com.minidmart.util.SessionUtil;
import com.minidmart.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class OrderDetailServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtil.hasRole(request, "CUSTOMER")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int id = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("id"), -1);
        try {
            Optional<Order> order = id < 0 ? Optional.empty() : orderDao.findById(id);
            if (order.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (order.get().getUserId() != SessionUtil.getUserId(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            request.setAttribute("order", order.get());
            request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
            request.setAttribute("pageTitle", "Order " + order.get().getOrderNumber() + " - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/orders/order-detail.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
