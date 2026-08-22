package com.minidmart.servlet;

import com.minidmart.dao.CategoryDao;
import com.minidmart.dao.ProductDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HomeServlet extends HttpServlet {

    private final ProductDao productDao = new ProductDao();
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<com.minidmart.model.Product> products = productDao.search(null, null);
            if (products.size() > 8) products = products.subList(0, 8);
            request.setAttribute("featuredProducts", products);
            request.setAttribute("categories", categoryDao.listAll());
            request.setAttribute("pageTitle", "Mini D-Mart - Fresh groceries, delivered or picked up");
            request.getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
