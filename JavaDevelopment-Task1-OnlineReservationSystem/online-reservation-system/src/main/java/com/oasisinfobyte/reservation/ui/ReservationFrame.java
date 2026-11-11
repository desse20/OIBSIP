package com.oasisinfobyte.reservation.ui;

import com.oasisinfobyte.reservation.model.Reservation;
import com.oasisinfobyte.reservation.model.Train;
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Train ticket reservation window.
 */
public class ReservationFrame extends JFrame {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final User currentUser;
    private final ReservationService reservationService;
    private final JFrame parentDashboard;

    private JTextField passengerNameField;
    private JTextField trainNumberField;
    private JComboBox<String> trainSelectorCombo;
    private JTextField trainNameField;
    private JComboBox<String> classTypeCombo;
    private JTextField journeyDateField;
    private JTextField sourceStationField;
    private JTextField destinationStationField;

    private JButton bookButton;
    private JButton clearButton;
    private JButton backButton;

    public ReservationFrame(User currentUser, ReservationService reservationService, JFrame parentDashboard) {
        this.currentUser = currentUser;
        this.reservationService = reservationService != null ? reservationService : new ReservationService();
        this.parentDashboard = parentDashboard;
        initializeUI();
        loadAvailableTrains();
    }

    private void initializeUI() {
        setTitle("Online Reservation System - New Train Reservation");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 680);
        setMinimumSize(new Dimension(600, 580));
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

        JLabel titleLabel = new JLabel("Train Ticket Reservation");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Logged in: " + currentUser.getUsername());
        userLabel.setFont(UITheme.REGULAR_FONT);
        userLabel.setForeground(new Color(224, 231, 255));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Form
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(UITheme.CARD_BG);
        formCard.setBorder(UITheme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 8, 7, 8);

        int row = 0;

        // 1. Passenger Name
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UITheme.createFieldLabel("1. Passenger Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        passengerNameField = new JTextField(20);
        UITheme.styleTextField(passengerNameField);
        formCard.add(passengerNameField, gbc);

        // 2. Select from Train Catalog (Helper)
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UITheme.createFieldLabel("Quick Select Train:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        trainSelectorCombo = new JComboBox<>();
        trainSelectorCombo.setFont(UITheme.REGULAR_FONT);
        trainSelectorCombo.setBackground(Color.WHITE);
        trainSelectorCombo.setPreferredSize(new Dimension(trainSelectorCombo.getPreferredSize().width, 36));
        trainSelectorCombo.addActionListener(e -> onTrainSelectedFromCombo());
        formCard.add(trainSelectorCombo, gbc);

        // 3. Train Number
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UITheme.createFieldLabel("2. Train Number:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JPanel trainNumPanel = new JPanel(new BorderLayout(8, 0));
        trainNumPanel.setOpaque(false);
        trainNumberField = new JTextField(10);
        UITheme.styleTextField(trainNumberField);
        trainNumberField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                onTrainNumberEntered();
            }
        });

        JButton lookupTrainBtn = UITheme.createSecondaryButton("Search Train");
        lookupTrainBtn.setPreferredSize(new Dimension(130, 36));
        lookupTrainBtn.addActionListener(e -> onTrainNumberEntered());

        trainNumPanel.add(trainNumberField, BorderLayout.CENTER);
        trainNumPanel.add(lookupTrainBtn, BorderLayout.EAST);
        formCard.add(trainNumPanel, gbc);

