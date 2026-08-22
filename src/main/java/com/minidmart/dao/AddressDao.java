package com.minidmart.dao;

import com.minidmart.model.Address;
import com.minidmart.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddressDao {

    public List<Address> listByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY is_default DESC, address_id DESC";
        List<Address> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Address> findById(int id) throws SQLException {
        String sql = "SELECT * FROM addresses WHERE address_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int create(Address a) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            if (a.isDefaultAddress()) {
                clearDefault(conn, a.getUserId());
            }
            String sql = "INSERT INTO addresses (user_id, label, line1, line2, city, state, pincode, is_default) VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, a.getUserId());
                ps.setString(2, a.getLabel());
                ps.setString(3, a.getLine1());
                ps.setString(4, a.getLine2());
                ps.setString(5, a.getCity());
                ps.setString(6, a.getState());
                ps.setString(7, a.getPincode());
                ps.setBoolean(8, a.isDefaultAddress());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    return keys.getInt(1);
                }
            }
        }
    }

    public boolean delete(int addressId, int userId) throws SQLException {
        String sql = "DELETE FROM addresses WHERE address_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, addressId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private void clearDefault(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE addresses SET is_default = 0 WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private Address map(ResultSet rs) throws SQLException {
        Address a = new Address();
        a.setAddressId(rs.getInt("address_id"));
        a.setUserId(rs.getInt("user_id"));
        a.setLabel(rs.getString("label"));
        a.setLine1(rs.getString("line1"));
        a.setLine2(rs.getString("line2"));
        a.setCity(rs.getString("city"));
        a.setState(rs.getString("state"));
        a.setPincode(rs.getString("pincode"));
        a.setDefaultAddress(rs.getBoolean("is_default"));
        return a;
    }
}
