package com.minidmart.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;

/** Best-effort audit trail writer. Failures are logged, never thrown, so a
 *  logging problem can't take down a business operation. */
public final class AuditLogger {

    private AuditLogger() {
    }

    public static void log(Integer userId, String action, String entityType, Integer entityId,
                            String details, HttpServletRequest request) {
        String ip = request == null ? null : clientIp(request);
        String sql = "INSERT INTO audit_log (user_id, action, entity_type, entity_id, details, ip_address) "
                + "VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) ps.setInt(1, userId); else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, action);
            ps.setString(3, entityType);
            if (entityId != null) ps.setInt(4, entityId); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, details);
            ps.setString(6, ip);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AuditLogger failed to write entry: " + e.getMessage());
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
