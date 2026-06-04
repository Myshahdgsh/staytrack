package com.staytrack.services;

import com.staytrack.database.DatabaseInitializer;
import com.staytrack.models.Payment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PaymentService {
    public List<Payment> findAll(String search) {
        String sql = "SELECT * FROM payments WHERE ?='' OR CAST(student_id AS CHAR) LIKE ? OR status LIKE ? OR fee_month LIKE ? ORDER BY id DESC";
        try (Connection con = DatabaseInitializer.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            String term = search == null ? "" : search.trim();
            ps.setString(1, term);
            ps.setString(2, "%" + term + "%");
            ps.setString(3, "%" + term + "%");
            ps.setString(4, "%" + term + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Payment> payments = new ArrayList<>();
                while (rs.next()) {
                    Date date = rs.getDate("payment_date");
                    payments.add(new Payment(rs.getInt("id"), rs.getInt("student_id"), rs.getString("fee_month"),
                        rs.getBigDecimal("amount"), date == null ? null : date.toLocalDate(),
                        rs.getBigDecimal("due_amount"), rs.getString("status")));
                }
                return payments;
            }
        } catch (Exception ex) {
            return DemoStore.payments(search);
        }
    }

    public void save(Payment p) throws Exception {
        String sql = "INSERT INTO payments(student_id,fee_month,amount,payment_date,due_amount,status) VALUES(?,?,?,?,?,?)";
        try (Connection con = DatabaseInitializer.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.studentId());
            ps.setString(2, p.month());
            ps.setBigDecimal(3, p.amount());
            ps.setDate(4, p.paymentDate() == null ? null : Date.valueOf(p.paymentDate()));
            ps.setBigDecimal(5, p.dueAmount());
            ps.setString(6, p.status());
            ps.executeUpdate();
        } catch (Exception ex) {
            DemoStore.savePayment(p);
        }
    }
}
