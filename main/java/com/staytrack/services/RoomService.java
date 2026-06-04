package com.staytrack.services;

import com.staytrack.database.DatabaseInitializer;
import com.staytrack.models.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RoomService {
    public List<Room> findAll(String search) {
        String sql = "SELECT * FROM rooms WHERE ?='' OR room_number LIKE ? OR status LIKE ? ORDER BY room_number";
        try (Connection con = DatabaseInitializer.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            String term = search == null ? "" : search.trim();
            ps.setString(1, term);
            ps.setString(2, "%" + term + "%");
            ps.setString(3, "%" + term + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Room> rooms = new ArrayList<>();
                while (rs.next()) {
                    rooms.add(map(rs));
                }
                return rooms;
            }
        } catch (Exception ex) {
            return DemoStore.rooms(search);
        }
    }

    public void save(Room r) throws Exception {
        if (r.id() == 0) {
            String sql = "INSERT INTO rooms(room_number,room_type,capacity,current_occupancy,monthly_rent,status) VALUES(?,?,?,?,?,?)";
            try (Connection con = DatabaseInitializer.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
                bind(ps, r);
                ps.executeUpdate();
            } catch (Exception ex) {
                DemoStore.saveRoom(r);
            }
        } else {
            String sql = "UPDATE rooms SET room_number=?,room_type=?,capacity=?,current_occupancy=?,monthly_rent=?,status=? WHERE id=?";
            try (Connection con = DatabaseInitializer.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
                bind(ps, r);
                ps.setInt(7, r.id());
                ps.executeUpdate();
            } catch (Exception ex) {
                DemoStore.saveRoom(r);
            }
        }
    }

    public void delete(int id) throws Exception {
        try (Connection con = DatabaseInitializer.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM rooms WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            DemoStore.deleteRoom(id);
        }
    }

    private void bind(PreparedStatement ps, Room r) throws Exception {
        ps.setString(1, r.roomNumber());
        ps.setString(2, r.roomType());
        ps.setInt(3, r.capacity());
        ps.setInt(4, r.currentOccupancy());
        ps.setBigDecimal(5, r.monthlyRent());
        ps.setString(6, r.status());
    }

    private Room map(ResultSet rs) throws Exception {
        return new Room(rs.getInt("id"), rs.getString("room_number"), rs.getString("room_type"),
            rs.getInt("capacity"), rs.getInt("current_occupancy"), rs.getBigDecimal("monthly_rent"),
            rs.getString("status"));
    }
}
