package com.minidmart.servlet.staff;

import com.minidmart.dao.OrderDao;
import com.minidmart.dao.ProductDao;
import com.minidmart.dao.ReturnDao;
import com.minidmart.model.Order;
import com.minidmart.model.OrderStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class StaffDashboardServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDao();
    private final ProductDao productDao = new ProductDao();
    private final ReturnDao returnDao = new ReturnDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Order> allOrders = orderDao.listAll();
            long placed = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PLACED).count();
            long preparing = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PREPARING
                    || o.getStatus() == OrderStatus.CONFIRMED).count();
            long readyOrOut = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.READY_FOR_PICKUP
                    || o.getStatus() == OrderStatus.OUT_FOR_DELIVERY).count();

            request.setAttribute("newOrders", placed);
            request.setAttribute("inProgressOrders", preparing);
            request.setAttribute("readyOrders", readyOrOut);
            request.setAttribute("upcomingPickups", orderDao.listUpcomingPickups().size());
            request.setAttribute("activeDeliveries", orderDao.listDeliveries().size());
            request.setAttribute("lowStockCount", productDao.listLowStock().size());
            request.setAttribute("pendingReturns", returnDao.listPending().size());
            request.setAttribute("recentOrders", allOrders.subList(0, Math.min(10, allOrders.size())));
            request.setAttribute("pageTitle", "Staff Dashboard - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/staff/dashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
