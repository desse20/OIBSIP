package com.oasisinfobyte.reservation.dao;

import com.oasisinfobyte.reservation.config.DatabaseConnection;
import com.oasisinfobyte.reservation.model.Reservation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Reservation entities.
 */
public class ReservationDAO {

    private static final Logger LOGGER = Logger.getLogger(ReservationDAO.class.getName());

    /**
     * Persists a new reservation within a database transaction.
     * The booking is only considered successful after the transaction commits.
     *
     * @param reservation the Reservation to save
     * @return the saved Reservation with its generated ID, or null on failure
     * @throws SQLException on database error
     */
    public Reservation save(Reservation reservation) throws SQLException {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }

        String sql = "INSERT INTO reservations (pnr, passenger_name, train_id, class_type, journey_date, " +
                "source_station, destination_station, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, reservation.getPnr().trim());
                ps.setString(2, reservation.getPassengerName().trim());
                ps.setLong(3, reservation.getTrainId());
                ps.setString(4, reservation.getClassType().trim());
                ps.setDate(5, Date.valueOf(reservation.getJourneyDate()));
                ps.setString(6, reservation.getSourceStation().trim());
                ps.setString(7, reservation.getDestinationStation().trim());
                ps.setLong(8, reservation.getUserId());

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    throw new SQLException("Creating reservation failed, no rows affected.");
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reservation.setId(generatedKeys.getLong(1));
                    } else {
                        conn.rollback();
                        throw new SQLException("Creating reservation failed, no ID obtained.");
                    }
                }

                conn.commit(); // Transaction committed successfully
                LOGGER.info("Reservation committed with PNR: " + reservation.getPnr());
                return reservation;
            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        LOGGER.log(Level.SEVERE, "Rollback failed", ex);
                    }
                }
                throw e;
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
    }

    /**
     * Finds a reservation by its unique PNR, including train and user details.
     *
     * @param pnr the PNR number
     * @return the complete Reservation if found, or null otherwise
     */
    public Reservation findByPNR(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT r.id, r.pnr, r.passenger_name, r.train_id, r.class_type, r.journey_date, " +
                "r.source_station, r.destination_station, r.user_id, r.created_at, " +
                "t.train_number, t.train_name, u.username " +
                "FROM reservations r " +
                "JOIN trains t ON r.train_id = t.id " +
                "JOIN users u ON r.user_id = u.id " +
                "WHERE r.pnr = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pnr.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding reservation by PNR: " + pnr, e);
        }
        return null;
    }

    /**
     * Checks if a reservation with the given PNR exists.
     *
     * @param pnr the PNR to test
     * @return true if exists, false otherwise
     */
    public boolean existsByPNR(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM reservations WHERE pnr = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pnr.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking PNR existence: " + pnr, e);
        }
        return false;
    }

    /**
     * Cancels (removes) a reservation identified by its unique PNR within a transaction.
     *
     * @param pnr the PNR number to cancel
     * @return true if successfully cancelled, false if reservation was not found or failed
     * @throws SQLException on database error
     */
    public boolean cancel(String pnr) throws SQLException {
        if (pnr == null || pnr.trim().isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM reservations WHERE pnr = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pnr.trim());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    LOGGER.info("Reservation with PNR " + pnr + " successfully deleted.");
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                if (conn != null) {
                    conn.rollback();
                }
                throw e;
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
    }

    /**
     * Retrieves all reservations across the system ordered by creation date descending.
     *
     * @return list of Reservations
     */
    public List<Reservation> findAll() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.id, r.pnr, r.passenger_name, r.train_id, r.class_type, r.journey_date, " +
                "r.source_station, r.destination_station, r.user_id, r.created_at, " +
                "t.train_number, t.train_name, u.username " +
                "FROM reservations r " +
                "JOIN trains t ON r.train_id = t.id " +
                "JOIN users u ON r.user_id = u.id " +
                "ORDER BY r.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all reservations", e);
        }
        return reservations;
    }

    /**
     * Retrieves all reservations made by a specific user.
     *
     * @param userId user ID
     * @return list of Reservations
     */
    public List<Reservation> findByUserId(Long userId) {
        List<Reservation> reservations = new ArrayList<>();
        if (userId == null) {
            return reservations;
        }

        String sql = "SELECT r.id, r.pnr, r.passenger_name, r.train_id, r.class_type, r.journey_date, " +
                "r.source_station, r.destination_station, r.user_id, r.created_at, " +
                "t.train_number, t.train_name, u.username " +
                "FROM reservations r " +
                "JOIN trains t ON r.train_id = t.id " +
                "JOIN users u ON r.user_id = u.id " +
                "WHERE r.user_id = ? ORDER BY r.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservations for user: " + userId, e);
        }
        return reservations;
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setId(rs.getLong("id"));
        res.setPnr(rs.getString("pnr"));
        res.setPassengerName(rs.getString("passenger_name"));
        res.setTrainId(rs.getLong("train_id"));
        res.setClassType(rs.getString("class_type"));
        res.setJourneyDate(rs.getDate("journey_date").toLocalDate());
        res.setSourceStation(rs.getString("source_station"));
        res.setDestinationStation(rs.getString("destination_station"));
        res.setUserId(rs.getLong("user_id"));
        res.setCreatedAt(rs.getTimestamp("created_at"));

        res.setTrainNumber(rs.getString("train_number"));
        res.setTrainName(rs.getString("train_name"));
        res.setUsername(rs.getString("username"));
        return res;
    }
}

