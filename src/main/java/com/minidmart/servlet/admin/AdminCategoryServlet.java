package com.minidmart.servlet.admin;

import com.minidmart.dao.CategoryDao;
import com.minidmart.model.Category;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class AdminCategoryServlet extends HttpServlet {

    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("categories", categoryDao.listAll());
            request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
            request.setAttribute("pageTitle", "Categories - Admin - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/categories.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValid(request)) {
            FlashUtil.error(request, "Your session expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/admin/categories");
            return;
        }
        String pathInfo = request.getPathInfo();
        int adminUserId = SessionUtil.getUserId(request);

        try {
            if ("/save".equals(pathInfo)) {
                String categoryIdParam = request.getParameter("categoryId");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                if (ValidationUtil.isBlank(name)) {
                    FlashUtil.error(request, "Category name is required.");
                    response.sendRedirect(request.getContextPath() + "/admin/categories");
                    return;
                }
                Category c = new Category();
                c.setName(name.trim());
                c.setDescription(description);
                if (ValidationUtil.isBlank(categoryIdParam)) {
                    int id = categoryDao.create(c);
                    AuditLogger.log(adminUserId, "CATEGORY_SAVED", "CATEGORY", id, name, request);
                } else {
                    c.setCategoryId(Integer.parseInt(categoryIdParam.trim()));
                    categoryDao.update(c);
                    AuditLogger.log(adminUserId, "CATEGORY_SAVED", "CATEGORY", c.getCategoryId(), name, request);
                }
                FlashUtil.success(request, "Category saved.");
            } else if ("/delete".equals(pathInfo)) {
                int categoryId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("categoryId"), -1);
                if (categoryId < 0) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                try {
                    categoryDao.delete(categoryId);
                    AuditLogger.log(adminUserId, "CATEGORY_DELETED", "CATEGORY", categoryId, null, request);
                    FlashUtil.success(request, "Category deleted.");
                } catch (SQLException e) {
                    if ("23000".equals(e.getSQLState())) {
                        FlashUtil.error(request, "Cannot delete a category that still has products.");
                    } else {
                        throw e;
                    }
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/admin/categories");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
