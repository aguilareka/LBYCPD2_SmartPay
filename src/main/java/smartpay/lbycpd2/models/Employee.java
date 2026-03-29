package smartpay.lbycpd2.models;

public class Employee {
    private String name;
    private double baseRate;
    private double regularHours;
    private double overtimeHours;
    private double lateMinutes;

    public Employee(String name, double baseRate, double regularHours, double overtimeHours, double lateMinutes) {
        this.name = name;
        this.baseRate = baseRate;
        this.regularHours = regularHours;
        this.overtimeHours = overtimeHours;
        this.lateMinutes = lateMinutes;
    }

    public String getName() { return name; }
    public double getBaseRate() { return baseRate; }
    public double getRegularHours() { return regularHours; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getLateMinutes() { return lateMinutes; }
}