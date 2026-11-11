package com.oasisinfobyte.reservation;

import com.oasisinfobyte.reservation.dao.ReservationDAO;
import com.oasisinfobyte.reservation.dao.TrainDAO;
import com.oasisinfobyte.reservation.dao.UserDAO;
import com.oasisinfobyte.reservation.model.Reservation;
import com.oasisinfobyte.reservation.model.Train;
import com.oasisinfobyte.reservation.model.User;
import com.oasisinfobyte.reservation.service.ReservationException;
import com.oasisinfobyte.reservation.service.ReservationService;
import com.oasisinfobyte.reservation.util.PasswordUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying business rules of ReservationService using in-memory DAO stubs.
 * Does not require a live MySQL database.
 */
public class ReservationServiceTest {

    private ReservationService reservationService;
    private FakeReservationDAO fakeReservationDAO;
    private FakeTrainDAO fakeTrainDAO;
    private FakeUserDAO fakeUserDAO;

    @BeforeEach
    void setUp() {
        fakeReservationDAO = new FakeReservationDAO();
        fakeTrainDAO = new FakeTrainDAO();
        fakeUserDAO = new FakeUserDAO();

        // Seed fake train
        Train train = new Train(1L, "12001", "Shatabdi Express", "New Delhi", "Bhopal", null);
        fakeTrainDAO.addTrain(train);

        // Seed fake user
        String hashed = PasswordUtil.hashPassword("secret123");
        User user = new User(10L, "testuser", hashed, null);
        fakeUserDAO.addUser(user);

        reservationService = new ReservationService(fakeReservationDAO, fakeTrainDAO, fakeUserDAO);
    }

    @Test
    @DisplayName("Empty passenger name must be rejected")
    void testEmptyPassengerNameRejected() {
        ReservationException exNull = assertThrows(ReservationException.class, () ->
                reservationService.createReservation(null, "12001", "Sleeper",
                        LocalDate.now().plusDays(2), "New Delhi", "Bhopal", 10L));
        assertTrue(exNull.getMessage().contains("Passenger name cannot be empty"));

        ReservationException exEmpty = assertThrows(ReservationException.class, () ->
                reservationService.createReservation("   ", "12001", "Sleeper",
                        LocalDate.now().plusDays(2), "New Delhi", "Bhopal", 10L));
        assertTrue(exEmpty.getMessage().contains("Passenger name cannot be empty"));
    }

    @Test
    @DisplayName("Invalid train number must be rejected")
    void testInvalidTrainNumberRejected() {
        // Empty train number
        ReservationException exEmpty = assertThrows(ReservationException.class, () ->
                reservationService.createReservation("John Doe", "", "Sleeper",
                        LocalDate.now().plusDays(2), "New Delhi", "Bhopal", 10L));
        assertTrue(exEmpty.getMessage().contains("Train number cannot be empty"));

        // Non-numeric train number
        ReservationException exNonNumeric = assertThrows(ReservationException.class, () ->
                reservationService.createReservation("John Doe", "TRAIN-ABC", "Sleeper",
                        LocalDate.now().plusDays(2), "New Delhi", "Bhopal", 10L));
        assertTrue(exNonNumeric.getMessage().contains("Train number must be numeric"));

        // Non-existent train number
        ReservationException exNotFound = assertThrows(ReservationException.class, () ->
                reservationService.createReservation("John Doe", "99999", "Sleeper",
                        LocalDate.now().plusDays(2), "New Delhi", "Bhopal", 10L));
        assertTrue(exNotFound.getMessage().contains("does not exist"));
    }

    @Test
    @DisplayName("Invalid journey date must be rejected")
    void testInvalidJourneyDateRejected() {
        // Null date
        ReservationException exNull = assertThrows(ReservationException.class, () ->
                reservationService.createReservation("John Doe", "12001", "Sleeper",
                        null, "New Delhi", "Bhopal", 10L));
        assertTrue(exNull.getMessage().contains("Journey date is required"));

        // Past date
        ReservationException exPast = assertThrows(ReservationException.class, () ->
                reservationService.createReservation("John Doe", "12001", "Sleeper",
                        LocalDate.now().minusDays(1), "New Delhi", "Bhopal", 10L));
        assertTrue(exPast.getMessage().contains("cannot be in the past"));
    }

    @Test
    @DisplayName("Identical source and destination must be rejected")
    void testIdenticalSourceAndDestinationRejected() {
        ReservationException ex = assertThrows(ReservationException.class, () ->
                reservationService.createReservation("John Doe", "12001", "Sleeper",
                        LocalDate.now().plusDays(3), "New Delhi", "New Delhi", 10L));
        assertTrue(ex.getMessage().contains("cannot be identical"));
    }

    @Test
    @DisplayName("Train route mismatch must be rejected")
    void testRouteMismatchRejected() {
        ReservationException ex = assertThrows(ReservationException.class, () ->
                reservationService.createReservation("John Doe", "12001", "Sleeper",
                        LocalDate.now().plusDays(3), "New Delhi", "Mumbai", 10L));
        assertTrue(ex.getMessage().contains("does not operate"));
    }

