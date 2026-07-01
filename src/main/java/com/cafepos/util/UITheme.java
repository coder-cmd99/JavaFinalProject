package com.cafepos.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Centralised UI theme for the Café POS.
 *
 * Palette: warm espresso browns, soft cream whites, terracotta accents.
 * Typography: SansSerif for legibility; bold weights for headers/prices.
 */
public final class UITheme {

    // ── Colour palette ────────────────────────────────────────────────────────
    public static final Color ESPRESSO        = new Color(0x3E2723);  // deep brown
    public static final Color ROAST           = new Color(0x5D4037);  // medium brown
    public static final Color MOCHA           = new Color(0x795548);  // warm brown
    public static final Color LATTE           = new Color(0xA1887F);  // light brown
    public static final Color CREAM           = new Color(0xFFF8F0);  // warm off-white
    public static final Color STEAM           = new Color(0xF5EDE3);  // subtle cream
    public static final Color TERRACOTTA      = new Color(0xBF5E2E);  // accent
    public static final Color TERRACOTTA_DARK = new Color(0x8D3F1A);  // pressed
    public static final Color GOLD            = new Color(0xD4A94D);  // highlight
    public static final Color SUCCESS_GREEN   = new Color(0x388E3C);
    public static final Color ERROR_RED       = new Color(0xC62828);
    public static final Color TEXT_PRIMARY    = new Color(0x212121);
    public static final Color TEXT_SECONDARY  = new Color(0x616161);
    public static final Color DIVIDER         = new Color(0xD7C5B7);
    public static final Color CARD_BG         = Color.WHITE;
    public static final Color SIDEBAR_BG      = ESPRESSO;
    public static final Color HEADER_BG       = ROAST;
    public static final Color MAIN_BG         = CREAM;
    public static final Color UNAVAILABLE_BG  = new Color(0xEEEEEE);

    // ── Typography ────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD,  16);
    public static final Font FONT_SUBHEAD = new Font("SansSerif", Font.BOLD,  13);
    public static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_PRICE   = new Font("SansSerif", Font.BOLD,  14);
    public static final Font FONT_EMOJI   = new Font("Segoe UI Emoji", Font.PLAIN, 28);

    // ── Spacing / sizing ──────────────────────────────────────────────────────
    public static final int  GAP       = 10;
    public static final int  PAD       = 16;
    public static final int  RADIUS    = 12;
    public static final int  BTN_H     = 40;
    public static final int  CARD_W    = 180;
    public static final int  CARD_H    = 160;

    private UITheme() {}

    // ── Button factory ────────────────────────────────────────────────────────

    /** Primary action button (terracotta). */
    public static JButton primaryButton(String text) {
        return styledButton(text, TERRACOTTA, Color.WHITE);
    }

    /** Secondary / neutral button (roast brown). */
    public static JButton secondaryButton(String text) {
        return styledButton(text, ROAST, Color.WHITE);
    }

    /** Danger button (red). */
    public static JButton dangerButton(String text) {
        return styledButton(text, ERROR_RED, Color.WHITE);
    }

    /** Ghost / outline button. */
    public static JButton ghostButton(String text) {
        JButton btn = styledButton(text, CREAM, ROAST);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ROAST, 1, true),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        return btn;
    }

    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed()  ? bg.darker()
                           : getModel().isRollover() ? bg.brighter()
                           : bg;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_SUBHEAD);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, BTN_H));
        return btn;
    }

    // ── Card panel ────────────────────────────────────────────────────────────

    /** A rounded white card with a subtle drop-shadow effect. */
    public static JPanel cardPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(3, 4, getWidth()-4, getHeight()-4, RADIUS, RADIUS);
                // card
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-3, getHeight()-3, RADIUS, RADIUS);
                g2.dispose();
            }
        };
        p.setBackground(CARD_BG);
        p.setOpaque(false);
        return p;
    }

    // ── TextField ─────────────────────────────────────────────────────────────

    public static JTextField styledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBackground(Color.WHITE);
        tf.setForeground(TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, BTN_H));
        return tf;
    }

    public static JPasswordField styledPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_BODY);
        pf.setBackground(Color.WHITE);
        pf.setForeground(TEXT_PRIMARY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        pf.setPreferredSize(new Dimension(pf.getPreferredSize().width, BTN_H));
        return pf;
    }

    // ── Label helpers ─────────────────────────────────────────────────────────

    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(Color.WHITE);
        return l;
    }

    public static JLabel headingLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel bodyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    // ── Divider ───────────────────────────────────────────────────────────────

    public static JSeparator divider() {
        JSeparator s = new JSeparator();
        s.setForeground(DIVIDER);
        s.setBackground(DIVIDER);
        return s;
    }

    // ── Global Swing defaults ─────────────────────────────────────────────────

    public static void applyGlobalDefaults() {
        UIManager.put("OptionPane.background",           CREAM);
        UIManager.put("Panel.background",                CREAM);
        UIManager.put("OptionPane.messageForeground",    TEXT_PRIMARY);
        UIManager.put("Button.focus",                    new Color(0, 0, 0, 0));
        UIManager.put("ScrollBar.thumb",                 LATTE);
        UIManager.put("ScrollBar.track",                 STEAM);
        UIManager.put("ScrollBar.width",                 8);
        UIManager.put("Table.selectionBackground",       GOLD);
        UIManager.put("Table.selectionForeground",       TEXT_PRIMARY);
        UIManager.put("Table.gridColor",                 DIVIDER);
        UIManager.put("TableHeader.background",          ROAST);
        UIManager.put("TableHeader.foreground",          Color.WHITE);
        UIManager.put("TextField.caretForeground",       TERRACOTTA);
        UIManager.put("ComboBox.background",             Color.WHITE);
        UIManager.put("ComboBox.border",
            BorderFactory.createLineBorder(DIVIDER, 1));
    }
}
