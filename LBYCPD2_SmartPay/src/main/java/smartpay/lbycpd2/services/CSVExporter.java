package smartpay.lbycpd2.services;

import smartpay.lbycpd2.models.Payslip;

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
}