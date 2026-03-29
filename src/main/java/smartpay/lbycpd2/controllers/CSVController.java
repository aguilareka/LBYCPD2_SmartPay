package smartpay.lbycpd2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import smartpay.lbycpd2.services.AttendanceCleaner;
import smartpay.lbycpd2.services.AttendanceValidator;
import smartpay.lbycpd2.services.CSVExporter;
import smartpay.lbycpd2.services.PayrollCalculator;
import smartpay.lbycpd2.models.AttendanceLog;
import smartpay.lbycpd2.models.Employee;
import smartpay.lbycpd2.models.Payslip;
import smartpay.lbycpd2.models.Users;
import smartpay.lbycpd2.services.Authentication;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVController {

    @FXML private Label fileLabel;
    @FXML private TextArea outputArea;

    @FXML private TextField newName;
    @FXML private TextField newEmpId;
    @FXML private TextField newRate;
    @FXML private TextField deleteEmpId;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private List<AttendanceLog> currentLogs = new ArrayList<>();


    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            fileLabel.setText("Selected file: " + file.getName());
            processCSV(file);
        } else {
            fileLabel.setText("No file selected");
        }
    }

    private void processCSV(File file) {
        outputArea.clear();
        currentLogs = parseCSV(file);

        if (currentLogs.isEmpty()) {
            outputArea.appendText("No valid records found.\n");
            return;
        }

        currentLogs = AttendanceCleaner.removeDuplicateScans(currentLogs);
        AttendanceValidator.validateByEmployee(currentLogs);
        displayLogs(currentLogs);
    }

    private List<AttendanceLog> parseCSV(File file) {
        List<AttendanceLog> logs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean headerSkipped = false;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length != 5) {
                    outputArea.appendText("Line " + lineNumber + ": Invalid format\n");
                    continue;
                }
                try {
                    String empId = fields[0].trim();
                    String name = fields[1].trim();
                    String date = fields[2].trim();
                    String time = fields[3].trim();
                    String type = fields[4].trim().toUpperCase();

                    LocalDateTime timestamp = LocalDateTime.parse(date + " " + time, formatter);
                    logs.add(new AttendanceLog(empId, name, timestamp, type));

                } catch (Exception e) {
                    outputArea.appendText("Line " + lineNumber + ": Invalid date/time\n");
                }
            }
        } catch (IOException e) {
            outputArea.appendText("Error reading file: " + e.getMessage());
        }
        return logs;
    }

    private void displayLogs(List<AttendanceLog> logs) {
        outputArea.appendText("Processed Attendance Logs\n");
        outputArea.appendText("-----------------------------------\n");

        for (AttendanceLog log : logs) {
            String line = log.getEmployeeId() + " | " +
                    log.getName() + " | " +
                    log.getDate() + " | " +
                    log.getTime() + " | " +
                    log.getType();

            if (log.isFlagged()) {
                outputArea.appendText("⚠ " + line + " → " + log.getIssue() + "\n");
            } else {
                outputArea.appendText("✔ " + line + "\n");
            }
        }
    }

    @FXML
    private void handleGenerateIndividualPayslips() {
        if (currentLogs == null || currentLogs.isEmpty()) {
            outputArea.appendText("\n[ERROR] No attendance data found. Please load CSV first.\n");
            return;
        }

        List<Employee> payrollData = generateEmployeeDataForPayroll(currentLogs);
        PayrollCalculator calculator = new PayrollCalculator();
        CSVExporter exporter = new CSVExporter();
        String payPeriod = "Feb 01 2026 - Feb 15 2026";

        for (Employee emp : payrollData) {
            Payslip slip = calculator.calculate(emp, payPeriod);
            exporter.exportPayslip(slip);
        }
        outputArea.appendText("\n[SUCCESS] Generated Individual Payslips for " + payrollData.size() + " employees.\n");
    }

    @FXML
    private void handleGenerateMasterSummary() {
        if (currentLogs == null || currentLogs.isEmpty()) {
            outputArea.appendText("\n[ERROR] No attendance data found. Please load CSV first.\n");
            return;
        }

        List<Employee> payrollData = generateEmployeeDataForPayroll(currentLogs);
        PayrollCalculator calculator = new PayrollCalculator();
        CSVExporter exporter = new CSVExporter();
        String payPeriod = "Feb 01 2026 - Feb 15 2026";
        List<Payslip> allPayslips = new ArrayList<>();

        for (Employee emp : payrollData) {
            Payslip slip = calculator.calculate(emp, payPeriod);
            allPayslips.add(slip);
        }

        exporter.exportMasterReport(allPayslips, payPeriod);
        outputArea.appendText("\n[SUCCESS] Generated Master Payroll Summary Report.\n");
    }

    private List<Employee> generateEmployeeDataForPayroll(List<AttendanceLog> allLogs) {
        List<Employee> employees = new ArrayList<>();
        Map<String, List<AttendanceLog>> groupedLogs = new HashMap<>();

        for (AttendanceLog log : allLogs) {
            groupedLogs.computeIfAbsent(log.getEmployeeId(), k -> new ArrayList<>()).add(log);
        }

        for (Map.Entry<String, List<AttendanceLog>> entry : groupedLogs.entrySet()) {
            String empId = entry.getKey();
            List<AttendanceLog> personLogs = entry.getValue();

            if (!personLogs.isEmpty()) {
                String name = personLogs.get(0).getName();
                double[] hours = AttendanceValidator.calculateWorkHours(personLogs); // Index: 0=Reg, 1=OT, 2=Late

                double hourlyRate = 80.0;
                Users empUser = Authentication.getUserByEmployeeId(empId);
                if (empUser != null) {
                    hourlyRate = empUser.getHourlyRate();
                }

                if (hours[0] > 0 || hours[1] > 0) {
                    employees.add(new Employee(name, hourlyRate, hours[0], hours[1], hours[2]));
                }
            }
        }
        return employees;
    }

    @FXML
    private void handleAddEmployee() {
        try {
            String name = newName.getText();
            String id = newEmpId.getText();
            double rate = Double.parseDouble(newRate.getText());

            Users newUser = new Users(name, id, rate);
            Authentication.addUser(newUser);

            outputArea.appendText("\n[SUCCESS] Employee " + name + " added to system.");
            newName.clear(); newEmpId.clear(); newRate.clear();
        } catch (Exception e) {
            outputArea.appendText("\n[ERROR] Failed to add employee. Check your inputs.");
        }
    }

    @FXML
    private void handleDeleteEmployee() {
        String id = deleteEmpId.getText();
        if (Authentication.getAllUsers().containsKey(id)) {
            Authentication.deleteUser(id);
            outputArea.appendText("\n[SUCCESS] Employee ID " + id + " deleted.");
            deleteEmpId.clear();
        } else {
            outputArea.appendText("\n[ERROR] Employee ID not found.");
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        javafx.stage.Stage stage = (javafx.stage.Stage) fileLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(smartpay.lbycpd2.SmartPayApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        scene.getStylesheets().add(smartpay.lbycpd2.SmartPayApplication.class.getResource("styles.css").toExternalForm());
        stage.setScene(scene);
    }
}