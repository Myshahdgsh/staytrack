package com.staytrack.models;

import java.math.BigDecimal;

public record Room(int id, String roomNumber, String roomType, int capacity, int currentOccupancy,
                   BigDecimal monthlyRent, String status) {
    public boolean hasSpace() {
        return "Available".equalsIgnoreCase(status) && currentOccupancy < capacity;
    }
}
