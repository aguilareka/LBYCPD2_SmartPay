package smartpay.lbycpd2.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AttendanceLog {
    private String employeeId;
    private String name;
    private LocalDateTime timestamp;
    private String type;
    private boolean flagged;
    private String issue;

    public AttendanceLog(String employeeId, String name,
                         LocalDateTime timestamp, String type) {
        this.employeeId = employeeId;
        this.name = name;
        this.timestamp = timestamp;
        this.type = type;
        this.flagged = false;
        this.issue = "";
    }

    public String getEmployeeId() { return employeeId; }
    public String getName() { return name; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getType() { return type; }

    public boolean isFlagged() { return flagged; }
    public String getIssue() { return issue; }

    public String getDate() {
        return timestamp.toLocalDate().toString();
    }

    public String getTime() {
        return timestamp.toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public void flag(String issue) {
        this.flagged = true;
        this.issue = issue;
    }
}