        // 4. Train Name (Auto-populated, read-only)
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UITheme.createFieldLabel("3. Train Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        trainNameField = new JTextField(20);
        UITheme.styleTextField(trainNameField);
        trainNameField.setEditable(false);
        trainNameField.setBackground(new Color(241, 245, 249));
        formCard.add(trainNameField, gbc);

        // 5. Class Type
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UITheme.createFieldLabel("4. Class Type:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        String[] classes = {
                "-- Select Class --",
                "AC First Class (1A)",
                "AC 2 Tier (2A)",
                "AC 3 Tier (3A)",
                "Executive Chair Car (EC)",
                "AC Chair Car (CC)",
                "Sleeper (SL)",
                "Second Sitting (2S)"
        };
        classTypeCombo = new JComboBox<>(classes);
        classTypeCombo.setFont(UITheme.REGULAR_FONT);
        classTypeCombo.setBackground(Color.WHITE);
        classTypeCombo.setPreferredSize(new Dimension(classTypeCombo.getPreferredSize().width, 36));
        formCard.add(classTypeCombo, gbc);

        // 6. Journey Date
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UITheme.createFieldLabel("5. Journey Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        journeyDateField = new JTextField(LocalDate.now().plusDays(1).format(DATE_FORMATTER), 15);
        UITheme.styleTextField(journeyDateField);
        formCard.add(journeyDateField, gbc);

        // 7. Source Station
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UITheme.createFieldLabel("6. Source Station:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        sourceStationField = new JTextField(20);
        UITheme.styleTextField(sourceStationField);
        formCard.add(sourceStationField, gbc);

        // 8. Destination Station
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UITheme.createFieldLabel("7. Destination Station:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        destinationStationField = new JTextField(20);
        UITheme.styleTextField(destinationStationField);
        formCard.add(destinationStationField, gbc);

        // Action Buttons
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 8, 8, 8);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);

        backButton = UITheme.createSecondaryButton("Back to Dashboard");
        backButton.addActionListener(e -> returnToDashboard());

        clearButton = UITheme.createSecondaryButton("Clear");
        clearButton.addActionListener(e -> clearForm());

        bookButton = UITheme.createSuccessButton("Book Ticket (Insert)");
        bookButton.addActionListener(e -> handleBooking());

        buttonPanel.add(backButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(bookButton);
        formCard.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(formCard);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private void loadAvailableTrains() {
        trainSelectorCombo.removeAllItems();
        trainSelectorCombo.addItem("-- Select from available trains --");
        try {
            List<Train> trains = reservationService.getAllTrains();
            for (Train t : trains) {
                trainSelectorCombo.addItem(t.getTrainNumber() + " - " + t.getTrainName() +
                        " (" + t.getSourceStation() + " -> " + t.getDestinationStation() + ")");
            }
        } catch (Exception e) {
            // Keep combo empty if failed
        }
    }

    private void onTrainSelectedFromCombo() {
        int index = trainSelectorCombo.getSelectedIndex();
        if (index <= 0) {
            return;
        }
        String selected = (String) trainSelectorCombo.getSelectedItem();
        if (selected != null && selected.contains(" - ")) {
            String trainNumber = selected.split(" - ")[0].trim();
            trainNumberField.setText(trainNumber);
            onTrainNumberEntered();
        }
    }

    private void onTrainNumberEntered() {
        String number = trainNumberField.getText().trim();
        if (number.isEmpty()) {
            trainNameField.setText("");
            return;
        }

        try {
            Train train = reservationService.findTrainByNumber(number);
            if (train != null) {
                trainNameField.setText(train.getTrainName());
                sourceStationField.setText(train.getSourceStation());
                destinationStationField.setText(train.getDestinationStation());
            } else {
                trainNameField.setText("");
                JOptionPane.showMessageDialog(this,
                        "No train found with number: " + number,
                        "Train Not Found",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (ReservationException ex) {
            trainNameField.setText("");
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error looking up train details: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleBooking() {
        String passengerName = passengerNameField.getText().trim();
        String trainNumber = trainNumberField.getText().trim();
        String trainName = trainNameField.getText().trim();
        String classType = (String) classTypeCombo.getSelectedItem();
        String dateStr = journeyDateField.getText().trim();
        String sourceStation = sourceStationField.getText().trim();
        String destinationStation = destinationStationField.getText().trim();

        // 1. Basic front validations
        if (passengerName.isEmpty()) {
            showError("Passenger Name is required.");
            passengerNameField.requestFocusInWindow();
            return;
        }
        if (trainNumber.isEmpty()) {
            showError("Train Number is required.");
            trainNumberField.requestFocusInWindow();
            return;
        }
        if (!trainNumber.matches("^\\d+$")) {
            showError("Train Number must be numeric.");
            trainNumberField.requestFocusInWindow();
            return;
        }
        if (classType == null || classType.startsWith("--")) {
            showError("Please select a valid travel Class.");
            classTypeCombo.requestFocusInWindow();
            return;
        }
        if (dateStr.isEmpty()) {
            showError("Journey Date is required.");
            journeyDateField.requestFocusInWindow();
            return;
        }

        LocalDate journeyDate;
        try {
            journeyDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            showError("Invalid Date format. Please use YYYY-MM-DD (e.g., 2026-11-15).");
            journeyDateField.requestFocusInWindow();
            return;
        }

        if (journeyDate.isBefore(LocalDate.now())) {
            showError("Journey Date cannot be in the past.");
            journeyDateField.requestFocusInWindow();
            return;
        }

        if (sourceStation.isEmpty()) {
            showError("Source Station cannot be empty.");
            sourceStationField.requestFocusInWindow();
            return;
        }
        if (destinationStation.isEmpty()) {
            showError("Destination Station cannot be empty.");
            destinationStationField.requestFocusInWindow();
            return;
        }
        if (sourceStation.equalsIgnoreCase(destinationStation)) {
            showError("Source and Destination stations cannot be identical.");
            return;
        }

        // 2. Execute reservation via Service layer
        try {
            Reservation savedReservation = reservationService.createReservation(
                    passengerName,
                    trainNumber,
                    classType,
                    journeyDate,
                    sourceStation,
                    destinationStation,
                    currentUser.getId()
            );

            // 3. Display Confirmation Dialog containing all required items
            String confirmationMessage = String.format(
                    "===============================================\n" +
                    "           BOOKING CONFIRMED SUCCESSFULLY\n" +
                    "===============================================\n" +
                    " • PNR Number:          %s\n" +
                    " • Passenger Name:      %s\n" +
                    " • Train Number:        %s\n" +
                    " • Train Name:          %s\n" +
                    " • Class Type:          %s\n" +
                    " • Journey Date:        %s\n" +
                    " • Source Station:      %s\n" +
                    " • Destination Station: %s\n" +
                    "===============================================\n" +
                    "Please save your PNR number for future reference or cancellation.",
                    savedReservation.getPnr(),
                    savedReservation.getPassengerName(),
                    savedReservation.getTrainNumber(),
                    savedReservation.getTrainName(),
                    savedReservation.getClassType(),
                    savedReservation.getJourneyDate().format(DATE_FORMATTER),
                    savedReservation.getSourceStation(),
                    savedReservation.getDestinationStation()
            );

            JOptionPane.showMessageDialog(this,
                    confirmationMessage,
                    "Reservation Successful - PNR: " + savedReservation.getPnr(),
                    JOptionPane.INFORMATION_MESSAGE);

            clearForm();

        } catch (ReservationException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("An unexpected error occurred while processing reservation: " + ex.getMessage());
        }
    }

    private void clearForm() {
        passengerNameField.setText("");
        trainNumberField.setText("");
        trainNameField.setText("");
        trainSelectorCombo.setSelectedIndex(0);
        classTypeCombo.setSelectedIndex(0);
        journeyDateField.setText(LocalDate.now().plusDays(1).format(DATE_FORMATTER));
        sourceStationField.setText("");
        destinationStationField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Reservation Error",
                JOptionPane.WARNING_MESSAGE);
    }

    private void returnToDashboard() {
        dispose();
        if (parentDashboard != null) {
            parentDashboard.setVisible(true);
        }
    }
}
