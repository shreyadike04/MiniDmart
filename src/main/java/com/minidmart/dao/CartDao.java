package com.minidmart.dao;

import com.minidmart.model.CartItem;
import com.minidmart.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDao {

    public int getOrCreateCartId(int userId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            return getOrCreateCartId(conn, userId);
        }
    }

    public int getOrCreateCartId(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT cart_id FROM carts WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO carts (user_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public List<CartItem> listItems(int userId) throws SQLException {
        String sql = "SELECT ci.cart_item_id, ci.product_id, ci.quantity, p.name, p.unit, p.price, p.stock_qty, p.is_active "
                + "FROM cart_items ci JOIN carts c ON ci.cart_id = c.cart_id "
                + "JOIN products p ON ci.product_id = p.product_id "
                + "WHERE c.user_id = ? ORDER BY ci.cart_item_id";
        List<CartItem> items = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem ci = new CartItem();
                    ci.setCartItemId(rs.getInt("cart_item_id"));
                    ci.setProductId(rs.getInt("product_id"));
                    ci.setQuantity(rs.getInt("quantity"));
                    ci.setProductName(rs.getString("name"));
                    ci.setUnit(rs.getString("unit"));
                    ci.setPrice(rs.getBigDecimal("price"));
                    ci.setStockQty(rs.getInt("stock_qty"));
                    ci.setProductActive(rs.getBoolean("is_active"));
                    items.add(ci);
                }
            }
        }
        return items;
    }

    public int countItems(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(ci.quantity),0) FROM cart_items ci JOIN carts c ON ci.cart_id = c.cart_id WHERE c.user_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Adds to cart, capping at available stock. Returns the resulting quantity. */
    public int addItem(int userId, int productId, int quantity) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int cartId = getOrCreateCartId(conn, userId);
                int existing = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT quantity FROM cart_items WHERE cart_id = ? AND product_id = ?")) {
                    ps.setInt(1, cartId);
                    ps.setInt(2, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) existing = rs.getInt(1);
                    }
                }
                int stockQty = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT stock_qty FROM products WHERE product_id = ? AND is_active = 1")) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) stockQty = rs.getInt(1);
                    }
                }
                int newQty = Math.min(existing + quantity, stockQty);
                upsert(conn, cartId, productId, newQty, existing > 0);
                conn.commit();
                return newQty;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void updateQuantity(int userId, int productId, int quantity) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            int cartId = getOrCreateCartId(conn, userId);
            if (quantity <= 0) {
                removeItem(conn, cartId, productId);
            } else {
                upsert(conn, cartId, productId, quantity, true);
            }
        }
    }

    public void removeItem(int userId, int productId) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            int cartId = getOrCreateCartId(conn, userId);
            removeItem(conn, cartId, productId);
        }
    }

    public void clearCart(Connection conn, int userId) throws SQLException {
        int cartId = getOrCreateCartId(conn, userId);
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
            ps.setInt(1, cartId);
            ps.executeUpdate();
        }
    }

    private void upsert(Connection conn, int cartId, int productId, int quantity, boolean exists) throws SQLException {
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND product_id = ?")) {
                ps.setInt(1, quantity);
                ps.setInt(2, cartId);
                ps.setInt(3, productId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?,?,?)")) {
                ps.setInt(1, cartId);
                ps.setInt(2, productId);
                ps.setInt(3, quantity);
                ps.executeUpdate();
            }
        }
    }

    private void removeItem(Connection conn, int cartId, int productId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?")) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }
}
