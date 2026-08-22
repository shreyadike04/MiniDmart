package com.minidmart.servlet.catalog;

import com.minidmart.dao.ProductDao;
import com.minidmart.model.Product;
import com.minidmart.util.CsrfUtil;
import com.minidmart.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ProductDetailServlet extends HttpServlet {

    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("id"), -1);
        if (id < 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            Optional<Product> product = productDao.findById(id);
            if (product.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("product", product.get());
            request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
            String currentPath = request.getRequestURI().substring(request.getContextPath().length());
            if (request.getQueryString() != null) {
                currentPath += "?" + request.getQueryString();
            }
            request.setAttribute("loginRedirect", java.net.URLEncoder.encode(currentPath, "UTF-8"));
            request.setAttribute("currentPath", currentPath);
            request.setAttribute("pageTitle", product.get().getName() + " - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/catalog/product-detail.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
