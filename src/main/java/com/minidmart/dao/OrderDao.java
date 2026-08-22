package com.minidmart.dao;

import com.minidmart.model.*;
import com.minidmart.util.BusinessRuleException;
import com.minidmart.util.DBUtil;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDao {

    private final CartDao cartDao = new CartDao();
    private final ProductDao productDao = new ProductDao();
    private final PickupSlotDao slotDao = new PickupSlotDao();
    private final StockMovementDao stockMovementDao = new StockMovementDao();
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Places an order from the user's current cart in a single transaction:
     * locks & validates stock for every line, decrements it, books the pickup
     * slot (if any) against its capacity, snapshots prices, then clears the cart.
     * Throws BusinessRuleException (safe to show to the user) on any conflict,
     * in which case nothing is committed.
     */
    public Order placeOrder(int userId, FulfillmentType type, Integer pickupSlotId, Integer deliveryAddressId,
                             BigDecimal deliveryFee) throws SQLException, BusinessRuleException {
        List<CartItem> cartItems = cartDao.listItems(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessRuleException("Your cart is empty.");
        }
        if (type == FulfillmentType.PICKUP && pickupSlotId == null) {
            throw new BusinessRuleException("Please choose a pickup slot.");
        }
        if (type == FulfillmentType.DELIVERY && deliveryAddressId == null) {
            throw new BusinessRuleException("Please choose a delivery address.");
        }

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal subtotal = BigDecimal.ZERO;
                List<OrderItem> orderItems = new ArrayList<>();

                for (CartItem ci : cartItems) {
                    Optional<Product> locked = productDao.findByIdForUpdate(conn, ci.getProductId());
                    if (locked.isEmpty()) {
                        throw new BusinessRuleException("A product in your cart no longer exists.");
                    }
                    Product product = locked.get();
                    if (!product.isActive()) {
                        throw new BusinessRuleException(product.getName() + " is no longer available.");
                    }
                    if (product.getStockQty() < ci.getQuantity()) {
                        throw new BusinessRuleException("Not enough stock for " + product.getName()
                                + " (only " + product.getStockQty() + " left).");
                    }
                    productDao.adjustStock(conn, product.getProductId(), -ci.getQuantity());
                    stockMovementDao.record(conn, product.getProductId(), -ci.getQuantity(),
                            "ORDER_PLACED", "ORDER", null, userId);

                    OrderItem oi = new OrderItem();
                    oi.setProductId(product.getProductId());
                    oi.setProductNameSnap(product.getName());
                    oi.setUnitPriceSnap(product.getPrice());
                    oi.setQuantity(ci.getQuantity());
                    oi.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
                    orderItems.add(oi);
                    subtotal = subtotal.add(oi.getLineTotal());
                }

                if (type == FulfillmentType.PICKUP) {
                    boolean booked = slotDao.tryBookSlot(conn, pickupSlotId);
                    if (!booked) {
                        throw new BusinessRuleException("That pickup slot just filled up. Please pick another.");
                    }
                }

                BigDecimal fee = type == FulfillmentType.DELIVERY ? deliveryFee : BigDecimal.ZERO;
                BigDecimal total = subtotal.add(fee);
                String orderNumber = generateOrderNumber();

                String sql = "INSERT INTO orders (order_number, user_id, fulfillment_type, status, subtotal, "
                        + "delivery_fee, total_amount, pickup_slot_id, delivery_address_id) VALUES (?,?,?,?,?,?,?,?,?)";
                int orderId;
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, orderNumber);
                    ps.setInt(2, userId);
                    ps.setString(3, type.name());
                    ps.setString(4, OrderStatus.PLACED.name());
                    ps.setBigDecimal(5, subtotal);
                    ps.setBigDecimal(6, fee);
                    ps.setBigDecimal(7, total);
                    if (pickupSlotId != null) ps.setInt(8, pickupSlotId); else ps.setNull(8, Types.INTEGER);
                    if (deliveryAddressId != null) ps.setInt(9, deliveryAddressId); else ps.setNull(9, Types.INTEGER);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        orderId = keys.getInt(1);
                    }
                }

                String itemSql = "INSERT INTO order_items (order_id, product_id, product_name_snap, unit_price_snap, quantity, line_total) "
                        + "VALUES (?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                    for (OrderItem oi : orderItems) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, oi.getProductId());
                        ps.setString(3, oi.getProductNameSnap());
                        ps.setBigDecimal(4, oi.getUnitPriceSnap());
                        ps.setInt(5, oi.getQuantity());
                        ps.setBigDecimal(6, oi.getLineTotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                cartDao.clearCart(conn, userId);
                conn.commit();

                Order order = new Order();
                order.setOrderId(orderId);
                order.setOrderNumber(orderNumber);
                order.setUserId(userId);
                order.setFulfillmentType(type);
                order.setStatus(OrderStatus.PLACED);
                order.setSubtotal(subtotal);
                order.setDeliveryFee(fee);
                order.setTotalAmount(total);
                order.setItems(orderItems);
                return order;
            } catch (BusinessRuleException e) {
                conn.rollback();
                throw e;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Optional<Order> findById(int orderId) throws SQLException {
        String sql = "SELECT o.*, u.full_name AS customer_name FROM orders o "
                + "JOIN users u ON o.user_id = u.user_id WHERE o.order_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Order order = map(rs);
                loadExtras(conn, order);
                return Optional.of(order);
            }
        }
    }

    public List<Order> listByUser(int userId) throws SQLException {
        return listWhere("o.user_id = ?", userId);
    }

    public List<Order> listAll() throws SQLException {
        return listWhere(null, null);
    }

    public List<Order> listByStatus(OrderStatus status) throws SQLException {
        return listWhere("o.status = '" + status.name() + "'", null);
    }

    public List<Order> listUpcomingPickups() throws SQLException {
        return listWhere("o.fulfillment_type = 'PICKUP' AND o.status IN ('CONFIRMED','PREPARING','READY_FOR_PICKUP')", null);
    }

    public List<Order> listDeliveries() throws SQLException {
        return listWhere("o.fulfillment_type = 'DELIVERY' AND o.status IN ('CONFIRMED','PREPARING','OUT_FOR_DELIVERY')", null);
    }

    private List<Order> listWhere(String whereClauseWithPlaceholder, Integer userIdParam) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT o.*, u.full_name AS customer_name FROM orders o JOIN users u ON o.user_id = u.user_id ");
        if (whereClauseWithPlaceholder != null) {
            sql.append("WHERE ").append(whereClauseWithPlaceholder).append(" ");
        }
        sql.append("ORDER BY o.placed_at DESC");
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (userIdParam != null) ps.setInt(1, userIdParam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) orders.add(map(rs));
            }
        }
        return orders;
    }

    public void updateStatus(int orderId, OrderStatus status) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status = ? WHERE order_id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    /** Cancels an order, restocking items and releasing any booked pickup slot, atomically. */
    public void cancelOrder(int orderId, String reason) throws SQLException, BusinessRuleException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                OrderStatus current;
                Integer pickupSlotId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT status, pickup_slot_id FROM orders WHERE order_id = ? FOR UPDATE")) {
                    ps.setInt(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new BusinessRuleException("Order not found.");
                        current = OrderStatus.valueOf(rs.getString("status"));
                        int slotId = rs.getInt("pickup_slot_id");
                        pickupSlotId = rs.wasNull() ? null : slotId;
                    }
                }
                if (!current.isCancellable()) {
                    throw new BusinessRuleException("Order can no longer be cancelled (status: " + current + ").");
                }
                try (PreparedStatement items = conn.prepareStatement(
                        "SELECT product_id, quantity FROM order_items WHERE order_id = ?")) {
                    items.setInt(1, orderId);
                    try (ResultSet rs = items.executeQuery()) {
                        while (rs.next()) {
                            int productId = rs.getInt("product_id");
                            int qty = rs.getInt("quantity");
                            productDao.adjustStock(conn, productId, qty);
                            stockMovementDao.record(conn, productId, qty, "ORDER_CANCELLED", "ORDER", orderId, null);
                        }
                    }
                }
                if (pickupSlotId != null) {
                    slotDao.releaseSlot(conn, pickupSlotId);
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET status = 'CANCELLED', cancelled_at = NOW(), cancel_reason = ? WHERE order_id = ?")) {
                    ps.setString(1, reason);
                    ps.setInt(2, orderId);
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

    private void loadExtras(Connection conn, Order order) throws SQLException {
        String itemSql = "SELECT oi.*, "
                + "(SELECT COUNT(*) FROM returns r WHERE r.order_item_id = oi.order_item_id) AS return_count "
                + "FROM order_items oi WHERE oi.order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
            ps.setInt(1, order.getOrderId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem oi = new OrderItem();
                    oi.setOrderItemId(rs.getInt("order_item_id"));
                    oi.setOrderId(rs.getInt("order_id"));
                    oi.setProductId(rs.getInt("product_id"));
                    oi.setProductNameSnap(rs.getString("product_name_snap"));
                    oi.setUnitPriceSnap(rs.getBigDecimal("unit_price_snap"));
                    oi.setQuantity(rs.getInt("quantity"));
                    oi.setLineTotal(rs.getBigDecimal("line_total"));
                    oi.setReturnEligible(order.getStatus() == OrderStatus.COMPLETED && rs.getInt("return_count") == 0);
                    items.add(oi);
                }
            }
        }
        order.setItems(items);

        if (order.getPickupSlotId() != null) {
            new PickupSlotDao().findById(order.getPickupSlotId()).ifPresent(order::setPickupSlot);
        }
        if (order.getDeliveryAddressId() != null) {
            new AddressDao().findById(order.getDeliveryAddressId()).ifPresent(order::setDeliveryAddress);
        }
    }

    private String generateOrderNumber() {
        long ts = System.currentTimeMillis() % 1_000_000L;
        int rand = 100 + RANDOM.nextInt(900);
        return "ORD" + ts + rand;
    }

    private Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setUserId(rs.getInt("user_id"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setFulfillmentType(FulfillmentType.valueOf(rs.getString("fulfillment_type")));
        o.setStatus(OrderStatus.valueOf(rs.getString("status")));
        o.setSubtotal(rs.getBigDecimal("subtotal"));
        o.setDeliveryFee(rs.getBigDecimal("delivery_fee"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        int slot = rs.getInt("pickup_slot_id");
        o.setPickupSlotId(rs.wasNull() ? null : slot);
        int addr = rs.getInt("delivery_address_id");
        o.setDeliveryAddressId(rs.wasNull() ? null : addr);
        Timestamp placed = rs.getTimestamp("placed_at");
        if (placed != null) o.setPlacedAt(placed.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) o.setUpdatedAt(updated.toLocalDateTime());
        Timestamp cancelled = rs.getTimestamp("cancelled_at");
        if (cancelled != null) o.setCancelledAt(cancelled.toLocalDateTime());
        o.setCancelReason(rs.getString("cancel_reason"));
        return o;
    }

}
