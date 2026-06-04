package com.staytrack.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DatabaseInitializer {
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    private DatabaseInitializer() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DbConfig.DB_URL, DbConfig.USER, DbConfig.PASSWORD);
    }

    public static void initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection server = DriverManager.getConnection(DbConfig.SERVER_URL, DbConfig.USER, DbConfig.PASSWORD);
                 Statement st = server.createStatement()) {
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DbConfig.DATABASE + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }
            try (Connection con = getConnection(); Statement st = con.createStatement()) {
                for (String sql : SCHEMA) {
                    st.executeUpdate(sql);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Database initialization skipped. Check MySQL connection settings.", ex);
        }
    }

    private static final String[] SCHEMA = {
        """
        CREATE TABLE IF NOT EXISTS users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            username VARCHAR(50) NOT NULL UNIQUE,
            password VARCHAR(100) NOT NULL,
            role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
            remember_me BOOLEAN NOT NULL DEFAULT FALSE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS students (
            id INT AUTO_INCREMENT PRIMARY KEY,
            full_name VARCHAR(120) NOT NULL,
            father_name VARCHAR(120) NOT NULL,
            mother_name VARCHAR(120) NOT NULL,
            gender VARCHAR(20) NOT NULL,
            date_of_birth DATE,
            phone VARCHAR(20) NOT NULL,
            email VARCHAR(120),
            address TEXT,
            admission_date DATE NOT NULL,
            guardian_contact VARCHAR(20) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_student_name(full_name),
            INDEX idx_student_phone(phone)
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS rooms (
            id INT AUTO_INCREMENT PRIMARY KEY,
            room_number VARCHAR(30) NOT NULL UNIQUE,
            room_type VARCHAR(40) NOT NULL,
            capacity INT NOT NULL,
            current_occupancy INT NOT NULL DEFAULT 0,
            monthly_rent DECIMAL(10,2) NOT NULL,
            status VARCHAR(30) NOT NULL DEFAULT 'Available',
            CHECK (capacity > 0),
            CHECK (current_occupancy >= 0),
            CHECK (monthly_rent >= 0),
            INDEX idx_room_number(room_number),
            INDEX idx_room_status(status)
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS allocations (
            id INT AUTO_INCREMENT PRIMARY KEY,
            student_id INT NOT NULL,
            room_id INT NOT NULL,
            allocation_date DATE NOT NULL,
            active BOOLEAN NOT NULL DEFAULT TRUE,
            FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
            FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
            INDEX idx_allocation_active(active)
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS payments (
            id INT AUTO_INCREMENT PRIMARY KEY,
            student_id INT NOT NULL,
            fee_month VARCHAR(20) NOT NULL,
            amount DECIMAL(10,2) NOT NULL,
            payment_date DATE,
            due_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
            status VARCHAR(30) NOT NULL,
            FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
            CHECK (amount >= 0),
            CHECK (due_amount >= 0),
            INDEX idx_payment_status(status),
            INDEX idx_payment_month(fee_month)
        )
        """,
        "INSERT IGNORE INTO users(username,password,role) VALUES('admin','admin123','ADMIN')",
        "INSERT IGNORE INTO rooms(room_number,room_type,capacity,current_occupancy,monthly_rent,status) VALUES('A-101','Single',1,0,7500,'Available'),('B-204','Double',2,0,5500,'Available'),('C-305','Triple',3,0,4200,'Available')"
    };
}
