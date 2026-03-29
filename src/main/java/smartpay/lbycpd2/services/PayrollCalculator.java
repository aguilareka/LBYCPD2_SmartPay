package smartpay.lbycpd2.services;

import smartpay.lbycpd2.models.Employee;
import smartpay.lbycpd2.models.Payslip;

public class PayrollCalculator {

    public Payslip calculate(Employee emp, String period) {
        double hourlyRate = emp.getBaseRate();

        double basicPay = hourlyRate * emp.getRegularHours();
        double otPay = (hourlyRate * 1.25) * emp.getOvertimeHours();
        double lateDeduction = (hourlyRate / 60.0) * emp.getLateMinutes();

        double grossPay = (basicPay + otPay) - lateDeduction;
        if (grossPay < 0) grossPay = 0.0;

        double sss = calculateSSS(grossPay);
        double phic = grossPay * 0.025;
        double hdmf = Math.min(grossPay * 0.02, 200.0);

        double taxableIncome = grossPay - (sss + phic + hdmf);
        double tax = calculateTax(taxableIncome);
        double netPay = grossPay - (sss + phic + hdmf + tax);

        return new Payslip(emp.getName(), period, grossPay, tax, sss, phic, hdmf, netPay);
    }

    private double calculateSSS(double grossPay) {
        double employeeRate = 0.045;
        if (grossPay < 4250.00) {
            return 4000.00 * employeeRate;
        } else if (grossPay >= 29750.00) {
            return 30000.00 * employeeRate;
        } else {
            double msc = 4000.00 + (Math.floor((grossPay - 3750.00) / 500.00) * 500.00);
            return msc * employeeRate;
        }
    }

    private double calculateTax(double taxableIncome) {
        if (taxableIncome < 10417) {
            return 0.0;
        } else if (taxableIncome < 16667) {
            return (taxableIncome - 10417) * 0.15;
        } else if (taxableIncome < 33333) {
            return 937.50 + ((taxableIncome - 16667) * 0.20);
        } else if (taxableIncome < 83333) {
            return 4270.70 + ((taxableIncome - 33333) * 0.25);
        } else if (taxableIncome < 333333) {
            return 16770.70 + ((taxableIncome - 83333) * 0.30);
        } else {
            return 91770.70 + ((taxableIncome - 333333) * 0.35);
        }
    }
}