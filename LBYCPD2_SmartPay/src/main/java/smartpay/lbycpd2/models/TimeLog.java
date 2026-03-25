package smartpay.lbycpd2.models;

public class TimeLog {
    private String employeeId;
    private String date;
    private String timeIn;
    private String timeOut;

    public TimeLog(String employeeId, String date, String timeIn, String timeOut) {
        this.employeeId = employeeId;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public boolean isValid() {
        return !employeeId.isEmpty() &&
                !date.isEmpty() &&
                !timeIn.isEmpty() &&
                !timeOut.isEmpty();
    }

    @Override
    public String toString() {
        return employeeId + " | " + date + " | " + timeIn + " - " + timeOut;
    }
}