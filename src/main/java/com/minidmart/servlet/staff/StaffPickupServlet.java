package com.minidmart.servlet.staff;

import com.minidmart.dao.OrderDao;
import com.minidmart.dao.PickupSlotDao;
import com.minidmart.model.Order;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

/**
 * "Who's picking up when" view for staff. OrderDao.listUpcomingPickups() does not
 * eagerly load pickup slot detail (only OrderDao.findById does, via loadExtras), so
 * we enrich each row with a cheap single-row PickupSlotDao lookup and sort by slot
 * date/time. We deliberately don't show item counts here (that would require a full
 * per-order reload / N+1) - full detail is one click away via /staff/orders?id=N.
 */
public class StaffPickupServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDao();
    private final PickupSlotDao slotDao = new PickupSlotDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Order> orders = orderDao.listUpcomingPickups();
            for (Order o : orders) {
                if (o.getPickupSlotId() != null) {
                    slotDao.findById(o.getPickupSlotId()).ifPresent(o::setPickupSlot);
                }
            }
            orders.sort(Comparator
                    .comparing((Order o) -> o.getPickupSlot() != null ? o.getPickupSlot().getSlotDate() : LocalDate.MAX)
                    .thenComparing(o -> o.getPickupSlot() != null ? o.getPickupSlot().getStartTime() : LocalTime.MAX));

            request.setAttribute("orders", orders);
            request.setAttribute("pageTitle", "Upcoming Pickups - Staff - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/staff/pickups.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
