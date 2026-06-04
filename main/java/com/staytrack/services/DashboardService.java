package com.staytrack.services;

import com.staytrack.database.DatabaseInitializer;
import com.staytrack.models.DashboardStats;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DashboardService {
    public DashboardStats loadStats() {
        try (Connection con = DatabaseInitializer.getConnection(); Statement st = con.createStatement()) {
            int students = scalarInt(st, "SELECT COUNT(*) FROM students");
            int occupied = scalarInt(st, "SELECT COUNT(*) FROM rooms WHERE current_occupancy > 0");
            int available = scalarInt(st, "SELECT COUNT(*) FROM rooms WHERE status='Available' AND current_occupancy < capacity");
            BigDecimal income = scalarDecimal(st, "SELECT COALESCE(SUM(amount),0) FROM payments WHERE status IN ('Paid','Partially Paid')");
            BigDecimal pending = scalarDecimal(st, "SELECT COALESCE(SUM(due_amount),0) FROM payments WHERE status <> 'Paid'");
            return new DashboardStats(students, occupied, available, income, pending);
        } catch (Exception ex) {
            return DemoStore.dashboardStats();
        }
    }

    private int scalarInt(Statement st, String sql) throws Exception {
        try (ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private BigDecimal scalarDecimal(Statement st, String sql) throws Exception {
        try (ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }
}
