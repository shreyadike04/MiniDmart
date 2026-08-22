package com.minidmart.servlet.admin;

import com.minidmart.dao.AuditDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class AdminAuditServlet extends HttpServlet {

    private final AuditDao auditDao = new AuditDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("rows", auditDao.listRecent(200));
            request.setAttribute("pageTitle", "Audit Log - Admin - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/audit.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
