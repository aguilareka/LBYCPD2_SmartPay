package smartpay.lbycpd2.models;

public class Users {
    private String fullName;
    private String employeeId;
    private double hourlyRate;

    public Users(String fullName, String employeeId, double hourlyRate) {
        this.fullName = fullName;
        this.employeeId = employeeId;
        this.hourlyRate = hourlyRate;
    }

    public String getFullName() { return fullName; }
    public String getEmployeeId() { return employeeId; }
    public double getHourlyRate() { return hourlyRate; }
}