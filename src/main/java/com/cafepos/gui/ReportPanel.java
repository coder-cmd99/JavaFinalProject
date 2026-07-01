package com.cafepos.gui;

import com.cafepos.dao.OrderDAO;
import com.cafepos.model.Order;
import com.cafepos.model.OrderItem;
import com.cafepos.util.CurrencyUtil;
import com.cafepos.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Report panel: shows order history, filterable by date range.
 * Includes aggregate statistics and a detail view for each order.
 */
public class ReportPanel extends JPanel {

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OrderDAO orderDAO = new OrderDAO();

    // Filter controls
    private JTextField fromDateField;
    private JTextField toDateField;

    // Stats labels
    private JLabel statTransactions;
    private JLabel statRevenue;
    private JLabel statItems;

    // Master table
    private DefaultTableModel masterModel;
    private JTable            masterTable;

    // Detail table
    private DefaultTableModel detailModel;
    private JLabel            detailHeading;

    private List<Order> currentOrders;

    public ReportPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.MAIN_BG);
        buildUI();
        refresh();
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private void buildUI() {
        // ── Header bar ────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        header.add(UITheme.titleLabel("📊  Sales Report"), BorderLayout.WEST);

        JButton refreshBtn = UITheme.ghostButton("↻ Refresh");
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> refresh());
        header.add(refreshBtn, BorderLayout.EAST);

        // ── Filter row ────────────────────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterBar.setBackground(UITheme.STEAM);
        filterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.DIVIDER));

        fromDateField = UITheme.styledTextField(10);
        fromDateField.setPreferredSize(new Dimension(120, UITheme.BTN_H));
        fromDateField.setText(LocalDate.now().withDayOfMonth(1).format(DATE_FMT));

        toDateField = UITheme.styledTextField(10);
        toDateField.setPreferredSize(new Dimension(120, UITheme.BTN_H));
        toDateField.setText(LocalDate.now().format(DATE_FMT));

        JButton filterBtn = UITheme.primaryButton("Filter");
        filterBtn.addActionListener(e -> refresh());

        JButton clearFilterBtn = UITheme.ghostButton("All Time");
        clearFilterBtn.addActionListener(e -> {
            fromDateField.setText("");
            toDateField.setText("");
            refresh();
        });

        filterBar.add(UITheme.bodyLabel("From:"));
        filterBar.add(fromDateField);
        filterBar.add(UITheme.bodyLabel("To:"));
        filterBar.add(toDateField);
        filterBar.add(filterBtn);
        filterBar.add(clearFilterBtn);
        filterBar.add(Box.createHorizontalStrut(20));
        filterBar.add(UITheme.bodyLabel("(Format: yyyy-MM-dd)"));

        // ── Stats cards ────────────────────────────────────────────────────────
        statTransactions = statValue("0");
        statRevenue      = statValue("$0.00");
        statItems        = statValue("0");

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setBackground(UITheme.MAIN_BG);
        statsRow.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        statsRow.add(buildStatCard("Transactions", statTransactions, "🧾"));
        statsRow.add(buildStatCard("Total Revenue", statRevenue,     "💰"));
        statsRow.add(buildStatCard("Items Sold",    statItems,       "🛍"));

        // ── Master order table ─────────────────────────────────────────────────
        String[] masterCols = {"Order #", "Date & Time", "Cashier",
                               "Items", "Payment", "Total"};
        masterModel = new DefaultTableModel(masterCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        masterTable = new JTable(masterModel);
        masterTable.setFont(UITheme.FONT_BODY);
        masterTable.setRowHeight(34);
        masterTable.setShowGrid(true);
        masterTable.setGridColor(UITheme.DIVIDER);
        masterTable.setBackground(Color.WHITE);
        masterTable.setSelectionBackground(UITheme.GOLD);
        masterTable.setSelectionForeground(UITheme.TEXT_PRIMARY);
        masterTable.setFillsViewportHeight(true);
        masterTable.getTableHeader().setFont(UITheme.FONT_SUBHEAD);
        masterTable.getTableHeader().setBackground(UITheme.ROAST);
        masterTable.getTableHeader().setForeground(Color.WHITE);
        masterTable.getTableHeader().setReorderingAllowed(false);

        // Column widths
        int[] cw = {80, 150, 120, 60, 90, 100};
        for (int i = 0; i < cw.length; i++) {
            masterTable.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);
        }
        // Right-align Total column
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        masterTable.getColumnModel().getColumn(5).setCellRenderer(rightAlign);

        JScrollPane masterScroll = new JScrollPane(masterTable);
        masterScroll.setBorder(BorderFactory.createLineBorder(UITheme.DIVIDER));
        masterScroll.getViewport().setBackground(Color.WHITE);

        // ── Detail panel ───────────────────────────────────────────────────────
        detailHeading = UITheme.headingLabel("Select an order to view details");
        detailHeading.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] detailCols = {"Product", "Unit Price", "Qty", "Line Total"};
        detailModel = new DefaultTableModel(detailCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable detailTable = new JTable(detailModel);
        detailTable.setFont(UITheme.FONT_BODY);
        detailTable.setRowHeight(30);
        detailTable.setShowGrid(true);
        detailTable.setGridColor(UITheme.DIVIDER);
        detailTable.setBackground(Color.WHITE);
        detailTable.getTableHeader().setFont(UITheme.FONT_SUBHEAD);
        detailTable.getTableHeader().setBackground(UITheme.MOCHA);
        detailTable.getTableHeader().setForeground(Color.WHITE);
        detailTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane detailScroll = new JScrollPane(detailTable);
        detailScroll.setBorder(BorderFactory.createLineBorder(UITheme.DIVIDER));
        detailScroll.setPreferredSize(new Dimension(0, 160));
        detailScroll.getViewport().setBackground(Color.WHITE);

        JPanel detailPanel = new JPanel(new BorderLayout(0, 4));
        detailPanel.setBackground(UITheme.MAIN_BG);
        detailPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        detailPanel.add(detailHeading, BorderLayout.NORTH);
        detailPanel.add(detailScroll, BorderLayout.CENTER);

        // ── Master + detail split ──────────────────────────────────────────────
        JPanel tableArea = new JPanel(new BorderLayout(0, 0));
        tableArea.setBackground(UITheme.MAIN_BG);
        tableArea.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        tableArea.add(masterScroll, BorderLayout.CENTER);
        tableArea.add(detailPanel,  BorderLayout.SOUTH);

        // ── Center scroll panel ────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.MAIN_BG);
        center.add(statsRow,  BorderLayout.NORTH);
        center.add(tableArea, BorderLayout.CENTER);

        // ── Top stack ─────────────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout());
        top.add(header,    BorderLayout.NORTH);
        top.add(filterBar, BorderLayout.SOUTH);

        add(top,    BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        // ── Master row selection → load detail ────────────────────────────────
        masterTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = masterTable.getSelectedRow();
                if (row >= 0 && currentOrders != null && row < currentOrders.size()) {
                    showDetail(currentOrders.get(row));
                }
            }
        });

        // ── Export: print current view ─────────────────────────────────────────
        JButton exportBtn = UITheme.secondaryButton("🖨  Print / Export");
        exportBtn.addActionListener(e -> {
            try { masterTable.print(); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Print failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel exportRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        exportRow.setBackground(UITheme.MAIN_BG);
        exportRow.add(exportBtn);

        tableArea.add(exportRow, BorderLayout.NORTH);
    }

    // ── Load data ─────────────────────────────────────────────────────────────

    public void refresh() {
        LocalDateTime from = parseDate(fromDateField.getText(), false);
        LocalDateTime to   = parseDate(toDateField.getText(),   true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<Order> orders;
            private double[] stats;

            @Override protected Void doInBackground() {
                orders = orderDAO.getOrders(from, to);
                stats  = orderDAO.getSummaryStats(from, to);
                return null;
            }

            @Override protected void done() {
                currentOrders = orders;
                populateMasterTable(orders);
                statTransactions.setText(String.valueOf((int) stats[0]));
                statRevenue.setText(CurrencyUtil.format(stats[1]));
                statItems.setText(String.valueOf((int) stats[2]));
                detailModel.setRowCount(0);
                detailHeading.setText("Select an order to view details");
            }
        };
        worker.execute();
    }

    private void populateMasterTable(List<Order> orders) {
        masterModel.setRowCount(0);
        for (Order o : orders) {
            masterModel.addRow(new Object[]{
                "#" + o.getId(),
                o.getOrderDate().format(DT_FMT),
                o.getCashierName(),
                o.getTotalItemCount(),
                o.getPaymentMethod().toUpperCase(),
                CurrencyUtil.format(o.getTotalAmount())
            });
        }
    }

    private void showDetail(Order order) {
        detailHeading.setText(
            "Order #" + order.getId() + "  –  " +
            order.getOrderDate().format(DT_FMT) + "  |  " +
            "Subtotal: " + CurrencyUtil.format(order.getSubtotal()) +
            "  Tax: " + CurrencyUtil.format(order.getTaxAmount()) +
            "  Discount: -" + CurrencyUtil.format(order.getDiscountAmount()) +
            "  TOTAL: " + CurrencyUtil.format(order.getTotalAmount()));

        detailModel.setRowCount(0);
        for (OrderItem item : order.getItems()) {
            detailModel.addRow(new Object[]{
                item.getProductName(),
                CurrencyUtil.format(item.getUnitPrice()),
                item.getQuantity(),
                CurrencyUtil.format(item.getLineTotal())
            });
        }
    }

    // ── Stat card ─────────────────────────────────────────────────────────────

    private JPanel buildStatCard(String title, JLabel valueLabel, String emoji) {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JLabel titleLbl = UITheme.bodyLabel(title);

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setOpaque(false);
        left.add(titleLbl,   BorderLayout.NORTH);
        left.add(valueLabel, BorderLayout.CENTER);

        card.add(emojiLbl, BorderLayout.EAST);
        card.add(left,     BorderLayout.CENTER);
        return card;
    }

    private JLabel statValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 22));
        l.setForeground(UITheme.ESPRESSO);
        return l;
    }

    // ── Date parsing ──────────────────────────────────────────────────────────

    private LocalDateTime parseDate(String text, boolean endOfDay) {
        if (text == null || text.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(text.trim(), DATE_FMT);
            return endOfDay
                ? d.atTime(23, 59, 59)
                : d.atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }
}
