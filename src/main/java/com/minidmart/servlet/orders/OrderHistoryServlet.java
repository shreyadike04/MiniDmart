package com.minidmart.servlet.orders;

import com.minidmart.dao.OrderDao;
import com.minidmart.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class OrderHistoryServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtil.hasRole(request, "CUSTOMER")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            request.setAttribute("orders", orderDao.listByUser(SessionUtil.getUserId(request)));
            request.setAttribute("pageTitle", "My Orders - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/orders/order-list.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
