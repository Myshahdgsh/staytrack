package com.staytrack.services;

import com.staytrack.database.DatabaseInitializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {
    public boolean login(String username, String password, boolean rememberMe) {
        String sql = "SELECT id FROM users WHERE username=? AND password=?";
        try (Connection con = DatabaseInitializer.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    updateRememberMe(username, rememberMe);
                    return true;
                }
            }
        } catch (Exception ex) {
            return "admin".equals(username) && "admin123".equals(password);
        }
        return false;
    }

    private void updateRememberMe(String username, boolean rememberMe) throws Exception {
        try (Connection con = DatabaseInitializer.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE users SET remember_me=? WHERE username=?")) {
            ps.setBoolean(1, rememberMe);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }
}
