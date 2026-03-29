package smartpay.lbycpd2.services;

import smartpay.lbycpd2.models.Users;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Authentication {
    // This now acts purely as our Employee Rate Database
    private static final Map<String, Users> users = new HashMap<>();
    private static final String USERS_FILE = "src/main/resources/smartpay/lbycpd2/users.csv";

    static {
        loadUsersFromFile();
    }

    private static void loadUsersFromFile() {
        users.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!headerSkipped) { headerSkipped = true; continue; }
                String[] fields = line.split(",");
                if (fields.length >= 3) {
                    String fullName = fields[0].trim();
                    String employeeId = fields[1].trim();
                    double hourlyRate = Double.parseDouble(fields[2].trim());

                    // Map by Employee ID for accurate lookup
                    users.put(employeeId, new Users(fullName, employeeId, hourlyRate));
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing users.csv.");
        }
    }

    public static Users getUserByEmployeeId(String empId) {
        return users.get(empId);
    }

    public static Map<String, Users> getAllUsers() { return users; }

    public static void addUser(Users user) {
        users.put(user.getEmployeeId(), user);
        saveUsersToFile();
    }

    public static void deleteUser(String empId) {
        users.remove(empId);
        saveUsersToFile();
    }

    private static void saveUsersToFile() {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            writer.write("fullName,employeeId,hourlyRate\n");
            for (Users u : users.values()) {
                writer.write(u.getFullName() + "," + u.getEmployeeId() + "," + u.getHourlyRate() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}