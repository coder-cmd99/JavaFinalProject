package com.cafepos.gui;

import com.cafepos.config.DatabaseConfig;
import com.cafepos.dao.OrderDAO;
import com.cafepos.model.CartItem;
import com.cafepos.model.Order;
import com.cafepos.model.OrderItem;
import com.cafepos.model.Product;
import com.cafepos.util.AppContext;
import com.cafepos.util.CurrencyUtil;
import com.cafepos.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Order panel: displays the active cart, calculates totals,
 * and handles checkout / payment saving.
 */
public class OrderPanel extends JPanel {

    private final OrderDAO       orderDAO = new OrderDAO();
    private final List<CartItem> cart     = new ArrayList<>();

    // Table
    private DefaultTableModel tableModel;
    private JTable            cartTable;

    // Summary labels
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel discountLabel;
    private JLabel totalLabel;

    // Controls
    private JComboBox<String> paymentCombo;
    private JSpinner          discountSpinner;
    private JButton           checkoutBtn;
    private JButton           clearBtn;

    private Runnable onOrderSaved;   // callback to refresh report panel

    public OrderPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.MAIN_BG);
        buildUI();
    }

    public void setOnOrderSaved(Runnable callback) {
        this.onOrderSaved = callback;
    }

    /** Called by DashboardPanel when user clicks a product card. */
    public void addProduct(Product product) {
        // Check if already in cart
        for (CartItem item : cart) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                refreshTable();
                return;
            }
        }
        cart.add(new CartItem(product, 1));
        refreshTable();
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private void buildUI() {
        // ── Header ───────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = UITheme.titleLabel("🛒  Current Order");
        clearBtn = UITheme.dangerButton("Clear");
        clearBtn.addActionListener(e -> clearCart());

        header.add(title,    BorderLayout.WEST);
        header.add(clearBtn, BorderLayout.EAST);

        // ── Cart table ────────────────────────────────────────────────────────
        String[] cols = {"Item", "Price", "Qty", "Total", ""};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == 2;   // only Qty is editable
            }
            @Override public Class<?> getColumnClass(int col) {
                return col == 2 ? Integer.class : String.class;
            }
        };

        cartTable = new JTable(tableModel);
        cartTable.setFont(UITheme.FONT_BODY);
        cartTable.setRowHeight(40);
        cartTable.setShowGrid(true);
        cartTable.setGridColor(UITheme.DIVIDER);
        cartTable.setBackground(Color.WHITE);
        cartTable.setSelectionBackground(new Color(0xFFF3E0));
        cartTable.setSelectionForeground(UITheme.TEXT_PRIMARY);
        cartTable.setFillsViewportHeight(true);
        cartTable.getTableHeader().setFont(UITheme.FONT_SUBHEAD);
        cartTable.getTableHeader().setBackground(UITheme.ROAST);
        cartTable.getTableHeader().setForeground(Color.WHITE);
        cartTable.getTableHeader().setReorderingAllowed(false);

        // Column widths
        int[] colWidths = {200, 80, 60, 80, 40};
        for (int i = 0; i < colWidths.length; i++) {
            cartTable.getColumnModel().getColumn(i)
                .setPreferredWidth(colWidths[i]);
        }

        // Delete button renderer/editor in last column
        cartTable.getColumnModel().getColumn(4).setCellRenderer(new DeleteButtonRenderer());
        cartTable.getColumnModel().getColumn(4).setCellEditor(new DeleteButtonEditor(cartTable));

        // Listen for qty changes
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 2 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (row >= 0 && row < cart.size()) {
                    Object val = tableModel.getValueAt(row, 2);
                    int qty = val instanceof Integer ? (Integer) val : 1;
                    if (qty <= 0) {
                        removeCartItem(row);
                    } else {
                        cart.get(row).setQuantity(qty);
                        updateTotals();
                    }
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(cartTable);
        tableScroll.setBorder(null);
        tableScroll.getViewport().setBackground(Color.WHITE);

        // ── Summary panel ─────────────────────────────────────────────────────
        JPanel summaryPanel = buildSummaryPanel();

        // ── Assemble center ───────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.MAIN_BG);
        center.add(tableScroll,  BorderLayout.CENTER);
        center.add(summaryPanel, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.DIVIDER),
            BorderFactory.createEmptyBorder(16, 20, 20, 20)));

        // Discount row
        JPanel discRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        discRow.setBackground(Color.WHITE);
        JLabel discLbl = UITheme.bodyLabel("Discount ($):");
        discountSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999.0, 0.5));
        discountSpinner.setFont(UITheme.FONT_BODY);
        discountSpinner.setPreferredSize(new Dimension(90, 30));
        discountSpinner.addChangeListener(e -> updateTotals());
        discRow.add(discLbl);
        discRow.add(discountSpinner);

        // Totals
        subtotalLabel = makeSummaryLabel("$0.00");
        taxLabel      = makeSummaryLabel("$0.00");
        discountLabel = makeSummaryLabel("$0.00");
        totalLabel    = new JLabel("$0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        totalLabel.setForeground(UITheme.TERRACOTTA);

        JPanel totalsGrid = new JPanel(new GridLayout(4, 2, 0, 4));
        totalsGrid.setBackground(Color.WHITE);
        totalsGrid.add(UITheme.bodyLabel("Subtotal:"));  totalsGrid.add(subtotalLabel);
        totalsGrid.add(UITheme.bodyLabel("Tax (8%):"));  totalsGrid.add(taxLabel);
        totalsGrid.add(UITheme.bodyLabel("Discount:"));  totalsGrid.add(discountLabel);
        totalsGrid.add(UITheme.headingLabel("TOTAL:"));  totalsGrid.add(totalLabel);

        // Payment method
        JPanel payRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        payRow.setBackground(Color.WHITE);
        payRow.add(UITheme.bodyLabel("Payment:"));
        paymentCombo = new JComboBox<>(new String[]{"cash", "card", "e-wallet"});
        paymentCombo.setFont(UITheme.FONT_BODY);
        paymentCombo.setBackground(Color.WHITE);
        payRow.add(paymentCombo);

        // Checkout button
        checkoutBtn = UITheme.primaryButton("✅  Complete Order");
        checkoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        checkoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkoutBtn.addActionListener(e -> checkout());

        panel.add(discRow);
        panel.add(Box.createVerticalStrut(12));
        panel.add(totalsGrid);
        panel.add(Box.createVerticalStrut(12));
        panel.add(UITheme.divider());
        panel.add(Box.createVerticalStrut(12));
        panel.add(payRow);
        panel.add(Box.createVerticalStrut(16));
        panel.add(checkoutBtn);

        return panel;
    }

    private JLabel makeSummaryLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    // ── Cart logic ─────────────────────────────────────────────────────────────

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (CartItem item : cart) {
            tableModel.addRow(new Object[]{
                item.getProduct().getImageEmoji() + " " + item.getProduct().getName(),
                CurrencyUtil.format(item.getProduct().getPrice()),
                item.getQuantity(),
                CurrencyUtil.format(item.getLineTotal()),
                "🗑"
            });
        }
        updateTotals();
    }

    private void updateTotals() {
        double subtotal = cart.stream().mapToDouble(CartItem::getLineTotal).sum();
        double discount = (double) discountSpinner.getValue();
        double taxBase  = Math.max(0, subtotal - discount);
        double tax      = taxBase * DatabaseConfig.TAX_RATE;
        double total    = taxBase + tax;

        subtotalLabel.setText(CurrencyUtil.format(subtotal));
        taxLabel.setText(CurrencyUtil.format(tax));
        discountLabel.setText("-" + CurrencyUtil.format(discount));
        totalLabel.setText(CurrencyUtil.format(total));
    }

    private void clearCart() {
        if (cart.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Clear all items from the current order?",
            "Clear Order", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            cart.clear();
            discountSpinner.setValue(0.0);
            refreshTable();
        }
    }

    void removeCartItem(int row) {
        if (row >= 0 && row < cart.size()) {
            cart.remove(row);
            refreshTable();
        }
    }

    // ── Checkout ───────────────────────────────────────────────────────────────

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Your cart is empty. Add items from the Menu tab first.",
                "Empty Cart", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double subtotal = cart.stream().mapToDouble(CartItem::getLineTotal).sum();
        double discount = (double) discountSpinner.getValue();
        double taxBase  = Math.max(0, subtotal - discount);
        double tax      = taxBase * DatabaseConfig.TAX_RATE;
        double total    = taxBase + tax;
        String payment  = (String) paymentCombo.getSelectedItem();

        // Confirmation dialog
        String msg = String.format(
            "<html><b>Order Summary</b><br><br>" +
            "%d item(s)<br>" +
            "Subtotal: %s<br>Discount: -%s<br>Tax: %s<br>" +
            "<font color='#BF5E2E'><b>Total: %s</b></font><br><br>" +
            "Payment: <b>%s</b><br><br>Confirm and save?</html>",
            cart.stream().mapToInt(CartItem::getQuantity).sum(),
            CurrencyUtil.format(subtotal),
            CurrencyUtil.format(discount),
            CurrencyUtil.format(tax),
            CurrencyUtil.format(total),
            payment.toUpperCase());

        int confirm = JOptionPane.showConfirmDialog(this, msg,
            "Confirm Order", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Build Order object
        Order order = new Order();
        order.setUserId(AppContext.getCurrentUser().getId());
        order.setSubtotal(subtotal);
        order.setTaxAmount(tax);
        order.setDiscountAmount(discount);
        order.setTotalAmount(total);
        order.setPaymentMethod(payment);

        List<OrderItem> items = new ArrayList<>();
        for (CartItem ci : cart) {
            items.add(new OrderItem(
                ci.getProduct().getId(),
                ci.getProduct().getName(),
                ci.getProduct().getPrice(),
                ci.getQuantity()));
        }
        order.setItems(items);

        // Save in background
        checkoutBtn.setEnabled(false);
        checkoutBtn.setText("Saving…");

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override protected Integer doInBackground() {
                return orderDAO.saveOrder(order);
            }
            @Override protected void done() {
                checkoutBtn.setEnabled(true);
                checkoutBtn.setText("✅  Complete Order");
                try {
                    int orderId = get();
                    if (orderId > 0) {
                        showOrderSuccess(orderId, total);
                        cart.clear();
                        discountSpinner.setValue(0.0);
                        refreshTable();
                        if (onOrderSaved != null) onOrderSaved.run();
                    } else {
                        JOptionPane.showMessageDialog(OrderPanel.this,
                            "Failed to save order. Check DB connection.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(OrderPanel.this,
                        "Unexpected error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showOrderSuccess(int orderId, double total) {
        JPanel msg = new JPanel();
        msg.setLayout(new BoxLayout(msg, BoxLayout.Y_AXIS));
        msg.setBackground(UITheme.CREAM);

        JLabel checkmark = new JLabel("✅", SwingConstants.CENTER);
        checkmark.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        checkmark.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heading = new JLabel("Order Saved!", SwingConstants.CENTER);
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.SUCCESS_GREEN);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel detail = new JLabel(
            "<html><center>Order #" + orderId + "<br>Total: " +
            CurrencyUtil.format(total) + "</center></html>",
            SwingConstants.CENTER);
        detail.setFont(UITheme.FONT_BODY);
        detail.setForeground(UITheme.TEXT_SECONDARY);
        detail.setAlignmentX(Component.CENTER_ALIGNMENT);

        msg.add(checkmark);
        msg.add(Box.createVerticalStrut(8));
        msg.add(heading);
        msg.add(Box.createVerticalStrut(4));
        msg.add(detail);

        JOptionPane.showMessageDialog(this, msg, "Order Complete",
            JOptionPane.PLAIN_MESSAGE);
    }

    // ── Delete button renderer & editor ──────────────────────────────────────

    private static class DeleteButtonRenderer extends JButton implements TableCellRenderer {
        DeleteButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            setBorder(BorderFactory.createEmptyBorder());
            setBackground(Color.WHITE);
            setForeground(UITheme.ERROR_RED);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            setText("🗑");
            return this;
        }
    }

    private class DeleteButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn;
        private int editingRow;

        DeleteButtonEditor(JTable table) {
            btn = new JButton("🗑");
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setBackground(Color.WHITE);
            btn.setForeground(UITheme.ERROR_RED);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                fireEditingStopped();
                removeCartItem(editingRow);
            });
        }

        @Override public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int col) {
            editingRow = row;
            return btn;
        }

        @Override public Object getCellEditorValue() { return "🗑"; }
    }
}
