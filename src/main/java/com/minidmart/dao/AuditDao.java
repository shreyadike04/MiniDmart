package com.minidmart.dao;

import com.minidmart.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read side of the audit trail, for the admin audit log view. Writes go through AuditLogger. */
public class AuditDao {

    public List<Map<String, Object>> listRecent(int limit) throws SQLException {
        String sql = "SELECT a.audit_id, a.action, a.entity_type, a.entity_id, a.details, a.ip_address, a.created_at, "
                + "u.full_name, u.email FROM audit_log a LEFT JOIN users u ON a.user_id = u.user_id "
                + "ORDER BY a.audit_id DESC LIMIT ?";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("auditId", rs.getLong("audit_id"));
                    row.put("action", rs.getString("action"));
                    row.put("entityType", rs.getString("entity_type"));
                    row.put("entityId", rs.getObject("entity_id"));
                    row.put("details", rs.getString("details"));
                    row.put("ipAddress", rs.getString("ip_address"));
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    row.put("userName", rs.getString("full_name"));
                    row.put("userEmail", rs.getString("email"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
