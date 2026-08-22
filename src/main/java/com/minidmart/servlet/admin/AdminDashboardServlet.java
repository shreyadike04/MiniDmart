package com.minidmart.servlet.admin;

import com.minidmart.dao.*;
import com.minidmart.model.Order;
import com.minidmart.model.OrderStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class AdminDashboardServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();
    private final ProductDao productDao = new ProductDao();
    private final OrderDao orderDao = new OrderDao();
    private final ReturnDao returnDao = new ReturnDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Order> orders = orderDao.listAll();
            BigDecimal revenue = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            request.setAttribute("totalUsers", userDao.listAll().size());
            request.setAttribute("totalProducts", productDao.listAll().size());
            request.setAttribute("totalOrders", orders.size());
            request.setAttribute("completedRevenue", revenue);
            request.setAttribute("lowStockCount", productDao.listLowStock().size());
            request.setAttribute("pendingReturns", returnDao.listPending().size());
            request.setAttribute("recentOrders", orders.subList(0, Math.min(10, orders.size())));
            request.setAttribute("pageTitle", "Admin Dashboard - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
