package com.staytrack.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Payment(int id, int studentId, String month, BigDecimal amount, LocalDate paymentDate,
                      BigDecimal dueAmount, String status) {
}
