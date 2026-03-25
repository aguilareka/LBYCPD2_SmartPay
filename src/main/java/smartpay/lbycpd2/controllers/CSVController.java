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


import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CSVController {
    @FXML
    private Label fileLabel;


    @FXML
    private TextArea outputArea;


    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private List<AttendanceLog> currentLogs = new ArrayList<>();

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );


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

                    LocalDateTime timestamp =
                            LocalDateTime.parse(date + " " + time, formatter);

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
    private void handleGeneratePayroll() {
        if (currentLogs == null || currentLogs.isEmpty()) {
            outputArea.appendText("\n[ERROR] No attendance data found. Please upload a CSV first.\n");
            return;
        }

        List<Employee> payrollData = generateEmployeeDataForPayroll(currentLogs);

        if (payrollData.isEmpty()) {
            outputArea.appendText("\n[FAILED] Cannot generate payroll. No valid, completed shifts were found for any employee.\n");
            return;
        }

        PayrollCalculator calculator = new PayrollCalculator();
        CSVExporter exporter = new CSVExporter();
        String payPeriod = "Feb 01 2026 - Feb 15 2026";

        for (Employee emp : payrollData) {
            Payslip slip = calculator.calculate(emp, payPeriod);
            exporter.exportPayslip(slip);
        }

        outputArea.appendText("\n[SUCCESS] Payroll processed and CSVs generated in project folder for " + payrollData.size() + " employees.\n");
    }

    private List<Employee> generateEmployeeDataForPayroll(List<AttendanceLog> allLogs) {
        List<Employee> employees = new ArrayList<>();
        Map<String, List<AttendanceLog>> groupedLogs = new HashMap<>();

        for (AttendanceLog log : allLogs) {
            groupedLogs.computeIfAbsent(log.getEmployeeId(), k -> new ArrayList<>()).add(log);
        }

        for (List<AttendanceLog> personLogs : groupedLogs.values()) {
            if (!personLogs.isEmpty()) {
                String name = personLogs.get(0).getName();
                double totalHours = AttendanceValidator.calculateTotalValidHours(personLogs);
                // Placeholder Hourly Rate, none placed in the activity guide //
                double hourlyRate = 250.0;

                // Only creates the payslip csv if the worker actually worked valid hours //
                if (totalHours > 0) {
                    employees.add(new Employee(name, hourlyRate, totalHours));
                }
            }
        }
        return employees;
    }
}

