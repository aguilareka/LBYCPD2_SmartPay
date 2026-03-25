package smartpay.lbycpd2.models;

import java.time.LocalDateTime;

public class AuditEntry {
    private String employeeId;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;

    public AuditEntry(String employeeId, String oldValue, String newValue) {
        this.employeeId = employeeId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return timestamp + " | " + employeeId +
                " | Changed: " + oldValue +
                " → " + newValue;
    }
}