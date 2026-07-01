package com.cafepos.gui;

import com.cafepos.dao.UserDAO;
import com.cafepos.model.User;
import com.cafepos.util.AppContext;
import com.cafepos.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Full-screen login form with café branding.
 * Validates credentials against the database, then hands off to MainFrame.
 */
public class LoginPanel extends JPanel {

    private final JTextField     usernameField;
    private final JPasswordField passwordField;
    private final JButton        loginButton;
    private final JLabel         errorLabel;
    private final MainFrame      mainFrame;
    private final UserDAO        userDAO = new UserDAO();

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(UITheme.ESPRESSO);

        // ── Left branding panel ──────────────────────────────────────────────
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(UITheme.ESPRESSO);
        brand.setPreferredSize(new Dimension(420, 0));

        JPanel brandInner = new JPanel();
        brandInner.setLayout(new BoxLayout(brandInner, BoxLayout.Y_AXIS));
        brandInner.setBackground(UITheme.ESPRESSO);
        brandInner.setBorder(BorderFactory.createEmptyBorder(0, 48, 0, 48));

        JLabel cupEmoji = new JLabel("☕");
        cupEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        cupEmoji.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("BREW & CO.");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(UITheme.GOLD);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taglineLabel = new JLabel("Café Point of Sale System");
        taglineLabel.setFont(UITheme.FONT_BODY);
        taglineLabel.setForeground(UITheme.LATTE);
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.MOCHA);
        sep.setMaximumSize(new Dimension(200, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel mottoLabel = new JLabel("<html><center>Every cup crafted<br>with care</center></html>");
        mottoLabel.setFont(UITheme.FONT_SMALL);
        mottoLabel.setForeground(new Color(0xAAAAAA));
        mottoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mottoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandInner.add(Box.createVerticalGlue());
        brandInner.add(cupEmoji);
        brandInner.add(Box.createVerticalStrut(16));
        brandInner.add(titleLabel);
        brandInner.add(Box.createVerticalStrut(6));
        brandInner.add(taglineLabel);
        brandInner.add(Box.createVerticalStrut(24));
        brandInner.add(sep);
        brandInner.add(Box.createVerticalStrut(24));
        brandInner.add(mottoLabel);
        brandInner.add(Box.createVerticalGlue());

        brand.add(brandInner);

        // ── Right login card ─────────────────────────────────────────────────
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UITheme.CREAM);

        JPanel card = buildLoginCard();
        rightPanel.add(card);

        add(brand,       BorderLayout.WEST);
        add(rightPanel,  BorderLayout.CENTER);

        // Placeholders / dummy references for final fields – assigned below
        usernameField = (JTextField)     findComponent(card, JTextField.class);
        passwordField = (JPasswordField) findComponent(card, JPasswordField.class);
        loginButton   = (JButton)        findComponent(card, JButton.class);
        errorLabel    = (JLabel)         findByName(card, "errorLabel");
    }

    // ── Build login card ─────────────────────────────────────────────────────

    private JPanel buildLoginCard() {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 48));
        card.setPreferredSize(new Dimension(380, 440));

        // Heading
        JLabel heading = new JLabel("Welcome back");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.ESPRESSO);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subheading = new JLabel("Sign in to continue");
        subheading.setFont(UITheme.FONT_BODY);
        subheading.setForeground(UITheme.TEXT_SECONDARY);
        subheading.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(UITheme.FONT_SUBHEAD);
        userLabel.setForeground(UITheme.ESPRESSO);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField tfUser = UITheme.styledTextField(20);
        tfUser.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.BTN_H));
        tfUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UITheme.FONT_SUBHEAD);
        passLabel.setForeground(UITheme.ESPRESSO);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField pfPass = UITheme.styledPasswordField(20);
        pfPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.BTN_H));
        pfPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label (hidden until needed)
        JLabel errLabel = new JLabel(" ");
        errLabel.setName("errorLabel");
        errLabel.setFont(UITheme.FONT_SMALL);
        errLabel.setForeground(UITheme.ERROR_RED);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        JButton btnLogin = UITheme.primaryButton("Sign In");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.BTN_H));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Demo hint
        JLabel hint = new JLabel("Demo: admin / admin123 or cashier / cashier123");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.LATTE);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assemble
        card.add(heading);
        card.add(Box.createVerticalStrut(4));
        card.add(subheading);
        card.add(Box.createVerticalStrut(28));
        card.add(userLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(tfUser);
        card.add(Box.createVerticalStrut(16));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(pfPass);
        card.add(Box.createVerticalStrut(8));
        card.add(errLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(20));
        card.add(hint);

        // ── Event wiring ──────────────────────────────────────────────────
        ActionListener doLogin = e -> attemptLogin(tfUser, pfPass, errLabel);
        btnLogin.addActionListener(doLogin);
        pfPass.addActionListener(doLogin);
        tfUser.addActionListener(e -> pfPass.requestFocusInWindow());

        return card;
    }

    // ── Authentication logic ──────────────────────────────────────────────────

    private void attemptLogin(JTextField tf, JPasswordField pf, JLabel errLabel) {
        String username = tf.getText().trim();
        String password = new String(pf.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errLabel.setText("Please enter username and password.");
            return;
        }

        // Disable button to prevent double-clicks; show progress
        loginButton.setEnabled(false);
        loginButton.setText("Signing in…");
        errLabel.setText(" ");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override protected User doInBackground() {
                return userDAO.authenticate(username, password);
            }
            @Override protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        AppContext.setCurrentUser(user);
                        mainFrame.showDashboard();
                    } else {
                        errLabel.setText("Invalid username or password.");
                        pf.setText("");
                        pf.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    errLabel.setText("Connection error – check DB settings.");
                }
                loginButton.setEnabled(true);
                loginButton.setText("Sign In");
            }
        };
        worker.execute();
    }

    // ── Utility: find first component of type in container tree ──────────────

    private Component findComponent(Container root, Class<?> type) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) return c;
            if (c instanceof Container) {
                Component found = findComponent((Container) c, type);
                if (found != null) return found;
            }
        }
        return null;
    }

    private Component findByName(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName())) return c;
            if (c instanceof Container) {
                Component found = findByName((Container) c, name);
                if (found != null) return found;
            }
        }
        return null;
    }
}
