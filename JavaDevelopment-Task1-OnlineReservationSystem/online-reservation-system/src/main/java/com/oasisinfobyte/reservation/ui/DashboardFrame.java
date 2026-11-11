package com.oasisinfobyte.reservation.ui;

import com.oasisinfobyte.reservation.model.Train;
import com.oasisinfobyte.reservation.model.User;
import com.oasisinfobyte.reservation.service.ReservationService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;
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

/**
 * Main dashboard displayed after successful authentication.
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    private final ReservationService reservationService;
    private final boolean isAdmin;

    public DashboardFrame(User currentUser, ReservationService reservationService) {
        this.currentUser = currentUser;
        this.reservationService = reservationService != null ? reservationService : new ReservationService();
        this.isAdmin = "admin".equalsIgnoreCase(currentUser.getUsername());
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Online Reservation System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 560);
        setMinimumSize(new Dimension(680, 480));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_COLOR);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setFont(UITheme.TITLE_FONT);
        welcomeLabel.setForeground(Color.WHITE);

        JLabel descLabel = new JLabel("Train & Transport Online Reservation Dashboard");
        descLabel.setFont(UITheme.REGULAR_FONT);
        descLabel.setForeground(new Color(224, 231, 255));

        headerLeft.add(welcomeLabel);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(descLabel);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content - Menu Action Cards
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 16, 16));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(720, 340));

        // 1. New Reservation Card
        JButton newReservationBtn = UITheme.createPrimaryButton("Book Ticket");
        newReservationBtn.setFont(UITheme.SUBTITLE_FONT);
        newReservationBtn.addActionListener(e -> openReservationFrame());
        cardsPanel.add(createActionCard("New Reservation", "Book train tickets and generate a unique PNR.", newReservationBtn));

        // 2. View Reservations Card
        JButton viewReservationsBtn = UITheme.createSuccessButton("View Reservations");
        viewReservationsBtn.setFont(UITheme.SUBTITLE_FONT);
        viewReservationsBtn.addActionListener(e -> openViewReservationsFrame());
        cardsPanel.add(createActionCard("Reservations", "Inspect, search and manage booked tickets.", viewReservationsBtn));

        // 3. Cancel Reservation Card
        JButton cancelReservationBtn = UITheme.createDangerButton("Cancel Ticket");
        cancelReservationBtn.setFont(UITheme.SUBTITLE_FONT);
        cancelReservationBtn.addActionListener(e -> openCancellationFrame());
        cardsPanel.add(createActionCard("Cancel Ticket", "Look up by PNR and cancel existing bookings.", cancelReservationBtn));

        // 4. Train Catalog Card
        JButton catalogBtn = UITheme.createSecondaryButton("Train Catalog");
        catalogBtn.setFont(UITheme.SUBTITLE_FONT);
        catalogBtn.addActionListener(e -> showTrainCatalog());
        cardsPanel.add(createActionCard("Trains & Routes", "Browse scheduled train lines and stations.", catalogBtn));

        // 5. Logout Card
        JButton logoutBtn = UITheme.createSecondaryButton("Logout");
        logoutBtn.setFont(UITheme.SUBTITLE_FONT);
        logoutBtn.addActionListener(e -> handleLogout());
        cardsPanel.add(createActionCard("Session", "Sign out of your user account.", logoutBtn));

        // 6. Exit Application Card
        JButton exitBtn = UITheme.createSecondaryButton("Exit");
        exitBtn.setFont(UITheme.SUBTITLE_FONT);
        exitBtn.addActionListener(e -> handleExit());
        cardsPanel.add(createActionCard("Exit System", "Safely close the application.", exitBtn));

        centerPanel.add(cardsPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Footer info
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(UITheme.BG_COLOR);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 12, 8));

        JLabel footerLabel = new JLabel("OASIS INFOBYTE Internship Task 1 — Online Reservation System");
        footerLabel.setFont(UITheme.SMALL_FONT);
        footerLabel.setForeground(UITheme.TEXT_MUTED);
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createActionCard(String title, String description, JButton actionButton) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(UITheme.createCardBorder());

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.SUBTITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(UITheme.SMALL_FONT);
        descLabel.setForeground(UITheme.TEXT_MUTED);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(descLabel);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(actionButton, BorderLayout.SOUTH);
        return card;
    }

    private void openReservationFrame() {
        ReservationFrame resFrame = new ReservationFrame(currentUser, reservationService, this);
        resFrame.setVisible(true);
        setVisible(false);
    }

    private void openViewReservationsFrame() {
        ViewReservationsFrame viewFrame = new ViewReservationsFrame(currentUser, reservationService, this);
        viewFrame.setVisible(true);
        setVisible(false);
    }

    private void openCancellationFrame() {
        CancellationFrame cancelFrame = new CancellationFrame(currentUser, reservationService, this);
        cancelFrame.setVisible(true);
        setVisible(false);
    }

    private void showTrainCatalog() {
        try {
            List<Train> trains = reservationService.getAllTrains();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-10s %-24s %-20s %-20s\n", "Train No.", "Train Name", "Origin", "Destination"));
            sb.append("--------------------------------------------------------------------------------\n");
            for (Train t : trains) {
                sb.append(String.format("%-10s %-24s %-20s %-20s\n",
                        t.getTrainNumber(), t.getTrainName(), t.getSourceStation(), t.getDestinationStation()));
            }

            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
            area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(650, 300));
            sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

            JOptionPane.showMessageDialog(this, sp, "Available Trains Catalog", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load trains catalog: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            LoginFrame loginFrame = new LoginFrame(reservationService);
            loginFrame.setVisible(true);
        }
    }

    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit the application?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