    @Test
    @DisplayName("Valid reservation must be processed")
    void testValidReservationProcessed() {
        LocalDate date = LocalDate.now().plusDays(5);
        Reservation res = reservationService.createReservation(
                "Jane Doe", "12001", "AC First Class (1A)", date, "New Delhi", "Bhopal", 10L
        );

        assertNotNull(res, "Saved reservation should not be null");
        assertNotNull(res.getPnr(), "Generated PNR should not be null");
        assertEquals(10, res.getPnr().length(), "Generated PNR should be a 10-digit number");
        assertEquals("Jane Doe", res.getPassengerName());
        assertEquals("12001", res.getTrainNumber());
        assertEquals("Shatabdi Express", res.getTrainName());
        assertEquals("New Delhi", res.getSourceStation());
        assertEquals("Bhopal", res.getDestinationStation());
        assertEquals("AC First Class (1A)", res.getClassType());
        assertEquals(date, res.getJourneyDate());
    }

    @Test
    @DisplayName("Nonexistent PNR must be handled correctly")
    void testNonexistentPNRHandledCorrectly() {
        ReservationException ex = assertThrows(ReservationException.class, () ->
                reservationService.findByPNR("NONEXISTENT"));
        assertTrue(ex.getMessage().contains("No reservation found"));
    }

    @Test
    @DisplayName("Cancellation must only succeed for an existing reservation")
    void testCancellationOnlySucceedsForExistingReservation() {
        // Book a reservation first
        Reservation res = reservationService.createReservation(
                "Alice Smith", "12001", "Sleeper",
                LocalDate.now().plusDays(7), "New Delhi", "Bhopal", 10L
        );
        String pnr = res.getPnr();

        // 1. Cancellation of nonexistent PNR fails
        ReservationException exNonExistent = assertThrows(ReservationException.class, () ->
                reservationService.cancelReservation("0000000000"));
        assertTrue(exNonExistent.getMessage().contains("No reservation found"));

        // 2. Cancellation of existing reservation succeeds
        boolean cancelled = reservationService.cancelReservation(pnr);
        assertTrue(cancelled, "Cancellation should return true for valid reservation");

        // 3. Trying to fetch or cancel the reservation again should fail
        assertThrows(ReservationException.class, () -> reservationService.findByPNR(pnr));
        assertThrows(ReservationException.class, () -> reservationService.cancelReservation(pnr));
    }

    @Test
    @DisplayName("Authentication succeeds with valid credentials and fails with invalid")
    void testAuthentication() {
        User user = reservationService.authenticate("testuser", "secret123");
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());

        assertThrows(ReservationException.class, () ->
                reservationService.authenticate("testuser", "wrongpassword"));

        assertThrows(ReservationException.class, () ->
                reservationService.authenticate("nonexistent", "secret123"));
    }

    @Test
    @DisplayName("Admin view retrieves all reservations system-wide")
    void testAdminViewAllReservations() {
        assertEquals(0, reservationService.getAllReservations().size());

        reservationService.createReservation(
                "Passenger A", "12001", "Sleeper",
                LocalDate.now().plusDays(2), "New Delhi", "Bhopal", 10L
        );
        reservationService.createReservation(
                "Passenger B", "12001", "AC 3 Tier (3A)",
                LocalDate.now().plusDays(3), "New Delhi", "Bhopal", 11L
        );

        List<Reservation> allReservations = reservationService.getAllReservations();
        assertEquals(2, allReservations.size(), "Admin view should return all reservations");

        List<Reservation> user10Reservations = reservationService.getReservationsByUserId(10L);
        assertEquals(1, user10Reservations.size());
        assertEquals("Passenger A", user10Reservations.get(0).getPassengerName());
    }

    // =========================================================================
    // IN-MEMORY FAKES FOR ISOLATED TESTING
    // =========================================================================

    private static class FakeReservationDAO extends ReservationDAO {
        private final Map<String, Reservation> storage = new HashMap<>();
        private long idSequence = 1;

        @Override
        public Reservation save(Reservation reservation) throws SQLException {
            reservation.setId(idSequence++);
            storage.put(reservation.getPnr(), reservation);
            return reservation;
        }

        @Override
        public Reservation findByPNR(String pnr) {
            return storage.get(pnr);
        }

        @Override
        public boolean existsByPNR(String pnr) {
            return storage.containsKey(pnr);
        }

        @Override
        public boolean cancel(String pnr) throws SQLException {
            return storage.remove(pnr) != null;
        }

        @Override
        public List<Reservation> findByUserId(Long userId) {
            return storage.values().stream()
                    .filter(r -> r.getUserId().equals(userId))
                    .toList();
        }

        @Override
        public List<Reservation> findAll() {
            return List.copyOf(storage.values());
        }
    }

    private static class FakeTrainDAO extends TrainDAO {
        private final Map<String, Train> trainsByNumber = new HashMap<>();

        public void addTrain(Train train) {
            trainsByNumber.put(train.getTrainNumber(), train);
        }

        @Override
        public Train findByTrainNumber(String trainNumber) {
            return trainsByNumber.get(trainNumber);
        }

        @Override
        public Train findById(Long id) {
            return trainsByNumber.values().stream()
                    .filter(t -> t.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Train> findAll() {
            return List.copyOf(trainsByNumber.values());
        }
    }

    private static class FakeUserDAO extends UserDAO {
        private final Map<String, User> users = new HashMap<>();

        public void addUser(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public User findByUsername(String username) {
            return users.get(username);
        }

        @Override
        public User authenticate(String username, String rawPassword) {
            User user = users.get(username);
            if (user != null && PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash())) {
                return user;
            }
            return null;
        }
    }
}
