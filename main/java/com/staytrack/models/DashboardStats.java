package com.staytrack.models;

import java.math.BigDecimal;

public record DashboardStats(int totalStudents, int occupiedRooms, int availableRooms,
                             BigDecimal monthlyIncome, BigDecimal pendingPayments) {
}
