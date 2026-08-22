package com.minidmart.dao;

import com.minidmart.model.ReturnRequest;
import com.minidmart.util.BusinessRuleException;
import com.minidmart.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReturnDao {

    private final ProductDao productDao = new ProductDao();
    private final StockMovementDao stockMovementDao = new StockMovementDao();

    /** Return/exchange window, in days, measured from the order item's order being marked COMPLETED. */
    public static final int ELIGIBILITY_DAYS = 7;

    public int create(ReturnRequest r) throws SQLException, BusinessRuleException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String checkSql = "SELECT oi.quantity, o.status, o.user_id, o.updated_at, "
                        + "(SELECT COUNT(*) FROM returns rr WHERE rr.order_item_id = oi.order_item_id) AS existing "
                        + "FROM order_items oi JOIN orders o ON oi.order_id = o.order_id "
                        + "WHERE oi.order_item_id = ? FOR UPDATE";
                int maxQty;
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setInt(1, r.getOrderItemId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new BusinessRuleException("Order item not found.");
                        if (rs.getInt("user_id") != r.getUserId()) {
                            throw new BusinessRuleException("This order does not belong to you.");
                        }
                        if (!"COMPLETED".equals(rs.getString("status"))) {
                            throw new BusinessRuleException("Only completed orders are eligible for return or exchange.");
                        }
                        Timestamp updatedAt = rs.getTimestamp("updated_at");
                        if (updatedAt != null
                                && updatedAt.toLocalDateTime().isBefore(LocalDateTime.now().minusDays(ELIGIBILITY_DAYS))) {
                            throw new BusinessRuleException("The " + ELIGIBILITY_DAYS + "-day return window has expired.");
                        }
                        if (rs.getInt("existing") > 0) {
                            throw new BusinessRuleException("A return/exchange request already exists for this item.");
                        }
                        maxQty = rs.getInt("quantity");
                    }
                }
                if (r.getQuantity() < 1 || r.getQuantity() > maxQty) {
                    throw new BusinessRuleException("Invalid return quantity.");
                }
                if (r.getType() == ReturnRequest.Type.EXCHANGE && r.getExchangeProductId() == null) {
                    throw new BusinessRuleException("Please choose a replacement product for exchange.");
                }

                String sql = "INSERT INTO returns (order_item_id, user_id, type, reason, quantity, status, exchange_product_id) "
                        + "VALUES (?,?,?,?,?, 'REQUESTED', ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, r.getOrderItemId());
                    ps.setInt(2, r.getUserId());
                    ps.setString(3, r.getType().name());
                    ps.setString(4, r.getReason());
                    ps.setInt(5, r.getQuantity());
                    if (r.getExchangeProductId() != null) ps.setInt(6, r.getExchangeProductId());
                    else ps.setNull(6, Types.INTEGER);
                    ps.executeUpdate();
                    conn.commit();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        return keys.getInt(1);
                    }
                }
            } catch (BusinessRuleException | SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<ReturnRequest> listByUser(int userId) throws SQLException {
        return listWhere("rt.user_id = ?", userId);
    }

    public List<ReturnRequest> listPending() throws SQLException {
        return listWhere("rt.status = 'REQUESTED'", null);
    }

    public List<ReturnRequest> listAll() throws SQLException {
        return listWhere(null, null);
    }

    public Optional<ReturnRequest> findById(int id) throws SQLException {
        List<ReturnRequest> results = listWhere("rt.return_id = ?", id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /** Approves the request and applies the inventory effect (restock, or restock+deduct for exchange). */
    public void approve(int returnId, int staffUserId, String notes) throws SQLException, BusinessRuleException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = "SELECT rt.*, oi.product_id AS orig_product_id, oi.order_id "
                        + "FROM returns rt JOIN order_items oi ON rt.order_item_id = oi.order_item_id "
                        + "WHERE rt.return_id = ? FOR UPDATE";
                String type, status;
                int quantity, origProductId, orderId;
                Integer exchangeProductId;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, returnId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new BusinessRuleException("Return request not found.");
                        status = rs.getString("status");
                        type = rs.getString("type");
                        quantity = rs.getInt("quantity");
                        origProductId = rs.getInt("orig_product_id");
                        orderId = rs.getInt("order_id");
                        int exProd = rs.getInt("exchange_product_id");
                        exchangeProductId = rs.wasNull() ? null : exProd;
                    }
                }
                if (!"REQUESTED".equals(status)) {
                    throw new BusinessRuleException("This request has already been resolved.");
                }

                productDao.adjustStock(conn, origProductId, quantity);
                stockMovementDao.record(conn, origProductId, quantity, "RETURN_RESTOCK", "RETURN", returnId, staffUserId);

                if ("EXCHANGE".equals(type)) {
                    Optional<com.minidmart.model.Product> replacement = productDao.findByIdForUpdate(conn, exchangeProductId);
                    if (replacement.isEmpty() || !replacement.get().isActive()
                            || replacement.get().getStockQty() < quantity) {
                        throw new BusinessRuleException("Replacement product is no longer available in that quantity.");
                    }
                    productDao.adjustStock(conn, exchangeProductId, -quantity);
                    stockMovementDao.record(conn, exchangeProductId, -quantity, "EXCHANGE_OUT", "RETURN", returnId, staffUserId);
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE returns SET status = 'APPROVED', staff_notes = ?, resolved_by = ?, resolved_at = NOW() WHERE return_id = ?")) {
                    ps.setString(1, notes);
                    ps.setInt(2, staffUserId);
                    ps.setInt(3, returnId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (BusinessRuleException | SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void reject(int returnId, int staffUserId, String notes) throws SQLException, BusinessRuleException {
        String sql = "UPDATE returns SET status = 'REJECTED', staff_notes = ?, resolved_by = ?, resolved_at = NOW() "
                + "WHERE return_id = ? AND status = 'REQUESTED'";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notes);
            ps.setInt(2, staffUserId);
            ps.setInt(3, returnId);
            int updated = ps.executeUpdate();
            if (updated == 0) throw new BusinessRuleException("This request has already been resolved.");
        }
    }

    public void complete(int returnId) throws SQLException, BusinessRuleException {
        String sql = "UPDATE returns SET status = 'COMPLETED' WHERE return_id = ? AND status = 'APPROVED'";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, returnId);
            int updated = ps.executeUpdate();
            if (updated == 0) throw new BusinessRuleException("Only approved requests can be marked completed.");
        }
    }

    private List<ReturnRequest> listWhere(String where, Integer param) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT rt.*, oi.order_id, o.order_number, oi.product_name_snap, u.full_name AS customer_name, "
                        + "ep.name AS exchange_product_name "
                        + "FROM returns rt "
                        + "JOIN order_items oi ON rt.order_item_id = oi.order_item_id "
                        + "JOIN orders o ON oi.order_id = o.order_id "
                        + "JOIN users u ON rt.user_id = u.user_id "
                        + "LEFT JOIN products ep ON rt.exchange_product_id = ep.product_id ");
        if (where != null) sql.append("WHERE ").append(where).append(" ");
        sql.append("ORDER BY rt.requested_at DESC");
        List<ReturnRequest> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private ReturnRequest map(ResultSet rs) throws SQLException {
        ReturnRequest r = new ReturnRequest();
        r.setReturnId(rs.getInt("return_id"));
        r.setOrderItemId(rs.getInt("order_item_id"));
        r.setOrderId(rs.getInt("order_id"));
        r.setOrderNumber(rs.getString("order_number"));
        r.setProductName(rs.getString("product_name_snap"));
        r.setUserId(rs.getInt("user_id"));
        r.setCustomerName(rs.getString("customer_name"));
        r.setType(ReturnRequest.Type.valueOf(rs.getString("type")));
        r.setReason(rs.getString("reason"));
        r.setQuantity(rs.getInt("quantity"));
        r.setStatus(ReturnRequest.Status.valueOf(rs.getString("status")));
        int exProd = rs.getInt("exchange_product_id");
        r.setExchangeProductId(rs.wasNull() ? null : exProd);
        r.setExchangeProductName(rs.getString("exchange_product_name"));
        r.setStaffNotes(rs.getString("staff_notes"));
        int resolvedBy = rs.getInt("resolved_by");
        r.setResolvedBy(rs.wasNull() ? null : resolvedBy);
        Timestamp requested = rs.getTimestamp("requested_at");
        if (requested != null) r.setRequestedAt(requested.toLocalDateTime());
        Timestamp resolved = rs.getTimestamp("resolved_at");
        if (resolved != null) r.setResolvedAt(resolved.toLocalDateTime());
        return r;
    }
}
