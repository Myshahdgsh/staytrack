package com.staytrack.models;

import java.time.LocalDate;

public record Allocation(int id, int studentId, int roomId, LocalDate allocationDate, boolean active) {
}
