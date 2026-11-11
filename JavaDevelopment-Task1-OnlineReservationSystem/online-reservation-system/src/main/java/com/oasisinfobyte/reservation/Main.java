package com.oasisinfobyte.reservation;

import com.oasisinfobyte.reservation.config.DatabaseConnection;
import com.oasisinfobyte.reservation.service.ReservationService;
import com.oasisinfobyte.reservation.ui.LoginFrame;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for the Online Reservation System.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // 1. Configure modern UI Look and Feel
        configureLookAndFeel();

        // 2. Test database connection asynchronously or log status
        boolean dbConnected = DatabaseConnection.testConnection();
        if (!dbConnected) {
            LOGGER.warning("Could not establish immediate connection to MySQL database at: "
                    + DatabaseConnection.getDatabaseUrl()
                    + ". Check your credentials in db.properties or environment variables.");
        } else {
            LOGGER.info("Database connection established successfully to: "
                    + DatabaseConnection.getDatabaseUrl());
        }

        // 3. Launch UI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            ReservationService reservationService = new ReservationService();
            LoginFrame loginFrame = new LoginFrame(reservationService);
            loginFrame.setVisible(true);

            if (!dbConnected) {
                JOptionPane.showMessageDialog(loginFrame,
                        "Warning: Could not connect to MySQL database at:\n"
                                + DatabaseConnection.getDatabaseUrl() + "\n\n"
                                + "Please check that MySQL is running and verify db.properties or DB_URL / DB_PASSWORD environment variables.",
                        "Database Connection Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private static void configureLookAndFeel() {
        try {
            // Attempt System Look and Feel first
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                // Fallback to Nimbus
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "Failed to apply custom Look and Feel, using standard default.", ex);
            }
        }
    }
}
