package com.staytrack.services;

import com.staytrack.models.Allocation;
import com.staytrack.models.DashboardStats;
import com.staytrack.models.Payment;
import com.staytrack.models.Room;
import com.staytrack.models.Student;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

final class DemoStore {
    private static final List<Student> STUDENTS = new ArrayList<>();
    private static final List<Room> ROOMS = new ArrayList<>();
    private static final List<Payment> PAYMENTS = new ArrayList<>();
    private static final List<Allocation> ALLOCATIONS = new ArrayList<>();
    private static final AtomicInteger STUDENT_ID = new AtomicInteger(1);
    private static final AtomicInteger ROOM_ID = new AtomicInteger(1);
    private static final AtomicInteger PAYMENT_ID = new AtomicInteger(1);
    private static final AtomicInteger ALLOCATION_ID = new AtomicInteger(1);

    static {
        saveStudent(new Student(0, "Ayesha Rahman", "Kamal Rahman", "Nusrat Rahman", "Female",
            LocalDate.of(2003, 4, 15), "01711111111", "ayesha@example.com", "Dhaka",
            LocalDate.of(2026, 1, 10), "01811111111"));
        saveStudent(new Student(0, "Tanvir Hasan", "Masud Hasan", "Salma Hasan", "Male",
            LocalDate.of(2002, 8, 21), "01722222222", "tanvir@example.com", "Chattogram",
            LocalDate.of(2026, 2, 1), "01822222222"));
        saveRoom(new Room(0, "A-101", "Single", 1, 0, new BigDecimal("7500.00"), "Available"));
        saveRoom(new Room(0, "B-204", "Double", 2, 0, new BigDecimal("5500.00"), "Available"));
        saveRoom(new Room(0, "C-305", "Triple", 3, 0, new BigDecimal("4200.00"), "Available"));
        savePayment(new Payment(0, 1, YearMonth.now().toString(), new BigDecimal("7500.00"),
            LocalDate.now(), BigDecimal.ZERO, "Paid"));
        savePayment(new Payment(0, 2, YearMonth.now().toString(), new BigDecimal("2500.00"),
            LocalDate.now(), new BigDecimal("3000.00"), "Partially Paid"));
    }

    private DemoStore() {
    }

    static synchronized List<Student> students(String search) {
        String term = normalize(search);
        return STUDENTS.stream()
            .filter(s -> term.isEmpty() || s.fullName().toLowerCase(Locale.ROOT).contains(term) || String.valueOf(s.id()).contains(term))
            .sorted((a, b) -> Integer.compare(b.id(), a.id()))
            .toList();
    }

    static synchronized void saveStudent(Student student) {
        if (student.id() == 0) {
            STUDENTS.add(new Student(STUDENT_ID.getAndIncrement(), student.fullName(), student.fatherName(),
                student.motherName(), student.gender(), student.dateOfBirth(), student.phone(), student.email(),
                student.address(), student.admissionDate(), student.guardianContact()));
            return;
        }
        deleteStudent(student.id());
        STUDENTS.add(student);
    }

    static synchronized void deleteStudent(int id) {
        STUDENTS.removeIf(student -> student.id() == id);
        PAYMENTS.removeIf(payment -> payment.studentId() == id);
        ALLOCATIONS.removeIf(allocation -> allocation.studentId() == id);
    }

    static synchronized List<Room> rooms(String search) {
        String term = normalize(search);
        return ROOMS.stream()
            .filter(r -> term.isEmpty() || r.roomNumber().toLowerCase(Locale.ROOT).contains(term) || r.status().toLowerCase(Locale.ROOT).contains(term))
            .sorted((a, b) -> a.roomNumber().compareToIgnoreCase(b.roomNumber()))
            .toList();
    }

    static synchronized void saveRoom(Room room) {
        if (room.id() == 0) {
            ROOMS.add(new Room(ROOM_ID.getAndIncrement(), room.roomNumber(), room.roomType(), room.capacity(),
                room.currentOccupancy(), room.monthlyRent(), room.status()));
            return;
        }
        deleteRoom(room.id());
        ROOMS.add(room);
    }

    static synchronized void deleteRoom(int id) {
        ROOMS.removeIf(room -> room.id() == id);
        ALLOCATIONS.removeIf(allocation -> allocation.roomId() == id);
    }

