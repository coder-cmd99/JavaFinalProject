package com.cafepos.dao;

import com.cafepos.config.ConnectionPool;
import com.cafepos.model.Order;
import com.cafepos.model.OrderItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Data Access Object for {@link Order} – saves and retrieves sales transactions.
 */
public class OrderDAO {

    private static final Logger LOG = Logger.getLogger(OrderDAO.class.getName());

    /**
     * Persists an order and all its line items inside a single transaction.
     *
     * @param order fully populated order (items must be set)
     * @return the generated order ID, or -1 on failure
     */
    public int saveOrder(Order order) {
        final String orderSql =
            "INSERT INTO orders (user_id, order_date, subtotal, tax_amount, " +
            "discount_amount, total_amount, payment_method, status, notes) " +
            "VALUES (?, NOW(), ?, ?, ?, ?, ?, 'completed', ?)";

        final String itemSql =
            "INSERT INTO order_items " +
            "(order_id, product_id, product_name, unit_price, quantity, line_total) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.setAutoCommit(false);                     // begin transaction

            int generatedOrderId;
            try (PreparedStatement ps =
                     conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt   (1, order.getUserId());
                ps.setDouble(2, order.getSubtotal());
                ps.setDouble(3, order.getTaxAmount());
                ps.setDouble(4, order.getDiscountAmount());
                ps.setDouble(5, order.getTotalAmount());
                ps.setString(6, order.getPaymentMethod());
                ps.setString(7, order.getNotes());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Order insert returned no key.");
                    generatedOrderId = keys.getInt(1);
                }
            }

            // Insert each line item
            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (OrderItem item : order.getItems()) {
                    ps.setInt   (1, generatedOrderId);
                    ps.setInt   (2, item.getProductId());
                    ps.setString(3, item.getProductName());
                    ps.setDouble(4, item.getUnitPrice());
                    ps.setInt   (5, item.getQuantity());
                    ps.setDouble(6, item.getLineTotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            LOG.info("Order #" + generatedOrderId + " saved successfully.");
            return generatedOrderId;

        } catch (SQLException e) {
            LOG.severe("Error saving order: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            return -1;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    ConnectionPool.getInstance().releaseConnection(conn);
                }
            } catch (SQLException ignored) {}
        }
    }

    /**
     * Retrieves all orders with their items for the report screen.
     *
     * @param fromDate optional lower bound (inclusive), null to skip
     * @param toDate   optional upper bound (inclusive), null to skip
     * @return list of orders newest-first
     */
    public List<Order> getOrders(LocalDateTime fromDate, LocalDateTime toDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT o.id, o.user_id, u.full_name AS cashier_name, o.order_date, " +
            "       o.subtotal, o.tax_amount, o.discount_amount, " +
            "       o.total_amount, o.payment_method, o.status, o.notes " +
            "FROM orders o " +
            "JOIN users u ON o.user_id = u.id " +
            "WHERE o.status = 'completed' ");

        if (fromDate != null) sql.append("AND o.order_date >= ? ");
        if (toDate   != null) sql.append("AND o.order_date <= ? ");
        sql.append("ORDER BY o.order_date DESC");

        List<Order> orders = new ArrayList<>();
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                if (fromDate != null) ps.setTimestamp(idx++, Timestamp.valueOf(fromDate));
                if (toDate   != null) ps.setTimestamp(idx,   Timestamp.valueOf(toDate.withHour(23).withMinute(59).withSecond(59)));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Order o = new Order();
                        o.setId(rs.getInt("id"));
                        o.setUserId(rs.getInt("user_id"));
                        o.setCashierName(rs.getString("cashier_name"));
                        o.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                        o.setSubtotal(rs.getDouble("subtotal"));
                        o.setTaxAmount(rs.getDouble("tax_amount"));
                        o.setDiscountAmount(rs.getDouble("discount_amount"));
                        o.setTotalAmount(rs.getDouble("total_amount"));
                        o.setPaymentMethod(rs.getString("payment_method"));
                        o.setStatus(rs.getString("status"));
                        o.setNotes(rs.getString("notes"));
                        orders.add(o);
                    }
                }
            }

            // Load items for every fetched order in one query
            if (!orders.isEmpty()) {
                loadItemsForOrders(conn, orders);
            }

        } catch (SQLException e) {
            LOG.severe("Error loading orders: " + e.getMessage());
        } finally {
            release(conn);
        }
        return orders;
    }

    /** Returns summary statistics: total revenue, total items, transaction count. */
    public double[] getSummaryStats(LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) AS cnt, " +
            "       SUM(total_amount) AS revenue, " +
            "       SUM((SELECT SUM(quantity) FROM order_items oi WHERE oi.order_id = o.id)) AS items " +
            "FROM orders o WHERE o.status = 'completed' ");
        if (from != null) sql.append("AND order_date >= ? ");
        if (to   != null) sql.append("AND order_date <= ? ");

        double[] stats = {0, 0, 0};   // [transactions, revenue, items]
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                if (from != null) ps.setTimestamp(idx++, Timestamp.valueOf(from));
                if (to   != null) ps.setTimestamp(idx,   Timestamp.valueOf(to.withHour(23).withMinute(59).withSecond(59)));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stats[0] = rs.getInt("cnt");
                        stats[1] = rs.getDouble("revenue");
                        stats[2] = rs.getDouble("items");
                    }
                }
            }
        } catch (SQLException e) {
            LOG.severe("Error loading stats: " + e.getMessage());
        } finally {
            release(conn);
        }
        return stats;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void loadItemsForOrders(Connection conn, List<Order> orders)
            throws SQLException {

        // Build a map for quick lookup
        Map<Integer, Order> orderMap = new LinkedHashMap<>();
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < orders.size(); i++) {
            orderMap.put(orders.get(i).getId(), orders.get(i));
            inClause.append(i == 0 ? "?" : ",?");
        }

        String itemSql =
            "SELECT order_id, product_id, product_name, unit_price, quantity, line_total " +
            "FROM order_items WHERE order_id IN (" + inClause + ") " +
            "ORDER BY order_id, id";

        try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
            int idx = 1;
            for (Order o : orders) ps.setInt(idx++, o.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setLineTotal(rs.getDouble("line_total"));
                    orderMap.get(item.getOrderId()).getItems().add(item);
                }
            }
        }
    }

    private void release(Connection conn) {
        if (conn != null) {
            try { ConnectionPool.getInstance().releaseConnection(conn); }
            catch (SQLException ignored) {}
        }
    }
}
