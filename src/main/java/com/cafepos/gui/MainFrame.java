package com.cafepos.gui;

import com.cafepos.config.ConnectionPool;
import com.cafepos.util.AppContext;
import com.cafepos.util.UITheme;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;

/**
 * Root application window. Controls navigation between Login,
 * Dashboard/Order, and Report screens via a CardLayout.
 */
public class MainFrame extends JFrame {

    // Card names
    private static final String CARD_LOGIN     = "login";
    private static final String CARD_MAIN      = "main";

    // Layout
    private final CardLayout  rootLayout = new CardLayout();
    private final JPanel      rootPanel  = new JPanel(rootLayout);

    // Panels (created lazily)
    private LoginPanel     loginPanel;
    private DashboardPanel dashboardPanel;
    private OrderPanel     orderPanel;
    private ReportPanel    reportPanel;

    // Sidebar buttons
    private JToggleButton  btnMenu;
    private JToggleButton  btnOrder;
    private JToggleButton  btnReport;
    private ButtonGroup    navGroup;

    // Main content area
    private final CardLayout mainCardLayout = new CardLayout();
    private final JPanel     mainContent    = new JPanel(mainCardLayout);

    // Status bar
    private JLabel statusLabel;

    public MainFrame() {
        super("Cafe E4. — POS System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setPreferredSize(new Dimension(1280, 800));

        UITheme.applyGlobalDefaults();

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { handleExit(); }
        });

