package smartpay.lbycpd2.services;

import smartpay.lbycpd2.models.AuditEntry;

import java.util.ArrayList;
import java.util.List;

public class AuditManager {
    private static List<AuditEntry> auditLog = new ArrayList<>();

    public static void recordChange(String empId, String oldVal, String newVal) {
        auditLog.add(new AuditEntry(empId, oldVal, newVal));
    }

    public static List<AuditEntry> getAuditLog() {
        return auditLog;
    }
}