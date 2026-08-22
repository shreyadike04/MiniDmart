package com.minidmart.servlet.admin;

import com.minidmart.dao.UserDao;
import com.minidmart.model.Role;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class AdminUsersServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("users", userDao.listAll());
            request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
            request.setAttribute("pageTitle", "Users - Admin - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/users.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValid(request)) {
            FlashUtil.error(request, "Your session expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }
        String pathInfo = request.getPathInfo();
        int adminUserId = SessionUtil.getUserId(request);
        int targetUserId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("userId"), -1);

        if (targetUserId < 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            if ("/role".equals(pathInfo)) {
                String roleParam = request.getParameter("role");
                if (targetUserId == adminUserId && !"ADMIN".equals(roleParam)) {
                    FlashUtil.error(request, "You cannot change your own role.");
                } else {
                    Role role = Role.valueOf(roleParam);
                    userDao.updateRole(targetUserId, role);
                    AuditLogger.log(adminUserId, "USER_ROLE_CHANGED", "USER", targetUserId, "role=" + role, request);
                    FlashUtil.success(request, "Role updated.");
                }
            } else if ("/status".equals(pathInfo)) {
                boolean active = Boolean.parseBoolean(request.getParameter("active"));
                if (targetUserId == adminUserId && !active) {
                    FlashUtil.error(request, "You cannot deactivate your own account.");
                } else {
                    userDao.setActive(targetUserId, active);
                    AuditLogger.log(adminUserId, "USER_STATUS_CHANGED", "USER", targetUserId, "active=" + active, request);
                    FlashUtil.success(request, "Status updated.");
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/admin/users");
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
