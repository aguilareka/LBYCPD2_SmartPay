package smartpay.lbycpd2.services;

import smartpay.lbycpd2.models.Payslip;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVExporter {

    public void exportPayslip(Payslip payslip) {
        String filename = payslip.getName().replaceAll("\\s+", "_") + "_Payslip.csv";

        try (FileWriter writer = new FileWriter(filename)) {
            writer.append("Name,Period,Gross Pay,Tax,SSS,PHIC,HDMF,Net Pay\n");
            writer.append(payslip.getName()).append(",")
                    .append(payslip.getPeriod()).append(",")
                    .append(String.format("%.2f", payslip.getGrossPay())).append(",")
                    .append(String.format("%.2f", payslip.getTax())).append(",")
                    .append(String.format("%.2f", payslip.getSss())).append(",")
                    .append(String.format("%.2f", payslip.getPhic())).append(",")
                    .append(String.format("%.2f", payslip.getHdmf())).append(",")
                    .append(String.format("%.2f", payslip.getNetPay())).append("\n");

            System.out.println("Generated: " + filename);
        } catch (IOException e) {
            System.out.println("Error writing file for " + payslip.getName());
            e.printStackTrace();
        }
    }
    public void exportMasterReport(java.util.List<Payslip> allPayslips, String period) {
        File directory = new File("Payslips_Output");
        if (!directory.exists()) directory.mkdir();

        String filename = "Payslips_Output/Master_Payroll_Summary.csv";
        double totalGross = 0, totalTax = 0, totalNet = 0;

        try (FileWriter writer = new FileWriter(filename)) {
            writer.append("Master Payroll Summary | Period: ").append(period).append("\n\n");
            writer.append("Name,Gross Pay,Tax,SSS,PHIC,HDMF,Net Pay\n");

            for (Payslip slip : allPayslips) {
                writer.append(slip.getName()).append(",")
                        .append(String.format("%.2f", slip.getGrossPay())).append(",")
                        .append(String.format("%.2f", slip.getTax())).append(",")
                        .append(String.format("%.2f", slip.getSss())).append(",")
                        .append(String.format("%.2f", slip.getPhic())).append(",")
                        .append(String.format("%.2f", slip.getHdmf())).append(",")
                        .append(String.format("%.2f", slip.getNetPay())).append("\n");

                totalGross += slip.getGrossPay();
                totalTax += slip.getTax();
                totalNet += slip.getNetPay();
            }

            writer.append("\nCOMPANY TOTALS\n");
            writer.append("Total Gross Pay,").append(String.format("%.2f", totalGross)).append("\n");
            writer.append("Total Tax Withheld,").append(String.format("%.2f", totalTax)).append("\n");
            writer.append("Total Net Disbursement,").append(String.format("%.2f", totalNet)).append("\n");

            System.out.println("Generated Master Summary: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}