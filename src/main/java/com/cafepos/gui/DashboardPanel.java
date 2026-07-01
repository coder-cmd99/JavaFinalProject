package com.cafepos.gui;

import com.cafepos.dao.ProductDAO;
import com.cafepos.model.Category;
import com.cafepos.model.Product;
import com.cafepos.util.CurrencyUtil;
import com.cafepos.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dashboard panel – displays product catalog as a card grid with
 * category filter tabs and a search bar.
 *
 * When a product card is clicked, it fires the onAddToCart callback so that
 * OrderPanel can receive the selection.
 */
public class DashboardPanel extends JPanel {

    private final ProductDAO          productDAO   = new ProductDAO();
    private       Consumer<Product>   onAddToCart;

    private JPanel        gridPanel;
    private JScrollPane   scrollPane;
    private JPanel        categoryBar;
    private JTextField    searchField;
    private int           activeCategoryId = 0;   // 0 = "All"

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.MAIN_BG);
        buildUI();
        loadData();
    }

    public void setOnAddToCart(Consumer<Product> callback) {
        this.onAddToCart = callback;
    }

    // ── Build UI structure ────────────────────────────────────────────────────

    private void buildUI() {
        // Top bar: search
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(UITheme.MAIN_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        JLabel menuLabel = UITheme.headingLabel("Menu");
        menuLabel.setFont(UITheme.FONT_TITLE);
        menuLabel.setForeground(UITheme.ESPRESSO);

        searchField = UITheme.styledTextField(20);
        searchField.setMaximumSize(new Dimension(260, UITheme.BTN_H));
        searchField.setPreferredSize(new Dimension(260, UITheme.BTN_H));
        searchField.putClientProperty("JTextField.placeholderText", "Search menu…");

        JButton searchBtn = UITheme.secondaryButton("🔍");
        searchBtn.setPreferredSize(new Dimension(UITheme.BTN_H, UITheme.BTN_H));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchRow.setBackground(UITheme.MAIN_BG);
        searchRow.add(searchField);
        searchRow.add(searchBtn);

        topBar.add(menuLabel, BorderLayout.WEST);
        topBar.add(searchRow, BorderLayout.EAST);

        // Category tab bar
        categoryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        categoryBar.setBackground(UITheme.MAIN_BG);
        categoryBar.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        // Product grid (will be populated later)
        gridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 12, 12));
        gridPanel.setBackground(UITheme.MAIN_BG);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        scrollPane = new JScrollPane(gridPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(UITheme.MAIN_BG);
        scrollPane.getViewport().setBackground(UITheme.MAIN_BG);

        add(topBar,      BorderLayout.NORTH);
        add(categoryBar, BorderLayout.LINE_START); // temp; replaced below

        // Stack category bar + grid
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.MAIN_BG);
        center.add(categoryBar, BorderLayout.NORTH);
        center.add(scrollPane,  BorderLayout.CENTER);

        remove(categoryBar);
        add(center, BorderLayout.CENTER);

        // Wire search
        ActionListener doSearch = e -> {
            String kw = searchField.getText().trim();
            if (kw.isEmpty()) {
                loadProductsForCategory(activeCategoryId);
            } else {
                showProducts(productDAO.searchProducts(kw));
            }
        };
        searchBtn.addActionListener(doSearch);
        searchField.addActionListener(doSearch);
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<Category> cats;
            private List<Product>  prods;

            @Override protected Void doInBackground() {
                cats  = productDAO.getAllCategories();
                prods = productDAO.getAllProducts();
                return null;
            }

            @Override protected void done() {
                buildCategoryTabs(cats);
                showProducts(prods);
            }
        };
        worker.execute();
    }

    private void loadProductsForCategory(int categoryId) {
        SwingWorker<List<Product>, Void> worker = new SwingWorker<>() {
            @Override protected List<Product> doInBackground() {
                return productDAO.getProductsByCategory(categoryId);
            }
            @Override protected void done() {
                try { showProducts(get()); } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    // ── Category tabs ─────────────────────────────────────────────────────────

    private void buildCategoryTabs(List<Category> categories) {
        categoryBar.removeAll();

        // "All" tab
        addCategoryTab("All", "🍽", 0);
        for (Category c : categories) {
            addCategoryTab(c.getName(), c.getIconEmoji(), c.getId());
        }
        categoryBar.revalidate();
        categoryBar.repaint();
    }

    private void addCategoryTab(String name, String emoji, int id) {
        JToggleButton btn = new JToggleButton(emoji + " " + name) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isSelected() ? UITheme.TERRACOTTA : UITheme.STEAM;
                Color fg = isSelected() ? Color.WHITE        : UITheme.ROAST;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS, UITheme.RADIUS);
                g2.dispose();
                setForeground(fg);
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_SUBHEAD);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (id == 0) btn.setSelected(true);

        btn.addActionListener(e -> {
            // Deselect all other tabs
            for (Component c : categoryBar.getComponents()) {
                if (c instanceof JToggleButton tb && tb != btn) tb.setSelected(false);
            }
            btn.setSelected(true);
            activeCategoryId = id;
            searchField.setText("");
            loadProductsForCategory(id);
        });

        categoryBar.add(btn);
    }

    // ── Product grid ──────────────────────────────────────────────────────────

    private void showProducts(List<Product> products) {
        gridPanel.removeAll();

        if (products.isEmpty()) {
            JLabel empty = new JLabel("No products found");
            empty.setFont(UITheme.FONT_HEADING);
            empty.setForeground(UITheme.LATTE);
            gridPanel.add(empty);
        } else {
            for (Product p : products) {
                gridPanel.add(buildProductCard(p));
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildProductCard(Product product) {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BorderLayout(0, 0));
        card.setPreferredSize(new Dimension(UITheme.CARD_W, UITheme.CARD_H));
        card.setCursor(product.isAvailable()
            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            : Cursor.getDefaultCursor());

        if (!product.isAvailable()) {
            card.setBackground(UITheme.UNAVAILABLE_BG);
        }

        // Emoji area
        JLabel emojiLbl = new JLabel(product.getImageEmoji(), SwingConstants.CENTER);
        emojiLbl.setFont(UITheme.FONT_EMOJI);
        emojiLbl.setPreferredSize(new Dimension(UITheme.CARD_W, 60));
        emojiLbl.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));

        // Info area
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JLabel nameLbl = new JLabel(
            "<html><body style='width:145px'>" + product.getName() + "</body></html>");
        nameLbl.setFont(UITheme.FONT_SUBHEAD);
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel descLbl = new JLabel(
            "<html><body style='width:145px'>" + product.getDescription() + "</body></html>");
        descLbl.setFont(UITheme.FONT_SMALL);
        descLbl.setForeground(UITheme.TEXT_SECONDARY);

        JLabel priceLbl = new JLabel(CurrencyUtil.format(product.getPrice()));
        priceLbl.setFont(UITheme.FONT_PRICE);
        priceLbl.setForeground(UITheme.TERRACOTTA);

        info.add(nameLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(descLbl);
        info.add(Box.createVerticalGlue());
        info.add(priceLbl);

        // Unavailable badge
        if (!product.isAvailable()) {
            JLabel badge = new JLabel("  Unavailable  ");
            badge.setFont(UITheme.FONT_SMALL);
            badge.setOpaque(true);
            badge.setBackground(UITheme.LATTE);
            badge.setForeground(Color.WHITE);
            badge.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            info.add(Box.createVerticalStrut(4));
            info.add(badge);
        }

        card.add(emojiLbl, BorderLayout.NORTH);
        card.add(info,     BorderLayout.CENTER);

        // Click to add
        if (product.isAvailable()) {
            MouseAdapter click = new MouseAdapter() {
                Color originalBg = card.getBackground();
                @Override public void mouseEntered(MouseEvent e) {
                    card.setBackground(UITheme.STEAM);
                    card.repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    card.setBackground(originalBg);
                    card.repaint();
                }
                @Override public void mouseClicked(MouseEvent e) {
                    if (onAddToCart != null) onAddToCart.accept(product);
                    // Flash feedback
                    card.setBackground(UITheme.GOLD);
                    card.repaint();
                    Timer t = new Timer(200, ev -> {
                        card.setBackground(originalBg);
                        card.repaint();
                    });
                    t.setRepeats(false);
                    t.start();
                }
            };
            card.addMouseListener(click);
            for (Component c : card.getComponents()) {
                if (c instanceof Container cont) addMouseListenerToAll(cont, click);
            }
        }

        return card;
    }

    private void addMouseListenerToAll(Container container, MouseListener ml) {
        container.addMouseListener(ml);
        for (Component c : container.getComponents()) {
            c.addMouseListener(ml);
            if (c instanceof Container cont) addMouseListenerToAll(cont, ml);
        }
    }

    /** Refreshes product data from the database. */
    public void refresh() {
        loadData();
    }
}