        buildFrame();
        pack();
        setLocationRelativeTo(null);
    }

    // ── Frame structure ───────────────────────────────────────────────────────

    private void buildFrame() {
        setContentPane(rootPanel);

        // Login card
        loginPanel = new LoginPanel(this);
        rootPanel.add(loginPanel, CARD_LOGIN);

        // Main card (sidebar + content)
        JPanel mainCard = buildMainCard();
        rootPanel.add(mainCard, CARD_MAIN);

        rootLayout.show(rootPanel, CARD_LOGIN);
    }

    private JPanel buildMainCard() {
        JPanel mainCard = new JPanel(new BorderLayout(0, 0));

        // Sidebar
        JPanel sidebar = buildSidebar();

        // Content area
        mainContent.setBackground(UITheme.MAIN_BG);

        // Initialise sub-panels
        dashboardPanel = new DashboardPanel();
        orderPanel     = new OrderPanel();
        reportPanel    = new ReportPanel();

        // Wire dashboard → order
        dashboardPanel.setOnAddToCart(product -> {
            orderPanel.addProduct(product);
            setStatus("Added: " + product.getName());
        });
        // Wire order → refresh report
        orderPanel.setOnOrderSaved(() -> {
            reportPanel.refresh();
            setStatus("Order saved successfully.");
        });

        mainContent.add(dashboardPanel, "menu");
        mainContent.add(orderPanel,     "order");
        mainContent.add(reportPanel,    "report");
        mainCardLayout.show(mainContent, "menu");

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(UITheme.ESPRESSO);
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.LATTE);
        statusBar.add(statusLabel, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("E4 Cafe. POS v1.0");
        versionLabel.setFont(UITheme.FONT_SMALL);
        versionLabel.setForeground(UITheme.MOCHA);
        statusBar.add(versionLabel, BorderLayout.EAST);

        mainCard.add(sidebar,      BorderLayout.WEST);
        mainCard.add(mainContent,  BorderLayout.CENTER);
        mainCard.add(statusBar,    BorderLayout.SOUTH);
        return mainCard;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(200, 0));

        // Logo area
        JPanel logoArea = new JPanel(new BorderLayout());
        logoArea.setBackground(UITheme.ESPRESSO);
        logoArea.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        JLabel cup = new JLabel("☕", SwingConstants.CENTER);
        cup.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        JLabel brandName = new JLabel("Cafe E4", SwingConstants.CENTER);
        brandName.setFont(new Font("SansSerif", Font.BOLD, 16));
        brandName.setForeground(UITheme.GOLD);
        JLabel posLabel = new JLabel("Point of Sale", SwingConstants.CENTER);
        posLabel.setFont(UITheme.FONT_SMALL);
        posLabel.setForeground(UITheme.LATTE);

        JPanel logoInner = new JPanel();
        logoInner.setLayout(new BoxLayout(logoInner, BoxLayout.Y_AXIS));
        logoInner.setBackground(UITheme.ESPRESSO);
        cup.setAlignmentX(CENTER_ALIGNMENT);
        brandName.setAlignmentX(CENTER_ALIGNMENT);
        posLabel.setAlignmentX(CENTER_ALIGNMENT);
        logoInner.add(cup);
        logoInner.add(brandName);
        logoInner.add(posLabel);
        logoArea.add(logoInner, BorderLayout.CENTER);

        // Nav buttons
        navGroup = new ButtonGroup();
        btnMenu   = navButton("🍽  Menu",    "menu");
        btnOrder  = navButton("🛒  Order",   "order");
        btnReport = navButton("📊  Reports", "report");
        navGroup.add(btnMenu);
        navGroup.add(btnOrder);
        navGroup.add(btnReport);
        btnMenu.setSelected(true);

        // Cashier info
        JPanel cashierInfo = new JPanel();
        cashierInfo.setLayout(new BoxLayout(cashierInfo, BoxLayout.Y_AXIS));
        cashierInfo.setBackground(new Color(0x2C1B16));
        cashierInfo.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel cashierName = new JLabel();
        cashierName.setName("cashierName");
        cashierName.setFont(UITheme.FONT_SUBHEAD);
        cashierName.setForeground(Color.WHITE);
        cashierName.setAlignmentX(LEFT_ALIGNMENT);

        JLabel cashierRole = new JLabel();
        cashierRole.setName("cashierRole");
        cashierRole.setFont(UITheme.FONT_SMALL);
        cashierRole.setForeground(UITheme.LATTE);
        cashierRole.setAlignmentX(LEFT_ALIGNMENT);

        cashierInfo.add(cashierName);
        cashierInfo.add(Box.createVerticalStrut(2));
        cashierInfo.add(cashierRole);

        // Logout
        JButton logoutBtn = new JButton("⎋  Logout");
        logoutBtn.setFont(UITheme.FONT_SUBHEAD);
        logoutBtn.setForeground(UITheme.LATTE);
        logoutBtn.setBackground(new Color(0x2C1B16));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        logoutBtn.addActionListener(e -> handleLogout());
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                logoutBtn.setForeground(UITheme.ERROR_RED);
            }
            @Override public void mouseExited(MouseEvent e) {
                logoutBtn.setForeground(UITheme.LATTE);
            }
        });

        // Assemble sidebar
        sidebar.add(logoArea);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(btnMenu);
        sidebar.add(btnOrder);
        sidebar.add(btnReport);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(cashierInfo);
        sidebar.add(logoutBtn);

        // Update cashier info on login (deferred to showDashboard)
        sidebar.putClientProperty("cashierName", cashierName);
        sidebar.putClientProperty("cashierRole", cashierRole);
        sidebar.putClientProperty("sidebar", sidebar);

        return sidebar;
    }

    private JToggleButton navButton(String text, String card) {
        JToggleButton btn = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (isSelected()) {
                    g2.setColor(UITheme.TERRACOTTA);
                    g2.fillRect(0, 0, 4, getHeight());
                    g2.setColor(new Color(0xFF, 0xFF, 0xFF, 20));
                    g2.fillRect(4, 0, getWidth()-4, getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_SUBHEAD);
        btn.setForeground(UITheme.LATTE);
        btn.setBackground(UITheme.SIDEBAR_BG);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addChangeListener(e -> {
            btn.setForeground(btn.isSelected() ? Color.WHITE : UITheme.LATTE);
        });
        btn.addActionListener(e -> {
            mainCardLayout.show(mainContent, card);
            if ("report".equals(card)) reportPanel.refresh();
        });
        return btn;
    }

    // ── Navigation helpers ────────────────────────────────────────────────────

    public void showDashboard() {
        // Update cashier info labels
        if (AppContext.isLoggedIn()) {
            Component sidebar = rootPanel.getComponent(1);
            updateCashierLabels(sidebar);
        }
        rootLayout.show(rootPanel, CARD_MAIN);
        mainCardLayout.show(mainContent, "menu");
        btnMenu.setSelected(true);
        dashboardPanel.refresh();
    }

    private void updateCashierLabels(Component root) {
        if (root instanceof JPanel p) {
            JLabel nameLabel = (JLabel) p.getClientProperty("cashierName");
            JLabel roleLabel = (JLabel) p.getClientProperty("cashierRole");
            if (nameLabel != null && AppContext.getCurrentUser() != null) {
                nameLabel.setText(AppContext.getCurrentUser().getFullName());
                roleLabel.setText(AppContext.getCurrentUser().getRole().toUpperCase());
            }
        }
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out?",
            "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            AppContext.clearSession();
            rootLayout.show(rootPanel, CARD_LOGIN);
        }
    }

    // ── Exit ──────────────────────────────────────────────────────────────────

    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Exit Brew & Co. POS?",
            "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try { ConnectionPool.getInstance().shutdown(); }
            catch (SQLException ignored) {}
            System.exit(0);
        }
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            // Fade after 5 seconds
            Timer t = new Timer(5000, e -> statusLabel.setText("Ready"));
            t.setRepeats(false);
            t.start();
        }
    }
}
