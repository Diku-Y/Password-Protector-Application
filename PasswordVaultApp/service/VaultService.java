package service;

import db.DBConnection;
import model.VaultItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VaultService {

    // Add Item
    public void addItem(int userId, VaultItem item) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO vault(user_id, site, site_username, site_password) VALUES(?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, item.getSite());
            ps.setString(3, item.getSiteUsername());
            ps.setString(4, item.getSitePassword());
            ps.executeUpdate();
        }
    }

    // Get all items for a user
    public List<VaultItem> getAllItems(int userId) throws SQLException {
        List<VaultItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM vault WHERE user_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new VaultItem(
                        rs.getInt("id"),
                        rs.getString("site"),
                        rs.getString("site_username"),
                        rs.getString("site_password")
                ));
            }
        }
        return list;
    }

    // Update item (by ID)
    public void updateItem(VaultItem item) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE vault SET site=?, site_username=?, site_password=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, item.getSite());
            ps.setString(2, item.getSiteUsername());
            ps.setString(3, item.getSitePassword());
            ps.setInt(4, item.getId());
            ps.executeUpdate();
        }
    }

    // Delete item by ID
    public void deleteItem(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM vault WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
