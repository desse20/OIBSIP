package com.oasisinfobyte.reservation.dao;

import com.oasisinfobyte.reservation.config.DatabaseConnection;
import com.oasisinfobyte.reservation.model.Train;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Train entities.
 */
public class TrainDAO {

    private static final Logger LOGGER = Logger.getLogger(TrainDAO.class.getName());

    /**
     * Finds a train by its unique train number.
     *
     * @param trainNumber the train number to search for
     * @return the Train if found, or null otherwise
     */
    public Train findByTrainNumber(String trainNumber) {
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id, train_number, train_name, source_station, destination_station, created_at FROM trains WHERE train_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trainNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrain(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding train by number: " + trainNumber, e);
        }
        return null;
    }

    /**
     * Finds a train by its primary key ID.
     *
     * @param id the train database ID
     * @return the Train if found, or null otherwise
     */
    public Train findById(Long id) {
        if (id == null) {
            return null;
        }

        String sql = "SELECT id, train_number, train_name, source_station, destination_station, created_at FROM trains WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrain(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding train by id: " + id, e);
        }
        return null;
    }

    /**
     * Retrieves all available trains from the database.
     *
     * @return list of Train objects
     */
    public List<Train> findAll() {
        List<Train> trains = new ArrayList<>();
        String sql = "SELECT id, train_number, train_name, source_station, destination_station, created_at FROM trains ORDER BY train_number ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                trains.add(mapResultSetToTrain(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all trains", e);
        }
        return trains;
    }

    /**
     * Saves a new train to the database.
     *
     * @param train the train to insert
     * @return true if successful, false otherwise
     */
    public boolean save(Train train) {
        if (train == null || train.getTrainNumber() == null || train.getTrainName() == null) {
            return false;
        }

        String sql = "INSERT INTO trains (train_number, train_name, source_station, destination_station) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, train.getTrainNumber().trim());
            ps.setString(2, train.getTrainName().trim());
            ps.setString(3, train.getSourceStation().trim());
            ps.setString(4, train.getDestinationStation().trim());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        train.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving train: " + train.getTrainNumber(), e);
        }
        return false;
    }

    private Train mapResultSetToTrain(ResultSet rs) throws SQLException {
        Train train = new Train();
        train.setId(rs.getLong("id"));
        train.setTrainNumber(rs.getString("train_number"));
        train.setTrainName(rs.getString("train_name"));
        train.setSourceStation(rs.getString("source_station"));
        train.setDestinationStation(rs.getString("destination_station"));
        train.setCreatedAt(rs.getTimestamp("created_at"));
        return train;
    }
}