    static synchronized void allocate(int studentId, int roomId, LocalDate date) {
        boolean studentExists = STUDENTS.stream().anyMatch(student -> student.id() == studentId);
        if (!studentExists) {
            throw new IllegalStateException("Student ID was not found.");
        }
        if (ALLOCATIONS.stream().anyMatch(a -> a.studentId() == studentId && a.active())) {
            throw new IllegalStateException("Student already has an active room allocation.");
        }
        Room room = ROOMS.stream().filter(r -> r.id() == roomId).findFirst()
            .orElseThrow(() -> new IllegalStateException("Room ID was not found."));
        if (!room.hasSpace()) {
            throw new IllegalStateException("Selected room is full or unavailable.");
        }
        ALLOCATIONS.add(new Allocation(ALLOCATION_ID.getAndIncrement(), studentId, roomId, date, true));
        Room updated = new Room(room.id(), room.roomNumber(), room.roomType(), room.capacity(),
            room.currentOccupancy() + 1, room.monthlyRent(),
            room.currentOccupancy() + 1 >= room.capacity() ? "Occupied" : "Available");
        saveRoom(updated);
    }

    static synchronized void vacate(int studentId) {
        Allocation allocation = ALLOCATIONS.stream().filter(a -> a.studentId() == studentId && a.active()).findFirst()
            .orElseThrow(() -> new IllegalStateException("Student does not have an active room allocation."));
        ALLOCATIONS.remove(allocation);
        ALLOCATIONS.add(new Allocation(allocation.id(), allocation.studentId(), allocation.roomId(), allocation.allocationDate(), false));
        Room room = ROOMS.stream().filter(r -> r.id() == allocation.roomId()).findFirst().orElse(null);
        if (room != null) {
            saveRoom(new Room(room.id(), room.roomNumber(), room.roomType(), room.capacity(),
                Math.max(room.currentOccupancy() - 1, 0), room.monthlyRent(), "Available"));
        }
    }

    static synchronized List<Payment> payments(String search) {
        String term = normalize(search);
        return PAYMENTS.stream()
            .filter(p -> term.isEmpty() || String.valueOf(p.studentId()).contains(term)
                || p.month().toLowerCase(Locale.ROOT).contains(term)
                || p.status().toLowerCase(Locale.ROOT).contains(term))
            .sorted((a, b) -> Integer.compare(b.id(), a.id()))
            .toList();
    }

    static synchronized void savePayment(Payment payment) {
        PAYMENTS.add(new Payment(PAYMENT_ID.getAndIncrement(), payment.studentId(), payment.month(),
            payment.amount(), payment.paymentDate(), payment.dueAmount(), payment.status()));
    }

    static synchronized DashboardStats dashboardStats() {
        int occupied = (int) ROOMS.stream().filter(room -> room.currentOccupancy() > 0).count();
        int available = (int) ROOMS.stream().filter(Room::hasSpace).count();
        BigDecimal income = PAYMENTS.stream()
            .filter(payment -> !"Pending".equalsIgnoreCase(payment.status()))
            .map(Payment::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pending = PAYMENTS.stream()
            .filter(payment -> !"Paid".equalsIgnoreCase(payment.status()))
            .map(Payment::dueAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardStats(STUDENTS.size(), occupied, available, income, pending);
    }

    static synchronized String csv(String reportName) {
        StringBuilder out = new StringBuilder();
        switch (reportName) {
            case "students" -> {
                out.append("id,full_name,phone,email,admission_date").append(System.lineSeparator());
                for (Student s : STUDENTS) row(out, s.id(), s.fullName(), s.phone(), s.email(), s.admissionDate());
            }
            case "room_occupancy" -> {
                out.append("id,room_number,type,capacity,current_occupancy,rent,status").append(System.lineSeparator());
                for (Room r : ROOMS) row(out, r.id(), r.roomNumber(), r.roomType(), r.capacity(), r.currentOccupancy(), r.monthlyRent(), r.status());
            }
            case "pending_fees" -> {
                out.append("id,student_id,month,amount,due_amount,status").append(System.lineSeparator());
                PAYMENTS.stream().filter(p -> !"Paid".equalsIgnoreCase(p.status()))
                    .forEach(p -> row(out, p.id(), p.studentId(), p.month(), p.amount(), p.dueAmount(), p.status()));
            }
            default -> {
                out.append("id,student_id,month,amount,payment_date,due_amount,status").append(System.lineSeparator());
                for (Payment p : PAYMENTS) row(out, p.id(), p.studentId(), p.month(), p.amount(), p.paymentDate(), p.dueAmount(), p.status());
            }
        }
        return out.toString();
    }

    private static String normalize(String search) {
        return search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
    }

    private static void row(StringBuilder out, Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) out.append(',');
            out.append('"').append(String.valueOf(values[i]).replace("\"", "\"\"")).append('"');
        }
        out.append(System.lineSeparator());
    }
}
