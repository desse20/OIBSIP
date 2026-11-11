package com.oasisinfobyte.reservation.service;

import com.oasisinfobyte.reservation.dao.ReservationDAO;
import com.oasisinfobyte.reservation.dao.TrainDAO;
import com.oasisinfobyte.reservation.dao.UserDAO;
import com.oasisinfobyte.reservation.model.Reservation;
import com.oasisinfobyte.reservation.model.Train;
import com.oasisinfobyte.reservation.model.User;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer coordinating business logic, validation, PNR generation,
 * and interactions between UI and DAO layers.
 */
public class ReservationService {

    private static final Logger LOGGER = Logger.getLogger(ReservationService.class.getName());
    private static final Random RANDOM = new SecureRandom();

    private final ReservationDAO reservationDAO;
    private final TrainDAO trainDAO;
    private final UserDAO userDAO;

    /**
     * Default constructor for production use with default DAOs.
     */
    public ReservationService() {
        this(new ReservationDAO(), new TrainDAO(), new UserDAO());
    }

    /**
     * Constructor for dependency injection and testing.
     */
    public ReservationService(ReservationDAO reservationDAO, TrainDAO trainDAO, UserDAO userDAO) {
        this.reservationDAO = Objects.requireNonNull(reservationDAO, "ReservationDAO cannot be null");
        this.trainDAO = Objects.requireNonNull(trainDAO, "TrainDAO cannot be null");
        this.userDAO = Objects.requireNonNull(userDAO, "UserDAO cannot be null");
    }

    /**
     * Authenticates a user by username and raw password.
     */
    public User authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new ReservationException("Username cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ReservationException("Password cannot be empty.");
        }

