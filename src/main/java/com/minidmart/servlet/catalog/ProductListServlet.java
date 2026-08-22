package com.minidmart.servlet.catalog;

import com.minidmart.dao.CategoryDao;
import com.minidmart.dao.ProductDao;
import com.minidmart.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class ProductListServlet extends HttpServlet {

    private final ProductDao productDao = new ProductDao();
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer categoryId = null;
        String categoryIdParam = request.getParameter("categoryId");
        if (!ValidationUtil.isBlank(categoryIdParam)) {
            try {
                categoryId = Integer.valueOf(categoryIdParam.trim());
            } catch (NumberFormatException ignored) {
                categoryId = null;
            }
        }
        String keyword = request.getParameter("keyword");

        try {
            request.setAttribute("products", productDao.search(categoryId, keyword));
            request.setAttribute("categories", categoryDao.listAll());
            request.setAttribute("selectedCategoryId", categoryId);
            request.setAttribute("keyword", keyword);
            request.setAttribute("pageTitle", "Shop - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/catalog/products.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
