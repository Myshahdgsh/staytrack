package com.staytrack.controllers;

import com.staytrack.models.DashboardStats;
import com.staytrack.models.Payment;
import com.staytrack.models.Room;
import com.staytrack.models.Student;
import com.staytrack.services.AllocationService;
import com.staytrack.services.DashboardService;
import com.staytrack.services.PaymentService;
import com.staytrack.services.ReportService;
import com.staytrack.services.RoomService;
import com.staytrack.services.StudentService;
import com.staytrack.util.AlertUtil;
import com.staytrack.util.ValidationUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;

public class MainController {
    private final Stage stage;
    private final BorderPane root = new BorderPane();
    private final StudentService studentService = new StudentService();
    private final RoomService roomService = new RoomService();
    private final AllocationService allocationService = new AllocationService();
    private final PaymentService paymentService = new PaymentService();
    private final DashboardService dashboardService = new DashboardService();
    private final ReportService reportService = new ReportService();

    public MainController(Stage stage) {
        this.stage = stage;
    }

    public Parent getView() {
        root.getStyleClass().add("app-root");
        root.setLeft(sidebar());
        showDashboard();
        return root;
    }

    private VBox sidebar() {
        VBox nav = new VBox(10);
        nav.getStyleClass().add("sidebar");
        nav.setPadding(new Insets(20));
        Label logo = new Label("StayTrack");
        logo.getStyleClass().add("sidebar-logo");
        nav.getChildren().add(logo);
        addNav(nav, "Dashboard", this::showDashboard);
        addNav(nav, "Students", this::showStudents);
        addNav(nav, "Rooms", this::showRooms);
        addNav(nav, "Allocation", this::showAllocation);
        addNav(nav, "Fees", this::showPayments);
        addNav(nav, "Reports", this::showReports);
        addNav(nav, "Settings", this::showSettings);
        addNav(nav, "About", this::showAbout);
        Button logout = navButton("Logout");
        logout.setOnAction(e -> {
            stage.setScene(new javafx.scene.Scene(new LoginController(stage).getView(), stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.getScene().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        });
        nav.getChildren().add(logout);
        return nav;
    }

    private void addNav(VBox nav, String text, Runnable action) {
        Button button = navButton(text);
        button.setOnAction(event -> action.run());
        nav.getChildren().add(button);
    }

    private Button navButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private VBox page(String title) {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));
        Label h = new Label(title);
        h.getStyleClass().add("page-title");
        page.getChildren().add(h);
        root.setCenter(page);
        return page;
    }

    private void showDashboard() {
        VBox page = page("Dashboard");
        DashboardStats s = dashboardService.loadStats();
        HBox cards = new HBox(14,
            statCard("Total Students", String.valueOf(s.totalStudents())),
            statCard("Occupied Rooms", String.valueOf(s.occupiedRooms())),
            statCard("Available Rooms", String.valueOf(s.availableRooms())),
            statCard("Monthly Income", s.monthlyIncome().toPlainString()),
            statCard("Pending Payments", s.pendingPayments().toPlainString())
        );
        cards.setFillHeight(true);
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bar = new BarChart<>(xAxis, yAxis);
        bar.setTitle("Hostel Overview");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Students", s.totalStudents()));
        series.getData().add(new XYChart.Data<>("Occupied", s.occupiedRooms()));
        series.getData().add(new XYChart.Data<>("Available", s.availableRooms()));
        bar.getData().add(series);
        PieChart pie = new PieChart(FXCollections.observableArrayList(
            new PieChart.Data("Paid", s.monthlyIncome().doubleValue()),
            new PieChart.Data("Pending", s.pendingPayments().doubleValue())
        ));
        pie.setTitle("Payment Health");
        HBox charts = new HBox(16, bar, pie);
        HBox.setHgrow(bar, Priority.ALWAYS);
        page.getChildren().addAll(cards, charts);
    }

