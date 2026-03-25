package smartpay.lbycpd2.models;

public class Payslip {
    private String name;
    private String period;
    private double grossPay;
    private double tax;
    private double sss;
    private double phic;
    private double hdmf;
    private double netPay;

    public Payslip(String name, String period, double grossPay, double tax,
                   double sss, double phic, double hdmf, double netPay) {
        this.name = name;
        this.period = period;
        this.grossPay = grossPay;
        this.tax = tax;
        this.sss = sss;
        this.phic = phic;
        this.hdmf = hdmf;
        this.netPay = netPay;
    }

    public String getName() { return name; }
    public String getPeriod() { return period; }
    public double getGrossPay() { return grossPay; }
    public double getTax() { return tax; }
    public double getSss() { return sss; }
    public double getPhic() { return phic; }
    public double getHdmf() { return hdmf; }
    public double getNetPay() { return netPay; }
}