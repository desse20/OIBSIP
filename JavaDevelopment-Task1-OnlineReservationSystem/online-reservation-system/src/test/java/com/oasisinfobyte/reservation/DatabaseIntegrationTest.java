package com.oasisinfobyte.reservation;

import com.oasisinfobyte.reservation.config.DatabaseConnection;
import com.oasisinfobyte.reservation.dao.ReservationDAO;
import com.oasisinfobyte.reservation.dao.TrainDAO;
import com.oasisinfobyte.reservation.dao.UserDAO;
import com.oasisinfobyte.reservation.model.Reservation;
import com.oasisinfobyte.reservation.model.Train;
import com.oasisinfobyte.reservation.model.User;
import com.oasisinfobyte.reservation.service.ReservationService;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test against the real MySQL database.
 * Gracefully skips if the live MySQL server is not reachable in the current execution environment.
 */
public class DatabaseIntegrationTest {

    private ReservationService reservationService;
    private UserDAO userDAO;
    private TrainDAO trainDAO;

    @BeforeEach
    void setUp() {
        // Only run if database connection is available
        boolean dbAvailable = DatabaseConnection.testConnection();
        Assumptions.assumeTrue(dbAvailable, "MySQL database is not reachable in this test environment; skipping integration test.");

        userDAO = new UserDAO();
        trainDAO = new TrainDAO();
        reservationService = new ReservationService();
    }

    @Test
    @DisplayName("Verify user authentication against MySQL database")
    void testDatabaseAuthentication() {
        User adminUser = userDAO.authenticate("admin", "admin123");
        assertNotNull(adminUser, "Default admin user should authenticate successfully");
        assertEquals("admin", adminUser.getUsername());

        User testUser = userDAO.authenticate("user1", "user123");
        assertNotNull(testUser, "Default user1 should authenticate successfully");
        assertEquals("user1", testUser.getUsername());
    }

    @Test
    @DisplayName("Verify train retrieval from MySQL database")
    void testDatabaseTrainRetrieval() {
        List<Train> trains = trainDAO.findAll();
        assertFalse(trains.isEmpty(), "Train catalog should contain seeded trains");

        Train train12001 = trainDAO.findByTrainNumber("12001");
        assertNotNull(train12001, "Train 12001 should exist");
        assertEquals("Shatabdi Express", train12001.getTrainName());
        assertEquals("New Delhi", train12001.getSourceStation());
        assertEquals("Bhopal", train12001.getDestinationStation());
    }

    @Test
    @DisplayName("Verify full booking and cancellation lifecycle in MySQL")
    void testBookingAndCancellationLifecycle() {
        User user = userDAO.findByUsername("admin");
        assertNotNull(user, "Admin user must exist");

        // 1. Create reservation
        LocalDate journeyDate = LocalDate.now().plusDays(10);
        Reservation created = reservationService.createReservation(
                "Integration Test Passenger",
                "12001",
                "AC 2 Tier (2A)",
                journeyDate,
                "New Delhi",
                "Bhopal",
                user.getId()
        );

        assertNotNull(created);
        assertNotNull(created.getId(), "Reservation should receive auto-generated primary key");
        assertNotNull(created.getPnr(), "Reservation should receive unique PNR");
        String pnr = created.getPnr();

        // 2. Fetch reservation by PNR
        Reservation fetched = reservationService.findByPNR(pnr);
        assertNotNull(fetched);
        assertEquals(pnr, fetched.getPnr());
        assertEquals("Integration Test Passenger", fetched.getPassengerName());
        assertEquals("12001", fetched.getTrainNumber());
        assertEquals("Shatabdi Express", fetched.getTrainName());
        assertEquals("New Delhi", fetched.getSourceStation());
        assertEquals("Bhopal", fetched.getDestinationStation());

        // 3. Cancel reservation
        boolean cancelled = reservationService.cancelReservation(pnr);
        assertTrue(cancelled, "Reservation cancellation should return true");

        // 4. Verify reservation is no longer present
        assertNull(new ReservationDAO().findByPNR(pnr), "Reservation should be removed from database");
    }
}
