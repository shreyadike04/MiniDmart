package com.minidmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class StockMovementDao {

    /** Must run inside the caller's transaction/connection so it commits atomically with the stock change. */
    public void record(Connection conn, int productId, int changeQty, String reason,
                        String referenceType, Integer referenceId, Integer createdBy) throws SQLException {
        String sql = "INSERT INTO stock_movements (product_id, change_qty, reason, reference_type, reference_id, created_by) "
                + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, changeQty);
            ps.setString(3, reason);
            ps.setString(4, referenceType);
            if (referenceId != null) ps.setInt(5, referenceId); else ps.setNull(5, Types.INTEGER);
            if (createdBy != null) ps.setInt(6, createdBy); else ps.setNull(6, Types.INTEGER);
            ps.executeUpdate();
        }
    }
}
