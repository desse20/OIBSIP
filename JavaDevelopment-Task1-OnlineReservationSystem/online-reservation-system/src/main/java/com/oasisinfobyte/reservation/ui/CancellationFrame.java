package com.oasisinfobyte.reservation.ui;

import com.oasisinfobyte.reservation.model.Reservation;
import com.oasisinfobyte.reservation.model.User;
import com.oasisinfobyte.reservation.service.ReservationException;
import com.oasisinfobyte.reservation.service.ReservationService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Cancellation interface allowing users to look up reservations by PNR
 * and cancel them after explicit confirmation.
 */
public class CancellationFrame extends JFrame {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final User currentUser;
    private final ReservationService reservationService;
    private final JFrame parentDashboard;

    private JTextField pnrField;
    private JButton fetchButton;
    private JTextArea detailsTextArea;
    private JButton confirmCancelButton;
    private JButton backButton;

    private Reservation currentFetchedReservation;

    public CancellationFrame(User currentUser, ReservationService reservationService, JFrame parentDashboard) {
        this.currentUser = currentUser;
        this.reservationService = reservationService != null ? reservationService : new ReservationService();
        this.parentDashboard = parentDashboard;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Online Reservation System - Cancel Reservation");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(650, 560);
        setMinimumSize(new Dimension(550, 480));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_COLOR);
        setLayout(new BorderLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                returnToDashboard();
            }
        });

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel titleLabel = new JLabel("Ticket Cancellation");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Logged in: " + currentUser.getUsername());
        userLabel.setFont(UITheme.REGULAR_FONT);
        userLabel.setForeground(new Color(224, 231, 255));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content Panel
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. PNR Search Card
        JPanel searchCard = new JPanel(new GridBagLayout());
        searchCard.setBackground(UITheme.CARD_BG);
        searchCard.setBorder(UITheme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.25;
        searchCard.add(UITheme.createFieldLabel("Enter PNR Number:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        pnrField = new JTextField(15);
        UITheme.styleTextField(pnrField);
        pnrField.addActionListener(e -> handleFetchReservation());
        searchCard.add(pnrField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        fetchButton = UITheme.createPrimaryButton("Fetch Details");
        fetchButton.addActionListener(e -> handleFetchReservation());
        searchCard.add(fetchButton, gbc);

        centerPanel.add(searchCard, BorderLayout.NORTH);

        // 2. Reservation Details Display Area
        JPanel detailsCard = new JPanel(new BorderLayout());
        detailsCard.setBackground(UITheme.CARD_BG);
        detailsCard.setBorder(UITheme.createCardBorder());

        JLabel detailsLabel = new JLabel("Reservation Details:");
        detailsLabel.setFont(UITheme.SUBTITLE_FONT);
        detailsLabel.setForeground(UITheme.TEXT_PRIMARY);
        detailsCard.add(detailsLabel, BorderLayout.NORTH);

        detailsTextArea = new JTextArea();
        detailsTextArea.setEditable(false);
        detailsTextArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
        detailsTextArea.setForeground(UITheme.TEXT_PRIMARY);
        detailsTextArea.setBackground(new Color(248, 250, 252));
        detailsTextArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        detailsTextArea.setText("\n   Please enter a valid PNR number above and click 'Fetch Details'.");

        JScrollPane scrollPane = new JScrollPane(detailsTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        detailsCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(detailsCard, BorderLayout.CENTER);

        // 3. Actions Panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        backButton = UITheme.createSecondaryButton("Back to Dashboard");
        backButton.addActionListener(e -> returnToDashboard());

        confirmCancelButton = UITheme.createDangerButton("Confirm Cancellation");
        confirmCancelButton.setEnabled(false); // Disabled until valid reservation is fetched
        confirmCancelButton.addActionListener(e -> handleConfirmCancellation());

        actionsPanel.add(backButton);
        actionsPanel.add(confirmCancelButton);

        centerPanel.add(actionsPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void handleFetchReservation() {
        String pnr = pnrField.getText().trim();
        if (pnr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a PNR number to fetch.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE);
            pnrField.requestFocusInWindow();
            return;
        }

        try {
            Reservation res = reservationService.findByPNR(pnr);
            currentFetchedReservation = res;

            String details = String.format(
                    "----------------------------------------------------------\n" +
                    " RESERVATION DETAILS FOUND\n" +
                    "----------------------------------------------------------\n" +
                    " PNR Number:          %s\n" +
                    " Passenger Name:      %s\n" +
                    " Train Number:        %s\n" +
                    " Train Name:          %s\n" +
                    " Class Type:          %s\n" +
                    " Journey Date:        %s\n" +
                    " Source Station:      %s\n" +
                    " Destination Station: %s\n" +
                    " Booking Timestamp:   %s\n" +
                    "----------------------------------------------------------\n" +
                    " Status: CONFIRMED (Ready for cancellation if requested)\n" +
                    "----------------------------------------------------------",
                    res.getPnr(),
                    res.getPassengerName(),
                    res.getTrainNumber(),
                    res.getTrainName(),
                    res.getClassType(),
                    res.getJourneyDate().format(DATE_FORMATTER),
                    res.getSourceStation(),
                    res.getDestinationStation(),
                    res.getCreatedAt() != null ? res.getCreatedAt().toString() : "N/A"
            );

            detailsTextArea.setText(details);
            confirmCancelButton.setEnabled(true); // Enable cancellation button

        } catch (ReservationException ex) {
            currentFetchedReservation = null;
            confirmCancelButton.setEnabled(false);
            detailsTextArea.setText("\n   [No valid reservation found for PNR: " + pnr + "]");
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "PNR Lookup Failed",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            currentFetchedReservation = null;
            confirmCancelButton.setEnabled(false);
            detailsTextArea.setText("\n   Error retrieving reservation details.");
            JOptionPane.showMessageDialog(this,
                    "Error querying database: " + ex.getMessage(),
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleConfirmCancellation() {
        if (currentFetchedReservation == null) {
            JOptionPane.showMessageDialog(this,
                    "No valid reservation has been fetched.",
                    "Action Denied",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pnr = currentFetchedReservation.getPnr();

        // 6. Confirmation dialog: "Are you sure you want to cancel this reservation?"
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this reservation?\n\n" +
                "PNR: " + pnr + "\n" +
                "Passenger: " + currentFetchedReservation.getPassengerName() + "\n" +
                "Train: " + currentFetchedReservation.getTrainNumber() + " - " + currentFetchedReservation.getTrainName() + "\n\n" +
                "This action cannot be undone.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                // 7. Remove reservation from database
                boolean cancelled = reservationService.cancelReservation(pnr);
                if (cancelled) {
                    // 9. Display success message
                    JOptionPane.showMessageDialog(this,
                            "Reservation with PNR " + pnr + " has been successfully cancelled.",
                            "Cancellation Confirmed",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Reset form state
                    pnrField.setText("");
                    detailsTextArea.setText("\n   Reservation " + pnr + " was successfully cancelled.\n   Enter another PNR above to search again.");
                    currentFetchedReservation = null;
                    confirmCancelButton.setEnabled(false);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not cancel reservation. It may have already been cancelled.",
                            "Cancellation Failed",
                            JOptionPane.WARNING_MESSAGE);
                }
            } catch (ReservationException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Cancellation Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Unexpected error during cancellation: " + ex.getMessage(),
                        "System Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        // 8. If declined, keep reservation unchanged
    }

    private void returnToDashboard() {
        dispose();
        if (parentDashboard != null) {
            parentDashboard.setVisible(true);
        }
    }
}
