package smartpay.lbycpd2.services;

import smartpay.lbycpd2.models.AttendanceLog;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

public class AttendanceValidator {

    public static List<AttendanceLog> validateByEmployee(List<AttendanceLog> logs) {
        Map<String, Map<LocalDate, List<AttendanceLog>>> grouped = new HashMap<>();

        for (AttendanceLog log : logs) {
            grouped
                    .computeIfAbsent(log.getEmployeeId(), k -> new HashMap<>())
                    .computeIfAbsent(log.getTimestamp().toLocalDate(), k -> new ArrayList<>())
                    .add(log);
        }

        for (Map<LocalDate, List<AttendanceLog>> perDate : grouped.values()) {
            for (List<AttendanceLog> dayLogs : perDate.values()) {

                boolean hasIn = false;
                boolean hasOut = false;
                boolean hasOTIn = false;
                boolean hasOTOut = false;

                for (AttendanceLog log : dayLogs) {
                    switch (log.getType()) {
                        case "IN": hasIn = true; break;
                        case "OUT": hasOut = true; break;
                        case "OT-IN": hasOTIn = true; break;
                        case "OT-OUT": hasOTOut = true; break;
                    }
                }

                if (hasIn && !hasOut) {
                    for (AttendanceLog log : dayLogs) {
                        if (log.getType().equals("IN")) {
                            log.flag("Missing TIME OUT");
                        }
                    }
                }

                if (!hasIn && hasOut) {
                    for (AttendanceLog log : dayLogs) {
                        if (log.getType().equals("OUT")) {
                            log.flag("Missing TIME IN");
                        }
                    }
                }

                if (hasOTIn && !hasOTOut) {
                    for (AttendanceLog log : dayLogs) {
                        if (log.getType().equals("OT-IN")) {
                            log.flag("Missing OT TIME OUT");
                        }
                    }
                }

                if (!hasOTIn && hasOTOut) {
                    for (AttendanceLog log : dayLogs) {
                        if (log.getType().equals("OT-OUT")) {
                            log.flag("Missing OT TIME IN");
                        }
                    }
                }
            }
        }

        return logs;
    }
    public static void validateSessions(List<AttendanceLog> logs) {
        logs.sort(Comparator.comparing(AttendanceLog::getTimestamp));

        AttendanceLog lastIn = null;

        for (AttendanceLog log : logs) {

            if (log.getType().equals("IN")) {
                if (lastIn != null) {
                    log.flag("Multiple consecutive TIME IN");
                }
                lastIn = log;
            }

            else if (log.getType().equals("OUT")) {
                if (lastIn == null) {
                    log.flag("TIME OUT without TIME IN");
                    continue;
                }

                Duration duration = Duration.between(lastIn.getTimestamp(), log.getTimestamp());

                if (duration.toMinutes() < 30) {
                    log.flag("Abnormally short session");
                }

                if (duration.toHours() > 16) {
                    log.flag("Abnormally long session");
                }

                lastIn = null;
            }
        }

        if (lastIn != null) {
            lastIn.flag("Missing TIME OUT");
        }
    }
    public static double[] calculateWorkHours(List<AttendanceLog> employeeLogs) {
        double regularHours = 0.0;
        double overtimeHours = 0.0;
        double lateMinutes = 0.0;

        AttendanceLog lastIn = null;
        AttendanceLog lastOtIn = null;
        employeeLogs.sort(Comparator.comparing(AttendanceLog::getTimestamp));

        for (AttendanceLog log : employeeLogs) {
            if (log.isFlagged()) continue;

            if (log.getType().equals("IN")) {
                lastIn = log;
                // Calculates Late Minutes (Assuming 08:00 AM Start)
                java.time.LocalTime time = log.getTimestamp().toLocalTime();
                java.time.LocalTime shiftStart = java.time.LocalTime.of(8, 0);
                if (time.isAfter(shiftStart)) {
                    lateMinutes += java.time.Duration.between(shiftStart, time).toMinutes();
                }
            } else if (log.getType().equals("OUT") && lastIn != null) {
                java.time.Duration duration = java.time.Duration.between(lastIn.getTimestamp(), log.getTimestamp());
                regularHours += duration.toMinutes() / 60.0;
                lastIn = null;
            } else if (log.getType().equals("OT-IN")) {
                lastOtIn = log;
            } else if (log.getType().equals("OT-OUT") && lastOtIn != null) {
                java.time.Duration duration = java.time.Duration.between(lastOtIn.getTimestamp(), log.getTimestamp());
                overtimeHours += duration.toMinutes() / 60.0;
                lastOtIn = null;
            }
        }
        return new double[]{regularHours, overtimeHours, lateMinutes};
    }
}