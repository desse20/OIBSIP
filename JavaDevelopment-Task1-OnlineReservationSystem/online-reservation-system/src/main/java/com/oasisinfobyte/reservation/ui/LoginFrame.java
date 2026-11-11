package com.oasisinfobyte.reservation.ui;

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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Authentication window for the Online Reservation System.
 */
public class LoginFrame extends JFrame {

    private final ReservationService reservationService;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;

    public LoginFrame(ReservationService reservationService) {
        this.reservationService = reservationService != null ? reservationService : new ReservationService();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Online Reservation System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 440);
        setMinimumSize(new Dimension(420, 400));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_COLOR);
        setLayout(new BorderLayout());

        // Header Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JLabel titleLabel = new JLabel("Online Reservation System", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Train & Transport Booking Portal", SwingConstants.CENTER);
        subtitleLabel.setFont(UITheme.REGULAR_FONT);
        subtitleLabel.setForeground(new Color(224, 231, 255));
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(subtitleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center Card Panel
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(UITheme.CARD_BG);
        formCard.setBorder(UITheme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formCard.add(UITheme.createFieldLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        usernameField = new JTextField(15);
        UITheme.styleTextField(usernameField);
        formCard.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formCard.add(UITheme.createFieldLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        passwordField = new JPasswordField(15);
        UITheme.styleTextField(passwordField);
        formCard.add(passwordField, gbc);

        // Buttons Panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 8, 8, 8);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        exitButton = UITheme.createSecondaryButton("Exit");
        exitButton.addActionListener(e -> handleExit());

        loginButton = UITheme.createPrimaryButton("Login");
        loginButton.addActionListener(e -> handleLogin());

        buttonPanel.add(exitButton);
        buttonPanel.add(loginButton);
        formCard.add(buttonPanel, gbc);

        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);

        // Default enter key action
        getRootPane().setDefaultButton(loginButton);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your username.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            usernameField.requestFocusInWindow();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your password.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocusInWindow();
            return;
        }

        try {
            User authenticatedUser = reservationService.authenticate(username, password);
            if (authenticatedUser != null) {
                dispose(); // Close Login Frame
                DashboardFrame dashboard = new DashboardFrame(authenticatedUser, reservationService);
                dashboard.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password.",
                        "Access Denied",
                        JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocusInWindow();
            }
        } catch (ReservationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Authentication Failed",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to connect to authentication service. Please verify database connectivity.",
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
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