        User user = userDAO.authenticate(username.trim(), password);
        if (user == null) {
            throw new ReservationException("Invalid username or password.");
        }
        return user;
    }

    /**
     * Retrieves all available trains.
     */
    public List<Train> getAllTrains() {
        return trainDAO.findAll();
    }

    /**
     * Retrieves all reservations across the system (e.g., for administrator view).
     */
    public List<Reservation> getAllReservations() {
        return reservationDAO.findAll();
    }

    /**
     * Retrieves reservations created by a specific user.
     */
    public List<Reservation> getReservationsByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return reservationDAO.findByUserId(userId);
    }

    /**
     * Finds a train by train number with format validation.
     */
    public Train findTrainByNumber(String trainNumber) {
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            throw new ReservationException("Train number cannot be empty.");
        }
        String cleanNumber = trainNumber.trim();
        if (!cleanNumber.matches("^\\d+$")) {
            throw new ReservationException("Train number must be numeric.");
        }
        return trainDAO.findByTrainNumber(cleanNumber);
    }

    /**
     * Creates and saves a new reservation after validating all business rules.
     *
     * @param passengerName      passenger's full name
     * @param trainNumber        train number string
     * @param classType          travel class
     * @param journeyDate        date of journey
     * @param sourceStation      origin station
     * @param destinationStation destination station
     * @param userId             authenticated user ID
     * @return the successfully persisted Reservation
     */
    public Reservation createReservation(String passengerName, String trainNumber, String classType,
                                         LocalDate journeyDate, String sourceStation, String destinationStation,
                                         Long userId) {
        // 1. Validate required fields
        if (passengerName == null || passengerName.trim().isEmpty()) {
            throw new ReservationException("Passenger name cannot be empty.");
        }
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            throw new ReservationException("Train number cannot be empty.");
        }
        String cleanTrainNumber = trainNumber.trim();
        if (!cleanTrainNumber.matches("^\\d+$")) {
            throw new ReservationException("Train number must be numeric.");
        }
        if (classType == null || classType.trim().isEmpty()) {
            throw new ReservationException("Class type must be selected.");
        }
        if (journeyDate == null) {
            throw new ReservationException("Journey date is required.");
        }
        if (journeyDate.isBefore(LocalDate.now())) {
            throw new ReservationException("Journey date cannot be in the past.");
        }
        if (sourceStation == null || sourceStation.trim().isEmpty()) {
            throw new ReservationException("Source station cannot be empty.");
        }
        if (destinationStation == null || destinationStation.trim().isEmpty()) {
            throw new ReservationException("Destination station cannot be empty.");
        }
        if (sourceStation.trim().equalsIgnoreCase(destinationStation.trim())) {
            throw new ReservationException("Source and destination stations cannot be identical.");
        }
        if (userId == null) {
            throw new ReservationException("A valid authenticated user is required.");
        }

        // 2. Validate train existence
        Train train = trainDAO.findByTrainNumber(cleanTrainNumber);
        if (train == null) {
            throw new ReservationException("Train with number " + cleanTrainNumber + " does not exist.");
        }

        // 3. Validate train route support
        boolean matchesSource = train.getSourceStation().trim().equalsIgnoreCase(sourceStation.trim());
        boolean matchesDestination = train.getDestinationStation().trim().equalsIgnoreCase(destinationStation.trim());
        if (!matchesSource || !matchesDestination) {
            throw new ReservationException(String.format(
                    "Selected train %s does not operate from '%s' to '%s'. Standard route: %s -> %s",
                    train.getTrainNumber(), sourceStation.trim(), destinationStation.trim(),
                    train.getSourceStation(), train.getDestinationStation()
            ));
        }

        // 4. Generate unique PNR
        String pnr = generateUniquePNR();

        // 5. Construct reservation entity
        Reservation reservation = new Reservation(
                pnr,
                passengerName.trim(),
                train.getId(),
                classType.trim(),
                journeyDate,
                train.getSourceStation(),
                train.getDestinationStation(),
                userId
        );
        reservation.setTrainNumber(train.getTrainNumber());
        reservation.setTrainName(train.getTrainName());

        // 6. Persist transactionally via DAO
        try {
            return reservationDAO.save(reservation);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while booking reservation", e);
            throw new ReservationException("Failed to complete reservation due to database error: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves an existing reservation by its PNR.
     *
     * @param pnr the unique PNR
     * @return the complete Reservation
     */
    public Reservation findByPNR(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            throw new ReservationException("PNR number cannot be empty.");
        }
        String cleanPnr = pnr.trim().toUpperCase();
        Reservation reservation = reservationDAO.findByPNR(cleanPnr);
        if (reservation == null) {
            throw new ReservationException("No reservation found for PNR: " + cleanPnr);
        }
        return reservation;
    }

    /**
     * Cancels an existing reservation after confirming its existence.
     *
     * @param pnr the PNR to cancel
     * @return true if successfully removed
     */
    public boolean cancelReservation(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            throw new ReservationException("PNR number cannot be empty.");
        }
        String cleanPnr = pnr.trim().toUpperCase();

        // Verify existence first
        Reservation reservation = reservationDAO.findByPNR(cleanPnr);
        if (reservation == null) {
            throw new ReservationException("Cannot cancel: No reservation found with PNR " + cleanPnr);
        }

        try {
            boolean success = reservationDAO.cancel(cleanPnr);
            if (!success) {
                throw new ReservationException("Cancellation failed: Reservation could not be removed.");
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during cancellation for PNR: " + cleanPnr, e);
            throw new ReservationException("Failed to cancel reservation due to database error: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a unique, non-colliding PNR number.
     * Enforces uniqueness both algorithmically and by querying the database.
     */
    public String generateUniquePNR() {
        int maxAttempts = 50;
        for (int i = 0; i < maxAttempts; i++) {
            // Standard 10-digit numeric PNR (1000000000 to 9999999999)
            long number = 1000000000L + (long) (RANDOM.nextDouble() * 9000000000L);
            String candidate = String.valueOf(number);
            if (!reservationDAO.existsByPNR(candidate)) {
                return candidate;
            }
        }
        throw new ReservationException("Unable to generate a unique PNR after " + maxAttempts + " attempts.");
    }
}

