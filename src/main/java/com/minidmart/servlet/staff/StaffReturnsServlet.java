package com.minidmart.servlet.staff;

import com.minidmart.dao.ReturnDao;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class StaffReturnsServlet extends HttpServlet {

    private final ReturnDao returnDao = new ReturnDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String filter = request.getParameter("filter");
        try {
            if ("all".equals(filter)) {
                request.setAttribute("returns", returnDao.listAll());
            } else {
                filter = "pending";
                request.setAttribute("returns", returnDao.listPending());
            }
            request.setAttribute("filter", filter);
            request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
            request.setAttribute("pageTitle", "Returns & Exchanges - Staff - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/staff/returns.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValid(request)) {
            FlashUtil.error(request, "Your session expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/staff/returns");
            return;
        }

        int returnId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("returnId"), -1);
        String action = request.getParameter("action");
        String notes = request.getParameter("notes");

        if (returnId < 0 || (!"approve".equals(action) && !"reject".equals(action))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if ("reject".equals(action) && ValidationUtil.isBlank(notes)) {
            FlashUtil.error(request, "Please give the customer a reason for the rejection.");
            response.sendRedirect(request.getContextPath() + "/staff/returns");
            return;
        }

        int staffId = SessionUtil.getUserId(request);
        try {
            if ("approve".equals(action)) {
                returnDao.approve(returnId, staffId, notes);
                AuditLogger.log(staffId, "RETURN_APPROVED", "RETURN", returnId, notes, request);
                FlashUtil.success(request, "Return/exchange approved.");
            } else {
                returnDao.reject(returnId, staffId, notes);
                AuditLogger.log(staffId, "RETURN_REJECTED", "RETURN", returnId, notes, request);
                FlashUtil.success(request, "Return/exchange rejected.");
            }
        } catch (BusinessRuleException e) {
            FlashUtil.error(request, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        response.sendRedirect(request.getContextPath() + "/staff/returns");
    }
}
