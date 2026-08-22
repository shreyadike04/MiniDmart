package com.minidmart.dao;

import com.minidmart.model.Product;
import com.minidmart.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDao {

    /** Storefront browse: active products only, optional category + search filter. */
    public List<Product> search(Integer categoryId, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.name AS category_name FROM products p "
                        + "JOIN categories c ON p.category_id = c.category_id WHERE p.is_active = 1");
        List<Object> params = new ArrayList<>();
        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.name LIKE ? OR p.description LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY p.name");
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Admin/staff view: all products including inactive. */
    public List<Product> listAll() throws SQLException {
        String sql = "SELECT p.*, c.name AS category_name FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id ORDER BY p.name";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Product> listLowStock() throws SQLException {
        String sql = "SELECT p.*, c.name AS category_name FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "WHERE p.stock_qty <= p.reorder_level AND p.is_active = 1 ORDER BY p.stock_qty";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<Product> findById(int id) throws SQLException {
        String sql = "SELECT p.*, c.name AS category_name FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id WHERE p.product_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /** Locks the row for update within the caller's transaction (used at checkout). */
    public Optional<Product> findByIdForUpdate(Connection conn, int id) throws SQLException {
        String sql = "SELECT p.*, c.name AS category_name FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id WHERE p.product_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int create(Product p) throws SQLException {
        String sql = "INSERT INTO products (category_id, sku, name, description, unit, price, image_url, stock_qty, reorder_level, is_active) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void update(Product p) throws SQLException {
        String sql = "UPDATE products SET category_id=?, sku=?, name=?, description=?, unit=?, price=?, "
                + "image_url=?, stock_qty=?, reorder_level=?, is_active=? WHERE product_id=?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, p);
            ps.setInt(11, p.getProductId());
            ps.executeUpdate();
        }
    }

    /** Adjusts stock by delta (can be negative) within an existing transaction. Returns new qty. */
    public int adjustStock(Connection conn, int productId, int delta) throws SQLException {
        String sql = "UPDATE products SET stock_qty = stock_qty + ? WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps2 = conn.prepareStatement("SELECT stock_qty FROM products WHERE product_id = ?")) {
            ps2.setInt(1, productId);
            try (ResultSet rs = ps2.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void bind(PreparedStatement ps, Product p) throws SQLException {
        ps.setInt(1, p.getCategoryId());
        ps.setString(2, p.getSku());
        ps.setString(3, p.getName());
        ps.setString(4, p.getDescription());
        ps.setString(5, p.getUnit());
        ps.setBigDecimal(6, p.getPrice());
        ps.setString(7, p.getImageUrl());
        ps.setInt(8, p.getStockQty());
        ps.setInt(9, p.getReorderLevel());
        ps.setBoolean(10, p.isActive());
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setSku(rs.getString("sku"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setUnit(rs.getString("unit"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setImageUrl(rs.getString("image_url"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setReorderLevel(rs.getInt("reorder_level"));
        p.setActive(rs.getBoolean("is_active"));
        return p;
    }
}
