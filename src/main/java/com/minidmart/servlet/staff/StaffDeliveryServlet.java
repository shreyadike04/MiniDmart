package com.minidmart.servlet.staff;

import com.minidmart.dao.AddressDao;
import com.minidmart.dao.OrderDao;
import com.minidmart.model.Order;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Active deliveries view for staff. OrderDao.listDeliveries() does not eagerly load
 * the delivery address (only OrderDao.findById does, via loadExtras), so we enrich
 * each row with a cheap single-row AddressDao lookup to show the city. Full order
 * detail is one click away via /staff/orders?id=N.
 */
public class StaffDeliveryServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDao();
    private final AddressDao addressDao = new AddressDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Order> orders = orderDao.listDeliveries();
            for (Order o : orders) {
                if (o.getDeliveryAddressId() != null) {
                    addressDao.findById(o.getDeliveryAddressId()).ifPresent(o::setDeliveryAddress);
                }
            }
            request.setAttribute("orders", orders);
            request.setAttribute("pageTitle", "Deliveries - Staff - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/staff/deliveries.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
