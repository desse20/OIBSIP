package com.oasisinfobyte.reservation.ui;

import com.oasisinfobyte.reservation.model.Reservation;
import com.oasisinfobyte.reservation.model.User;
import com.oasisinfobyte.reservation.service.ReservationException;
import com.oasisinfobyte.reservation.service.ReservationService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/**
 * Interface allowing users (especially Administrators) to inspect, filter,
 * and manage existing reservations.
 */
public class ViewReservationsFrame extends JFrame {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final User currentUser;
    private final ReservationService reservationService;
    private final JFrame parentDashboard;
    private final boolean isAdmin;

    private JTable reservationsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JLabel countLabel;
    private JButton cancelSelectedBtn;
    private JButton refreshBtn;
    private JButton backBtn;

    public ViewReservationsFrame(User currentUser, ReservationService reservationService, JFrame parentDashboard) {
        this.currentUser = currentUser;
        this.reservationService = reservationService != null ? reservationService : new ReservationService();
        this.parentDashboard = parentDashboard;
        this.isAdmin = "admin".equalsIgnoreCase(currentUser.getUsername());

        initializeUI();
        loadReservationsData();
    }

    private void initializeUI() {
        setTitle("Online Reservation System - Reservations");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(920, 580);
        setMinimumSize(new Dimension(800, 480));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_COLOR);
        setLayout(new BorderLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                returnToDashboard();
            }
        });

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel titleLabel = new JLabel("Reservation Records");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);

        countLabel = new JLabel("Loading reservations...");
        countLabel.setFont(UITheme.REGULAR_FONT);
        countLabel.setForeground(new Color(224, 231, 255));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(countLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content Panel
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Filter / Search Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(UITheme.CARD_BG);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JLabel searchLabel = new JLabel("Filter / Search:");
        searchLabel.setFont(UITheme.BOLD_FONT);
        searchLabel.setForeground(UITheme.TEXT_PRIMARY);

        searchField = new JTextField(24);
        UITheme.styleTextField(searchField);
        searchField.setToolTipText("Filter by PNR, Passenger, Train, or Station");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTable();
            }
        });

        JButton clearFilterBtn = UITheme.createSecondaryButton("Clear Search");
        clearFilterBtn.setPreferredSize(new Dimension(110, 36));
        clearFilterBtn.addActionListener(e -> searchField.setText(""));

        toolbar.add(searchLabel);
        toolbar.add(searchField);
        toolbar.add(clearFilterBtn);
        centerPanel.add(toolbar, BorderLayout.NORTH);

        // Reservations Table
        String[] columns = {
                "PNR", "Passenger Name", "Train No.", "Train Name",
                "Class", "Journey Date", "From", "To", "Booked At"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only cells
            }
        };

        reservationsTable = new JTable(tableModel);
        reservationsTable.setFont(UITheme.REGULAR_FONT);
        reservationsTable.setRowHeight(30);
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationsTable.setGridColor(new Color(226, 232, 240));
        reservationsTable.setShowVerticalLines(true);
        reservationsTable.setShowHorizontalLines(true);

        // Header Styling
        JTableHeader tableHeader = reservationsTable.getTableHeader();
        tableHeader.setFont(UITheme.BOLD_FONT);
        tableHeader.setBackground(new Color(241, 245, 249));
        tableHeader.setForeground(UITheme.TEXT_PRIMARY);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getPreferredSize().width, 36));

        // Center renderers for specific columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        reservationsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // PNR
        reservationsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Train No
        reservationsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Date

        // Row sorter for search filter
        rowSorter = new TableRowSorter<>(tableModel);
        reservationsTable.setRowSorter(rowSorter);

        // Selection listener to toggle cancel button
        reservationsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = reservationsTable.getSelectedRow() != -1;
            cancelSelectedBtn.setEnabled(hasSelection);
        });

        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // 3. Actions Panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        backBtn = UITheme.createSecondaryButton("Back to Dashboard");
        backBtn.addActionListener(e -> returnToDashboard());

        refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadReservationsData());

        cancelSelectedBtn = UITheme.createDangerButton("Cancel Selected Ticket");
        cancelSelectedBtn.setEnabled(false);
        cancelSelectedBtn.addActionListener(e -> handleCancelSelected());

        actionsPanel.add(backBtn);
        actionsPanel.add(refreshBtn);
        actionsPanel.add(cancelSelectedBtn);

        centerPanel.add(actionsPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadReservationsData() {
        tableModel.setRowCount(0);
        try {
            List<Reservation> reservations = isAdmin
                    ? reservationService.getAllReservations()
                    : reservationService.getReservationsByUserId(currentUser.getId());

            for (Reservation r : reservations) {
                tableModel.addRow(new Object[]{
                        r.getPnr(),
                        r.getPassengerName(),
                        r.getTrainNumber(),
                        r.getTrainName(),
                        r.getClassType(),
                        r.getJourneyDate() != null ? r.getJourneyDate().format(DATE_FORMATTER) : "N/A",
                        r.getSourceStation(),
                        r.getDestinationStation(),
                        r.getCreatedAt() != null ? r.getCreatedAt().toString().substring(0, 16) : "N/A"
                });
            }

            countLabel.setText("Total Records: " + reservations.size());
            cancelSelectedBtn.setEnabled(false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load reservations: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    private void handleCancelSelected() {
        int selectedViewRow = reservationsTable.getSelectedRow();
        if (selectedViewRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a reservation row from the table.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = reservationsTable.convertRowIndexToModel(selectedViewRow);
        String pnr = (String) tableModel.getValueAt(modelRow, 0);
        String passengerName = (String) tableModel.getValueAt(modelRow, 1);
        String trainInfo = tableModel.getValueAt(modelRow, 2) + " - " + tableModel.getValueAt(modelRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this reservation?\n\n" +
                "PNR: " + pnr + "\n" +
                "Passenger: " + passengerName + "\n" +
                "Train: " + trainInfo + "\n\n" +
                "This action cannot be undone.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean cancelled = reservationService.cancelReservation(pnr);
                if (cancelled) {
                    JOptionPane.showMessageDialog(this,
                            "Reservation " + pnr + " successfully cancelled.",
                            "Cancellation Confirmed",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadReservationsData();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not cancel reservation " + pnr + ".",
                            "Cancellation Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
            } catch (ReservationException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Unexpected error: " + ex.getMessage(),
                        "System Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void returnToDashboard() {
        dispose();
        if (parentDashboard != null) {
            parentDashboard.setVisible(true);
        }
    }
}
