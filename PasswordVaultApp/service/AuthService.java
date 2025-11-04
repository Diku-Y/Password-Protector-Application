package service;

import db.DBConnection;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Base64;

public class AuthService {

    public boolean register(String username, String password) {
        System.out.println("[REGISTER] Attempting to register: " + username);

        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            System.out.println("[REGISTER] Invalid input — username or password empty");
            return false;
        }

        String checkSQL = "SELECT 1 FROM users WHERE username = ?";
        String insertSQL = "INSERT INTO users(username, password) VALUES(?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            // Step 1: Check if username already exists
            try (PreparedStatement check = conn.prepareStatement(checkSQL)) {
                check.setString(1, username.trim());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("[REGISTER] Username already exists: " + username);
                        return false;
                    }
                }
            }

            // Step 2: Hash password
            String hash = hashPassword(password.trim());
            System.out.println("[REGISTER] Password hash: " + hash);

            // Step 3: Insert into DB
            try (PreparedStatement insert = conn.prepareStatement(insertSQL)) {
                insert.setString(1, username.trim());
                insert.setString(2, hash);
                int rows = insert.executeUpdate();
                System.out.println("[REGISTER] Rows inserted: " + rows);
                return rows > 0;
            }

        } catch (SQLException ex) {
            System.out.println("[REGISTER] SQL Exception: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.out.println("[REGISTER] General Exception: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public int login(String username, String password) throws Exception {
        if (username == null || password == null) return -1;
        username = username.trim();
        password = password.trim();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username=? AND password=?")) {

            String hashed = hashPassword(password);
            ps.setString(1, username);
            ps.setString(2, hashed);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                return -1;
            }
        }
    }

    public void deleteUser(int userId) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(password.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(digest);
    }
}