    private VBox statCard(String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("stat-label");
        Label v = new Label(value);
        v.getStyleClass().add("stat-value");
        VBox box = new VBox(8, l, v);
        box.getStyleClass().add("stat-card");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private void showStudents() {
        VBox page = page("Student Management");
        TextField search = new TextField();
        search.setPromptText("Search by student name or ID");
        TableView<Student> table = new TableView<>();
        table.getColumns().add(col("ID", "id"));
        table.getColumns().add(col("Full Name", "fullName"));
        table.getColumns().add(col("Phone", "phone"));
        table.getColumns().add(col("Email", "email"));
        table.getColumns().add(col("Admission", "admissionDate"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        GridPane form = formGrid();
        TextField name = field(form, "Full Name", 0);
        TextField father = field(form, "Father Name", 1);
        TextField mother = field(form, "Mother Name", 2);
        ComboBox<String> gender = combo(form, "Gender", 3, "Male", "Female", "Other");
        DatePicker dob = date(form, "Date of Birth", 4);
        TextField phone = field(form, "Phone", 5);
        TextField email = field(form, "Email", 6);
        TextField guardian = field(form, "Guardian Contact", 7);
        DatePicker admission = date(form, "Admission Date", 8);
        admission.setValue(LocalDate.now());
        TextArea address = new TextArea();
        address.setPromptText("Address");
        address.setPrefRowCount(2);
        form.add(new Label("Address"), 0, 9);
        form.add(address, 1, 9);
        final int[] selectedId = {0};

        Button save = new Button("Save Student");
        Button delete = new Button("Delete");
        Button clear = new Button("Clear");
        HBox actions = new HBox(10, save, delete, clear);
        page.getChildren().addAll(search, table, form, actions);
        VBox.setVgrow(table, Priority.ALWAYS);

        Runnable refresh = () -> runSafe(() -> table.setItems(FXCollections.observableArrayList(studentService.findAll(search.getText()))));
        search.textProperty().addListener((obs, old, val) -> refresh.run());
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> {
            if (s == null) return;
            selectedId[0] = s.id();
            name.setText(s.fullName());
            father.setText(s.fatherName());
            mother.setText(s.motherName());
            gender.setValue(s.gender());
            dob.setValue(s.dateOfBirth());
            phone.setText(s.phone());
            email.setText(s.email());
            address.setText(s.address());
            admission.setValue(s.admissionDate());
            guardian.setText(s.guardianContact());
        });
        save.setOnAction(e -> runSafe(() -> {
            if (!ValidationUtil.validPhone(phone.getText()) || !ValidationUtil.validPhone(guardian.getText())) throw new IllegalArgumentException("Phone numbers must be 7-15 digits.");
            if (!ValidationUtil.validEmail(email.getText())) throw new IllegalArgumentException("Enter a valid email address.");
            studentService.save(new Student(selectedId[0], name.getText(), father.getText(), mother.getText(), gender.getValue(), dob.getValue(), phone.getText(), email.getText(), address.getText(), admission.getValue(), guardian.getText()));
            refresh.run();
            AlertUtil.info("Saved", "Student information saved.");
        }));
        delete.setOnAction(e -> runSafe(() -> {
            if (selectedId[0] > 0) studentService.delete(selectedId[0]);
            refresh.run();
        }));
        clear.setOnAction(e -> {
            selectedId[0] = 0;
            for (javafx.scene.Node node : form.getChildren()) if (node instanceof TextField tf) tf.clear();
            address.clear();
        });
        refresh.run();
    }

    private void showRooms() {
        VBox page = page("Room Management");
        TextField search = new TextField();
        search.setPromptText("Search by room number or status");
        TableView<Room> table = new TableView<>();
        table.getColumns().add(col("ID", "id"));
        table.getColumns().add(col("Room", "roomNumber"));
        table.getColumns().add(col("Type", "roomType"));
        table.getColumns().add(col("Capacity", "capacity"));
        table.getColumns().add(col("Occupancy", "currentOccupancy"));
        table.getColumns().add(col("Rent", "monthlyRent"));
        table.getColumns().add(col("Status", "status"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        GridPane form = formGrid();
        TextField number = field(form, "Room Number", 0);
        ComboBox<String> type = combo(form, "Room Type", 1, "Single", "Double", "Triple", "Dormitory");
        TextField capacity = field(form, "Capacity", 2);
        TextField occupancy = field(form, "Current Occupancy", 3);
        occupancy.setText("0");
        TextField rent = field(form, "Monthly Rent", 4);
        ComboBox<String> status = combo(form, "Status", 5, "Available", "Occupied", "Maintenance");
        final int[] selectedId = {0};
        Button save = new Button("Save Room");
        Button delete = new Button("Delete");
        page.getChildren().addAll(search, table, form, new HBox(10, save, delete));
        VBox.setVgrow(table, Priority.ALWAYS);

        Runnable refresh = () -> runSafe(() -> table.setItems(FXCollections.observableArrayList(roomService.findAll(search.getText()))));
        search.textProperty().addListener((obs, old, val) -> refresh.run());
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, r) -> {
            if (r == null) return;
            selectedId[0] = r.id();
            number.setText(r.roomNumber());
            type.setValue(r.roomType());
            capacity.setText(String.valueOf(r.capacity()));
            occupancy.setText(String.valueOf(r.currentOccupancy()));
            rent.setText(r.monthlyRent().toPlainString());
            status.setValue(r.status());
        });
        save.setOnAction(e -> runSafe(() -> {
            int cap = Integer.parseInt(capacity.getText());
            int occ = Integer.parseInt(occupancy.getText());
            BigDecimal monthlyRent = new BigDecimal(rent.getText());
            if (!ValidationUtil.positive(cap) || occ < 0 || occ > cap) throw new IllegalArgumentException("Occupancy must be between 0 and capacity.");
            if (!ValidationUtil.nonNegative(monthlyRent)) throw new IllegalArgumentException("Monthly rent cannot be negative.");
            roomService.save(new Room(selectedId[0], number.getText(), type.getValue(), cap, occ, monthlyRent, status.getValue()));
            refresh.run();
        }));
        delete.setOnAction(e -> runSafe(() -> {
            if (selectedId[0] > 0) roomService.delete(selectedId[0]);
            refresh.run();
        }));
        refresh.run();
    }

    private void showAllocation() {
        VBox page = page("Room Allocation");
        TextField studentId = new TextField();
        studentId.setPromptText("Student ID");
        TextField roomId = new TextField();
        roomId.setPromptText("Room ID");
        DatePicker date = new DatePicker(LocalDate.now());
        Button allocate = new Button("Allocate Room");
        Button vacate = new Button("Vacate Student Room");
        allocate.setOnAction(e -> runSafe(() -> {
            allocationService.allocate(Integer.parseInt(studentId.getText()), Integer.parseInt(roomId.getText()), date.getValue());
            AlertUtil.info("Allocated", "Room allocated and occupancy updated.");
        }));
        vacate.setOnAction(e -> runSafe(() -> {
            allocationService.vacate(Integer.parseInt(studentId.getText()));
            AlertUtil.info("Vacated", "Student room allocation closed.");
        }));
        page.getChildren().addAll(new Label("Use IDs from the Students and Rooms tables."), new HBox(10, studentId, roomId, date, allocate, vacate));
    }

    private void showPayments() {
        VBox page = page("Fee Management");
        TextField search = new TextField();
        search.setPromptText("Search by student ID, month, or payment status");
        TableView<Payment> table = new TableView<>();
        table.getColumns().add(col("ID", "id"));
        table.getColumns().add(col("Student ID", "studentId"));
        table.getColumns().add(col("Month", "month"));
        table.getColumns().add(col("Amount", "amount"));
        table.getColumns().add(col("Payment Date", "paymentDate"));
        table.getColumns().add(col("Due", "dueAmount"));
        table.getColumns().add(col("Status", "status"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        GridPane form = formGrid();
        TextField studentId = field(form, "Student ID", 0);
        TextField month = field(form, "Month", 1);
        month.setText(YearMonth.now().toString());
        TextField amount = field(form, "Amount", 2);
        DatePicker payDate = date(form, "Payment Date", 3);
        payDate.setValue(LocalDate.now());
        TextField due = field(form, "Due Amount", 4);
        ComboBox<String> status = combo(form, "Status", 5, "Paid", "Pending", "Partially Paid");
        Button save = new Button("Record Payment");
        page.getChildren().addAll(search, table, form, save);
        VBox.setVgrow(table, Priority.ALWAYS);
        Runnable refresh = () -> runSafe(() -> table.setItems(FXCollections.observableArrayList(paymentService.findAll(search.getText()))));
        search.textProperty().addListener((obs, old, val) -> refresh.run());
        save.setOnAction(e -> runSafe(() -> {
            BigDecimal paid = new BigDecimal(amount.getText());
            BigDecimal dueAmount = new BigDecimal(due.getText());
            if (!ValidationUtil.nonNegative(paid) || !ValidationUtil.nonNegative(dueAmount)) throw new IllegalArgumentException("Payments cannot be negative.");
            paymentService.save(new Payment(0, Integer.parseInt(studentId.getText()), month.getText(), paid, payDate.getValue(), dueAmount, status.getValue()));
            refresh.run();
        }));
        refresh.run();
    }

    private void showReports() {
        VBox page = page("Reports");
        Button students = new Button("Export Student List CSV");
        Button rooms = new Button("Export Room Occupancy CSV");
        Button payments = new Button("Export Payment Report CSV");
        Button pending = new Button("Export Pending Fee CSV");
        Button print = new Button("Print Current Page");
        Path reportFolder = Path.of("reports");
        students.setOnAction(e -> export("students", "SELECT * FROM students", reportFolder));
        rooms.setOnAction(e -> export("room_occupancy", "SELECT * FROM rooms", reportFolder));
        payments.setOnAction(e -> export("payments", "SELECT * FROM payments", reportFolder));
        pending.setOnAction(e -> export("pending_fees", "SELECT * FROM payments WHERE status <> 'Paid'", reportFolder));
        print.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(stage) && job.printPage(root.getCenter())) job.endJob();
        });
        page.getChildren().add(new HBox(10, students, rooms, payments, pending, print));
    }

    private void showSettings() {
        VBox page = page("Settings");
        page.getChildren().add(new Label("Database settings can be passed as JVM properties: staytrack.db.host, staytrack.db.port, staytrack.db.user, staytrack.db.password."));
    }

    private void showAbout() {
        VBox page = page("About");
        Label about = new Label("StayTrack is a JavaFX desktop application for hostel administration: students, rooms, allocation, fees, reports, and dashboard analytics.");
        about.setWrapText(true);
        page.getChildren().add(about);
    }

    private void export(String name, String sql, Path folder) {
        runSafe(() -> AlertUtil.info("Report exported", "Saved: " + reportService.exportCsv(name, sql, folder).toAbsolutePath()));
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.TOP_LEFT);
        return grid;
    }

    private TextField field(GridPane grid, String label, int row) {
        TextField field = new TextField();
        field.setPromptText(label);
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
        return field;
    }

    private DatePicker date(GridPane grid, String label, int row) {
        DatePicker field = new DatePicker();
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
        return field;
    }

    private ComboBox<String> combo(GridPane grid, String label, int row, String... values) {
        ComboBox<String> box = new ComboBox<>(FXCollections.observableArrayList(values));
        box.setValue(values[0]);
        grid.add(new Label(label), 0, row);
        grid.add(box, 1, row);
        return box;
    }

    private <S, T> TableColumn<S, T> col(String title, String property) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setCellValueFactory(cell -> {
            try {
                Method method = cell.getValue().getClass().getMethod(property);
                @SuppressWarnings("unchecked")
                T value = (T) method.invoke(cell.getValue());
                return new ReadOnlyObjectWrapper<>(value);
            } catch (Exception ex) {
                return new ReadOnlyObjectWrapper<>(null);
            }
        });
        return c;
    }

    private void runSafe(ThrowingAction action) {
        try {
            action.run();
        } catch (Exception ex) {
            AlertUtil.error("Action failed", ex.getMessage() == null ? "Unexpected error. Check application logs." : ex.getMessage());
        }
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }
}
