package smartpay.lbycpd2.models;

public class Employee {
    private String name;
    private double baseRate;
    private double totalHours;

    public Employee(String name, double baseRate, double totalHours) {
        this.name = name;
        this.baseRate = baseRate;
        this.totalHours = totalHours;
    }

    public String getName() { return name; }
    public double getBaseRate() { return baseRate; }
    public double getTotalHours() { return totalHours; }
}