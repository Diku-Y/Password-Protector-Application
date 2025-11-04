package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL =
        "jdbc:mysql://localhost:3306/password_vault?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Bujji1610*";

    public static Connection getConnection() throws SQLException {
        System.out.println("Connecting to DB: " + URL);
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connected to DB successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}