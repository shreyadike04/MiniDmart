package com.minidmart.servlet.staff;

import com.minidmart.dao.ProductDao;
import com.minidmart.dao.StockMovementDao;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class StaffInventoryServlet extends HttpServlet {

    private final ProductDao productDao = new ProductDao();
    private final StockMovementDao stockMovementDao = new StockMovementDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("products", productDao.listAll());
            request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
            request.setAttribute("pageTitle", "Inventory - Staff - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/staff/inventory.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"/adjust".equals(request.getPathInfo())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!CsrfUtil.isValid(request)) {
            FlashUtil.error(request, "Your session expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/staff/inventory");
            return;
        }

        int productId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("productId"), -1);
        String reason = request.getParameter("reason");
        int delta;
        try {
            delta = Integer.parseInt(request.getParameter("delta").trim());
        } catch (Exception e) {
            FlashUtil.error(request, "Please enter a valid whole-number adjustment.");
            response.sendRedirect(request.getContextPath() + "/staff/inventory");
            return;
        }
        if (productId < 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (delta == 0) {
            FlashUtil.error(request, "Enter a non-zero adjustment.");
            response.sendRedirect(request.getContextPath() + "/staff/inventory");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int newQty = productDao.adjustStock(conn, productId, delta);
                if (newQty < 0) {
                    conn.rollback();
                    FlashUtil.error(request, "Adjustment would make stock negative.");
                } else {
                    stockMovementDao.record(conn, productId, delta, "MANUAL_ADJUST", "MANUAL", null,
                            SessionUtil.getUserId(request));
                    conn.commit();
                    AuditLogger.log(SessionUtil.getUserId(request), "STOCK_ADJUSTED", "PRODUCT", productId,
                            "delta=" + delta + " reason=" + reason, request);
                    FlashUtil.success(request, "Stock updated.");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        response.sendRedirect(request.getContextPath() + "/staff/inventory");
    }
}
