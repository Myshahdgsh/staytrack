package com.staytrack.models;

import java.time.LocalDate;

public record Student(int id, String fullName, String fatherName, String motherName, String gender,
                      LocalDate dateOfBirth, String phone, String email, String address,
                      LocalDate admissionDate, String guardianContact) {
}
