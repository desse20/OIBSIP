package com.oasisinfobyte.reservation.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;

/**
 * Common modern styling, colors, and UI utilities for Swing frames.
 */
public final class UITheme {

    public static final Color PRIMARY_COLOR = new Color(30, 58, 138);       // #1E3A8A Deep Navy
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);       // #1D4ED8
    public static final Color ACCENT_COLOR = new Color(37, 99, 235);        // #2563EB Royal Blue
    public static final Color SUCCESS_COLOR = new Color(22, 163, 74);       // #16A34A Green
    public static final Color DANGER_COLOR = new Color(220, 38, 38);        // #DC2626 Red
    public static final Color DANGER_HOVER = new Color(185, 28, 28);
    public static final Color BG_COLOR = new Color(241, 245, 249);          // #F1F5F9 Soft Slate
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);         // #0F172A
    public static final Color TEXT_MUTED = new Color(100, 116, 139);        // #64748B
    public static final Color BORDER_COLOR = new Color(203, 213, 225);      // #CBD5E1

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR, Color.WHITE, PRIMARY_HOVER);
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS_COLOR, Color.WHITE, new Color(16, 185, 129));
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER_COLOR, Color.WHITE, DANGER_HOVER);
    }

    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, new Color(226, 232, 240), TEXT_PRIMARY, new Color(203, 213, 225));
    }

    public static JButton createStyledButton(String text, Color bg, Color fg, Color hoverBg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(hoverBg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverBg);
                } else if (!isEnabled()) {
                    g2.setColor(new Color(203, 213, 225));
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(BOLD_FONT);
        button.setForeground(fg);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width + 24, 38));
        return button;
    }

    public static void styleTextField(JTextField textField) {
        textField.setFont(REGULAR_FONT);
        textField.setForeground(TEXT_PRIMARY);
        textField.setBackground(Color.WHITE);
        textField.setCaretColor(TEXT_PRIMARY);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 36));
    }

    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BOLD_FONT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        );
    }
}
