package smartpay.lbycpd2.services;

import smartpay.lbycpd2.models.AttendanceLog;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class AttendanceCleaner {

    public static List<AttendanceLog> removeDuplicateScans(List<AttendanceLog> logs) {
        List<AttendanceLog> cleaned = new ArrayList<>();

        for (AttendanceLog log : logs) {
            boolean duplicate = false;

            for (AttendanceLog existing : cleaned) {
                if (existing.getEmployeeId().equals(log.getEmployeeId()) &&
                        existing.getType().equals(log.getType()) &&
                        ChronoUnit.MINUTES.between(existing.getTimestamp(), log.getTimestamp()) == 0) {
                    duplicate = true;
                    break;
                }
            }

            if (!duplicate) cleaned.add(log);
        }
        return cleaned;
    }
}