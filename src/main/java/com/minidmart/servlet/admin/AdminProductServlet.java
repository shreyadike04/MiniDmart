package com.minidmart.servlet.admin;

import com.minidmart.dao.CategoryDao;
import com.minidmart.dao.ProductDao;
import com.minidmart.model.Product;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class AdminProductServlet extends HttpServlet {

    private final ProductDao productDao = new ProductDao();
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        try {
            if ("/new".equals(pathInfo)) {
                request.setAttribute("categories", categoryDao.listAll());
                request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
                request.setAttribute("pageTitle", "New Product - Admin - Mini D-Mart");
                request.getRequestDispatcher("/WEB-INF/jsp/admin/product-form.jsp").forward(request, response);
            } else if ("/edit".equals(pathInfo)) {
                int id = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("id"), -1);
                Optional<Product> product = id < 0 ? Optional.empty() : productDao.findById(id);
                if (product.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("product", product.get());
                request.setAttribute("categories", categoryDao.listAll());
                request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
                request.setAttribute("pageTitle", "Edit Product - Admin - Mini D-Mart");
                request.getRequestDispatcher("/WEB-INF/jsp/admin/product-form.jsp").forward(request, response);
            } else {
                request.setAttribute("products", productDao.listAll());
                request.setAttribute("pageTitle", "Products - Admin - Mini D-Mart");
                request.getRequestDispatcher("/WEB-INF/jsp/admin/products.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"/save".equals(request.getPathInfo())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!CsrfUtil.isValid(request)) {
            FlashUtil.error(request, "Your session expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/admin/products");
            return;
        }
        String productIdParam = request.getParameter("productId");
        String name = request.getParameter("name");
        String sku = request.getParameter("sku");
        String unit = request.getParameter("unit");
        String description = request.getParameter("description");
        String imageUrl = request.getParameter("imageUrl");
        String categoryIdParam = request.getParameter("categoryId");
        String priceParam = request.getParameter("price");
        // Stock quantity is only ever taken from this form when creating a brand-new
        // product (its starting count doesn't need an audit trail entry, since the
        // product itself didn't exist before). For an existing product this form does
        // not submit stock at all (see product-form.jsp) — edits go through
        // /staff/inventory instead, so every stock change is recorded in stock_movements.
        boolean isCreate = ValidationUtil.isBlank(productIdParam);
        String stockQtyParam = isCreate ? request.getParameter("stockQty") : null;
        String reorderLevelParam = request.getParameter("reorderLevel");
        boolean active = "on".equals(request.getParameter("active")) || "true".equals(request.getParameter("active"));

        String redirectOnError = ValidationUtil.isBlank(productIdParam)
                ? "/admin/products/new" : "/admin/products/edit?id=" + productIdParam;

        if (ValidationUtil.isBlank(name) || ValidationUtil.isBlank(sku) || ValidationUtil.isBlank(unit)) {
            FlashUtil.error(request, "Name, SKU and unit are required.");
            response.sendRedirect(request.getContextPath() + redirectOnError);
            return;
        }
        BigDecimal price;
        Integer newProductStockQty = null;
        int reorderLevel;
        int categoryId;
        try {
            price = new BigDecimal(priceParam.trim());
            if (price.signum() <= 0) throw new NumberFormatException("price must be positive");
            if (isCreate) {
                newProductStockQty = Integer.parseInt(stockQtyParam.trim());
                if (newProductStockQty < 0) throw new NumberFormatException("must be non-negative");
            }
            reorderLevel = Integer.parseInt(reorderLevelParam.trim());
            categoryId = Integer.parseInt(categoryIdParam.trim());
            if (reorderLevel < 0) throw new NumberFormatException("must be non-negative");
        } catch (Exception e) {
            FlashUtil.error(request, "Please provide a valid category, price, stock and reorder level.");
            response.sendRedirect(request.getContextPath() + redirectOnError);
            return;
        }

        Product product = new Product();
        product.setCategoryId(categoryId);
        product.setSku(sku.trim());
        product.setName(name.trim());
        product.setDescription(description);
        product.setUnit(unit.trim());
        product.setPrice(price);
        product.setImageUrl(imageUrl);
        product.setReorderLevel(reorderLevel);
        product.setActive(active);

        try {
            int adminUserId = SessionUtil.getUserId(request);
            if (isCreate) {
                product.setStockQty(newProductStockQty);
                int id = productDao.create(product);
                AuditLogger.log(adminUserId, "PRODUCT_CREATED", "PRODUCT", id, sku, request);
            } else {
                int productId = Integer.parseInt(productIdParam.trim());
                Optional<Product> existing = productDao.findById(productId);
                if (existing.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                product.setProductId(productId);
                product.setStockQty(existing.get().getStockQty());
                productDao.update(product);
                AuditLogger.log(adminUserId, "PRODUCT_UPDATED", "PRODUCT", product.getProductId(), sku, request);
            }
            FlashUtil.success(request, "Product saved.");
            response.sendRedirect(request.getContextPath() + "/admin/products");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
