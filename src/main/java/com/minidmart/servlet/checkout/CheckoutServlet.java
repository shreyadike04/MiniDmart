package com.minidmart.servlet.checkout;

import com.minidmart.dao.AddressDao;
import com.minidmart.dao.CartDao;
import com.minidmart.dao.OrderDao;
import com.minidmart.dao.PickupSlotDao;
import com.minidmart.model.CartItem;
import com.minidmart.model.FulfillmentType;
import com.minidmart.model.Order;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CheckoutServlet extends HttpServlet {

    public static final BigDecimal DELIVERY_FEE = new BigDecimal("40.00");

    private final CartDao cartDao = new CartDao();
    private final PickupSlotDao slotDao = new PickupSlotDao();
    private final AddressDao addressDao = new AddressDao();
    private final OrderDao orderDao = new OrderDao();

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
            if (items.isEmpty()) {
                FlashUtil.error(request, "Your cart is empty.");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }
            BigDecimal subtotal = items.stream()
                    .map(CartItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            request.setAttribute("items", items);
            request.setAttribute("subtotal", subtotal);
            request.setAttribute("deliveryFee", DELIVERY_FEE);
            request.setAttribute("slots", slotDao.listUpcoming());
            request.setAttribute("addresses", addressDao.listByUser(userId));
            request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
            request.setAttribute("pageTitle", "Checkout - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/checkout/checkout.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/checkout");
            return;
        }
        int userId = SessionUtil.getUserId(request);
        String fulfillmentTypeParam = request.getParameter("fulfillmentType");
        FulfillmentType type;
        try {
            type = FulfillmentType.valueOf(fulfillmentTypeParam);
        } catch (Exception e) {
            FlashUtil.error(request, "Please choose a fulfillment method.");
            response.sendRedirect(request.getContextPath() + "/checkout");
            return;
        }
        Integer pickupSlotId = null;
        Integer deliveryAddressId = null;
        if (type == FulfillmentType.PICKUP) {
            int slotId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("pickupSlotId"), -1);
            if (slotId > 0) pickupSlotId = slotId;
        } else {
            int addressId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("addressId"), -1);
            if (addressId > 0) deliveryAddressId = addressId;
        }

        try {
            Order order = orderDao.placeOrder(userId, type, pickupSlotId, deliveryAddressId, DELIVERY_FEE);
            AuditLogger.log(userId, "ORDER_PLACED", "ORDER", order.getOrderId(),
                    order.getOrderNumber(), request);
            FlashUtil.success(request, "Order " + order.getOrderNumber() + " placed!");
            response.sendRedirect(request.getContextPath() + "/orders/view?id=" + order.getOrderId());
        } catch (BusinessRuleException e) {
            FlashUtil.error(request, e.getMessage());
            response.sendRedirect(request.getContextPath() + "/checkout");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
