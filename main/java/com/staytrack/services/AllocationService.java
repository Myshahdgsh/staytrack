package com.staytrack.services;

import com.staytrack.database.DatabaseInitializer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class AllocationService {
    public void allocate(int studentId, int roomId, LocalDate date) throws Exception {
        try (Connection con = DatabaseInitializer.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (activeRoom(con, studentId) != 0) {
                    throw new IllegalStateException("Student already has an active room allocation.");
                }
                if (!roomHasSpace(con, roomId)) {
                    throw new IllegalStateException("Selected room is full or unavailable.");
                }
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO allocations(student_id,room_id,allocation_date,active) VALUES(?,?,?,TRUE)")) {
                    ps.setInt(1, studentId);
                    ps.setInt(2, roomId);
                    ps.setDate(3, Date.valueOf(date));
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE rooms SET current_occupancy=current_occupancy+1, status=IF(current_occupancy+1 >= capacity,'Occupied','Available') WHERE id=?")) {
                    ps.setInt(1, roomId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException) throw ex;
            DemoStore.allocate(studentId, roomId, date);
        }
    }

    public void vacate(int studentId) throws Exception {
        try (Connection con = DatabaseInitializer.getConnection()) {
            con.setAutoCommit(false);
            try {
                int roomId = activeRoom(con, studentId);
                if (roomId == 0) {
                    throw new IllegalStateException("Student does not have an active room allocation.");
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE allocations SET active=FALSE WHERE student_id=? AND active=TRUE")) {
                    ps.setInt(1, studentId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE rooms SET current_occupancy=GREATEST(current_occupancy-1,0), status='Available' WHERE id=?")) {
                    ps.setInt(1, roomId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException) throw ex;
            DemoStore.vacate(studentId);
        }
    }

    private boolean roomHasSpace(Connection con, int roomId) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT capacity,current_occupancy,status FROM rooms WHERE id=?")) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && "Available".equalsIgnoreCase(rs.getString("status")) && rs.getInt("current_occupancy") < rs.getInt("capacity");
            }
        }
    }

    private int activeRoom(Connection con, int studentId) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT room_id FROM allocations WHERE student_id=? AND active=TRUE")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
