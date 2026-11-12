
package com.oasisinfobyte.reservation.ui;

import com.oasisinfobyte.reservation.model.User;
import com.oasisinfobyte.reservation.service.ReservationException;
import com.oasisinfobyte.reservation.service.ReservationService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
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

    private static final Color NAVY = new Color(20, 52, 89);
    private static final Color BLUE = new Color(39, 104, 184);
    private static final Color LIGHT_BG = new Color(244, 247, 251);
    private static final Color TEXT = new Color(37, 48, 65);

    private static final String APP_ICON_PATH =
            "/images/train-reservation-icon.png";

    private final ReservationService reservationService;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;

    public LoginFrame(ReservationService reservationService) {
        this.reservationService = reservationService != null
                ? reservationService
                : new ReservationService();

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Online Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        // Application icon in the window title bar
        URL iconUrl = getClass().getResource(APP_ICON_PATH);

        if (iconUrl != null) {
            setIconImage(new ImageIcon(iconUrl).getImage());
        }

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(LIGHT_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        // Left panel: branding and train image
        gbc.gridx = 0;
        gbc.weightx = 1.05;
        root.add(createTrainPanel(), gbc);

        // Right panel: login form
        gbc.gridx = 1;
        gbc.weightx = 0.95;
        root.add(createFormPanel(), gbc);

        setContentPane(root);

        // Default Enter key action
        getRootPane().setDefaultButton(loginButton);
    }

    /**
     * Creates the left branding panel with the train image.
     */
    private JPanel createTrainPanel() {
        JPanel panel = new ImageTrainPanel();
        panel.setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(
                BorderFactory.createEmptyBorder(42, 38, 0, 25)
        );

        JLabel titleLabel = new JLabel("Online Reservation System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 25));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Train & Transport Booking Portal"
        );
        subtitleLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );
        subtitleLabel.setForeground(new Color(218, 231, 246));
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(12));
        header.add(subtitleLabel);

        panel.add(header, BorderLayout.NORTH);

        return panel;
    }

    /**
     * Creates the login form.
     */
    private JPanel createFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(LIGHT_BG);
        wrapper.setBorder(
                BorderFactory.createEmptyBorder(35, 35, 35, 35)
        );

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(228, 234, 242), 1
                ),
                BorderFactory.createEmptyBorder(32, 28, 32, 28)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Form title
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);

        JLabel formTitle = new JLabel("Login", SwingConstants.LEFT);
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 27));
        formTitle.setForeground(NAVY);
        formCard.add(formTitle, gbc);

        // Blue separator
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 23, 0);

        JPanel separatorWrapper = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );
        separatorWrapper.setOpaque(false);

        JPanel separator = new JPanel();
        separator.setBackground(BLUE);
        separator.setPreferredSize(new Dimension(48, 3));

        separatorWrapper.add(separator);
        formCard.add(separatorWrapper, gbc);

        // Username
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 0, 7, 0);
        formCard.add(createFieldLabel("Username:"), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 17, 0);

        usernameField = new JTextField(18);
        styleField(usernameField);
        formCard.add(usernameField, gbc);

        // Password
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 0, 7, 0);
        formCard.add(createFieldLabel("Password:"), gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 25, 0);

        passwordField = new JPasswordField(18);
        styleField(passwordField);
        formCard.add(passwordField, gbc);

        // Buttons
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        buttonPanel.setOpaque(false);

        exitButton = UITheme.createSecondaryButton("Exit");
        exitButton.addActionListener(e -> handleExit());

        loginButton = UITheme.createPrimaryButton("Login");
        loginButton.addActionListener(e -> handleLogin());

        buttonPanel.add(exitButton);
        buttonPanel.add(loginButton);

        formCard.add(buttonPanel, gbc);

        wrapper.add(formCard);

        return wrapper;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(TEXT);

        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(new Color(249, 251, 254));
        field.setCaretColor(BLUE);
        field.setPreferredSize(new Dimension(280, 43));

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(216, 225, 237), 1
                ),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your username.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );

            usernameField.requestFocusInWindow();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your password.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );

            passwordField.requestFocusInWindow();
            return;
        }

        try {
            User authenticatedUser =
                    reservationService.authenticate(username, password);

            if (authenticatedUser != null) {
                dispose();

                DashboardFrame dashboard =
                        new DashboardFrame(
                                authenticatedUser,
                                reservationService
                        );

                dashboard.setVisible(true);

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid username or password.",
                        "Access Denied",
                        JOptionPane.ERROR_MESSAGE
                );

                passwordField.setText("");
                passwordField.requestFocusInWindow();
            }

        } catch (ReservationException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Authentication Failed",
                    JOptionPane.ERROR_MESSAGE
            );

            passwordField.setText("");
            passwordField.requestFocusInWindow();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to connect to authentication service. "
                            + "Please verify database connectivity.",
                    "System Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit the application?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    /**
     * Left panel displaying the generated train image.
     * The Java2D train illustration has been removed.
     */
    private static class ImageTrainPanel extends JPanel {

        private final Image trainImage;

        ImageTrainPanel() {
            setBackground(NAVY);

            URL imageUrl = LoginFrame.class.getResource(
                    APP_ICON_PATH
            );

            if (imageUrl != null) {
                trainImage = new ImageIcon(imageUrl).getImage();
            } else {
                trainImage = null;
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int w = getWidth();
            int h = getHeight();

            // Blue gradient background
            g.setPaint(new GradientPaint(
                    0, 0, new Color(19, 48, 82),
                    0, h, new Color(71, 128, 165)
            ));

            g.fillRect(0, 0, w, h);

            // Display the generated train image
            if (trainImage != null) {

                int imageSize = Math.min(
                        300,
                        Math.min(w - 60, h - 180)
                );

                imageSize = Math.max(100, imageSize);

                int imageX = (w - imageSize) / 2;
                int availableHeight = h - 145;

                int imageY = 120
                        + (availableHeight - imageSize) / 2;

                g.drawImage(
                        trainImage,
                        imageX,
                        imageY,
                        imageSize,
                        imageSize,
                        this
                );

            } else {
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.PLAIN, 14));

                g.drawString(
                        "Train image not found",
                        40,
                        h / 2
                );
            }

            g.dispose();
        }
    }
}
