package com.minidmart.servlet.returns;

import com.minidmart.dao.ProductDao;
import com.minidmart.dao.ReturnDao;
import com.minidmart.model.ReturnRequest;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturnsServlet extends HttpServlet {

    private final ReturnDao returnDao = new ReturnDao();
    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtil.hasRole(request, "CUSTOMER")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int userId = SessionUtil.getUserId(request);
        String pathInfo = request.getPathInfo();

        try {
            if ("/new".equals(pathInfo)) {
                showForm(request, response, userId);
            } else {
                request.setAttribute("returns", returnDao.listByUser(userId));
                request.setAttribute("pageTitle", "My Returns - Mini D-Mart");
                request.getRequestDispatcher("/WEB-INF/jsp/returns/return-list.jsp").forward(request, response);
            }
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
        if (!"/new".equals(request.getPathInfo())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!CsrfUtil.isValid(request)) {
            FlashUtil.error(request, "Your session expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/returns");
            return;
        }
        int userId = SessionUtil.getUserId(request);
        int orderItemId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("orderItemId"), -1);
        String type = request.getParameter("type");
        String reason = request.getParameter("reason");
        int quantity = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("quantity"), -1);
        String exchangeProductIdParam = request.getParameter("exchangeProductId");

        try {
            if (orderItemId < 0 || quantity < 0 || ValidationUtil.isBlank(reason)) {
                FlashUtil.error(request, "Please fill in all required fields.");
                response.sendRedirect(request.getContextPath() + "/returns/new?orderItemId=" + orderItemId);
                return;
            }
            ReturnRequest r = new ReturnRequest();
            r.setOrderItemId(orderItemId);
            r.setUserId(userId);
            r.setType(ReturnRequest.Type.valueOf(type));
            r.setReason(reason.trim());
            r.setQuantity(quantity);
            if (r.getType() == ReturnRequest.Type.EXCHANGE) {
                if (ValidationUtil.isBlank(exchangeProductIdParam)) {
                    FlashUtil.error(request, "Please choose a replacement product.");
                    response.sendRedirect(request.getContextPath() + "/returns/new?orderItemId=" + orderItemId);
                    return;
                }
                r.setExchangeProductId(Integer.valueOf(exchangeProductIdParam.trim()));
            }
            int returnId = returnDao.create(r);
            AuditLogger.log(userId, "RETURN_REQUESTED", "RETURN", returnId, type + " qty=" + quantity, request);
            FlashUtil.success(request, "Request submitted.");
            response.sendRedirect(request.getContextPath() + "/returns");
        } catch (BusinessRuleException e) {
            FlashUtil.error(request, e.getMessage());
            response.sendRedirect(request.getContextPath() + "/returns/new?orderItemId=" + orderItemId);
        } catch (IllegalArgumentException e) {
            FlashUtil.error(request, "Invalid request.");
            response.sendRedirect(request.getContextPath() + "/returns");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException, SQLException {
        int orderItemId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("orderItemId"), -1);
        if (orderItemId < 0) {
            FlashUtil.error(request, "That order item could not be found.");
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.product_name_snap, oi.quantity, "
                + "o.order_number, o.status FROM order_items oi JOIN orders o ON oi.order_id = o.order_id "
                + "WHERE oi.order_item_id = ? AND o.user_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderItemId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    FlashUtil.error(request, "That order item could not be found.");
                    response.sendRedirect(request.getContextPath() + "/orders");
                    return;
                }
                if (!"COMPLETED".equals(rs.getString("status"))) {
                    FlashUtil.error(request, "Only completed orders are eligible for return or exchange.");
                    response.sendRedirect(request.getContextPath() + "/orders");
                    return;
                }
                request.setAttribute("orderItemId", rs.getInt("order_item_id"));
                request.setAttribute("orderNumber", rs.getString("order_number"));
                request.setAttribute("productName", rs.getString("product_name_snap"));
                request.setAttribute("maxQuantity", rs.getInt("quantity"));
            }
        }
        request.setAttribute("exchangeProducts", productDao.search(null, null));
        request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
        request.setAttribute("pageTitle", "Request Return / Exchange - Mini D-Mart");
        request.getRequestDispatcher("/WEB-INF/jsp/returns/return-form.jsp").forward(request, response);
    }
}
