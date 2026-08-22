package com.minidmart.dao;

import com.minidmart.model.PickupSlot;
import com.minidmart.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PickupSlotDao {

    public List<PickupSlot> listUpcoming() throws SQLException {
        String sql = "SELECT * FROM pickup_slots WHERE slot_date >= CURDATE() AND booked_count < capacity "
                + "ORDER BY slot_date, start_time";
        List<PickupSlot> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<PickupSlot> listByDateRange(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT * FROM pickup_slots WHERE slot_date BETWEEN ? AND ? ORDER BY slot_date, start_time";
        List<PickupSlot> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<PickupSlot> findById(int id) throws SQLException {
        String sql = "SELECT * FROM pickup_slots WHERE slot_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /** Atomically books a slot iff capacity remains. Returns true if booked. Must run inside caller's transaction. */
    public boolean tryBookSlot(Connection conn, int slotId) throws SQLException {
        String sql = "UPDATE pickup_slots SET booked_count = booked_count + 1 "
                + "WHERE slot_id = ? AND booked_count < capacity";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            return ps.executeUpdate() > 0;
        }
    }

    public void releaseSlot(Connection conn, int slotId) throws SQLException {
        String sql = "UPDATE pickup_slots SET booked_count = GREATEST(0, booked_count - 1) WHERE slot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ps.executeUpdate();
        }
    }

    public int create(PickupSlot s) throws SQLException {
        String sql = "INSERT INTO pickup_slots (slot_date, start_time, end_time, capacity) VALUES (?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(s.getSlotDate()));
            ps.setTime(2, Time.valueOf(s.getStartTime()));
            ps.setTime(3, Time.valueOf(s.getEndTime()));
            ps.setInt(4, s.getCapacity());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private PickupSlot map(ResultSet rs) throws SQLException {
        PickupSlot s = new PickupSlot();
        s.setSlotId(rs.getInt("slot_id"));
        s.setSlotDate(rs.getDate("slot_date").toLocalDate());
        s.setStartTime(rs.getTime("start_time").toLocalTime());
        s.setEndTime(rs.getTime("end_time").toLocalTime());
        s.setCapacity(rs.getInt("capacity"));
        s.setBookedCount(rs.getInt("booked_count"));
        return s;
    }
}
