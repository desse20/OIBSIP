package com.oasisinfobyte.reservation.dao;

import com.oasisinfobyte.reservation.config.DatabaseConnection;
import com.oasisinfobyte.reservation.model.User;
import com.oasisinfobyte.reservation.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for User entities.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    /**
     * Finds a user by their username.
     *
     * @param username the username to look for
     * @return the User if found, or null otherwise
     */
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by username: " + username, e);
        }
        return null;
    }

    /**
     * Authenticates a user with username and raw password.
     *
     * @param username    the username
     * @param rawPassword the raw password to verify against stored hash
     * @return the authenticated User if valid, or null otherwise
     */
    public User authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return null;
        }

        User user = findByUsername(username);
        if (user == null) {
            return null;
        }

        if (PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    /**
     * Checks if a user already exists with the given username.
     *
     * @param username the username to check
     * @return true if exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking user existence: " + username, e);
        }
        return false;
    }

    /**
     * Inserts a new user into the database.
     *
     * @param user the user to persist
     * @return true if persisted, false otherwise
     */
    public boolean save(User user) {
        if (user == null || user.getUsername() == null || user.getPasswordHash() == null) {
            return false;
        }

        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername().trim());
            ps.setString(2, user.getPasswordHash());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving user: " + user.getUsername(), e);
        }
        return false;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}

