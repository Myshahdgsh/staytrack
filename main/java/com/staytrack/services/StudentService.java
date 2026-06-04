package com.staytrack.services;

import com.staytrack.database.DatabaseInitializer;
import com.staytrack.models.Student;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StudentService {
    public List<Student> findAll(String search) {
        String sql = """
            SELECT * FROM students
            WHERE ? = '' OR full_name LIKE ? OR CAST(id AS CHAR) LIKE ?
            ORDER BY id DESC
            """;
        try (Connection con = DatabaseInitializer.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String term = search == null ? "" : search.trim();
            ps.setString(1, term);
            ps.setString(2, "%" + term + "%");
            ps.setString(3, "%" + term + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Student> students = new ArrayList<>();
                while (rs.next()) {
                    students.add(map(rs));
                }
                return students;
            }
        } catch (Exception ex) {
            return DemoStore.students(search);
        }
    }

    public void save(Student s) throws Exception {
        if (s.id() == 0) {
            String sql = "INSERT INTO students(full_name,father_name,mother_name,gender,date_of_birth,phone,email,address,admission_date,guardian_contact) VALUES(?,?,?,?,?,?,?,?,?,?)";
            try (Connection con = DatabaseInitializer.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
                bind(ps, s);
                ps.executeUpdate();
            } catch (Exception ex) {
                DemoStore.saveStudent(s);
            }
        } else {
            String sql = "UPDATE students SET full_name=?,father_name=?,mother_name=?,gender=?,date_of_birth=?,phone=?,email=?,address=?,admission_date=?,guardian_contact=? WHERE id=?";
            try (Connection con = DatabaseInitializer.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
                bind(ps, s);
                ps.setInt(11, s.id());
                ps.executeUpdate();
            } catch (Exception ex) {
                DemoStore.saveStudent(s);
            }
        }
    }

    public void delete(int id) throws Exception {
        try (Connection con = DatabaseInitializer.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM students WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            DemoStore.deleteStudent(id);
        }
    }

    private void bind(PreparedStatement ps, Student s) throws Exception {
        ps.setString(1, s.fullName());
        ps.setString(2, s.fatherName());
        ps.setString(3, s.motherName());
        ps.setString(4, s.gender());
        ps.setDate(5, s.dateOfBirth() == null ? null : Date.valueOf(s.dateOfBirth()));
        ps.setString(6, s.phone());
        ps.setString(7, s.email());
        ps.setString(8, s.address());
        ps.setDate(9, Date.valueOf(s.admissionDate()));
        ps.setString(10, s.guardianContact());
    }

    private Student map(ResultSet rs) throws Exception {
        Date dob = rs.getDate("date_of_birth");
        Date adm = rs.getDate("admission_date");
        return new Student(rs.getInt("id"), rs.getString("full_name"), rs.getString("father_name"),
            rs.getString("mother_name"), rs.getString("gender"), dob == null ? null : dob.toLocalDate(),
            rs.getString("phone"), rs.getString("email"), rs.getString("address"),
            adm == null ? null : adm.toLocalDate(), rs.getString("guardian_contact"));
    }
}
