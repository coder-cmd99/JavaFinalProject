package com.cafepos.dao;

import com.cafepos.config.ConnectionPool;
import com.cafepos.model.Category;
import com.cafepos.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for products and categories.
 */
public class ProductDAO {

    private static final Logger LOG = Logger.getLogger(ProductDAO.class.getName());

    /** Returns all active categories ordered by sort_order. */
    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        final String sql =
            "SELECT id, name, icon_emoji, sort_order " +
            "FROM categories ORDER BY sort_order";

        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("icon_emoji"),
                        rs.getInt("sort_order")));
                }
            }
        } catch (SQLException e) {
            LOG.severe("Error loading categories: " + e.getMessage());
        } finally {
            release(conn);
        }
        return list;
    }

    /** Returns all products joined with their category name. */
    public List<Product> getAllProducts() {
        return getProductsByCategory(0);   // 0 = all
    }

    /**
     * Returns products for a specific category (pass 0 for all).
     */
    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> list = new ArrayList<>();
        String sql =
            "SELECT p.id, p.category_id, c.name AS cat_name, " +
            "       p.name, p.description, p.price, p.image_emoji, p.available " +
            "FROM products p " +
            "JOIN categories c ON p.category_id = c.id ";
        if (categoryId > 0) {
            sql += "WHERE p.category_id = ? ";
        }
        sql += "ORDER BY c.sort_order, p.name";

        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (categoryId > 0) ps.setInt(1, categoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Product p = new Product(
                            rs.getInt("id"),
                            rs.getInt("category_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getString("image_emoji"),
                            rs.getBoolean("available"));
                        p.setCategoryName(rs.getString("cat_name"));
                        list.add(p);
                    }
                }
            }
        } catch (SQLException e) {
            LOG.severe("Error loading products: " + e.getMessage());
        } finally {
            release(conn);
        }
        return list;
    }

    /** Searches products by name (case-insensitive). */
    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        final String sql =
            "SELECT p.id, p.category_id, c.name AS cat_name, " +
            "       p.name, p.description, p.price, p.image_emoji, p.available " +
            "FROM products p " +
            "JOIN categories c ON p.category_id = c.id " +
            "WHERE LOWER(p.name) LIKE LOWER(?) " +
            "ORDER BY p.name";

        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + keyword + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Product p = new Product(
                            rs.getInt("id"),
                            rs.getInt("category_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getString("image_emoji"),
                            rs.getBoolean("available"));
                        p.setCategoryName(rs.getString("cat_name"));
                        list.add(p);
                    }
                }
            }
        } catch (SQLException e) {
            LOG.severe("Error searching products: " + e.getMessage());
        } finally {
            release(conn);
        }
        return list;
    }

    private void release(Connection conn) {
        if (conn != null) {
            try { ConnectionPool.getInstance().releaseConnection(conn); }
            catch (SQLException ignored) {}
        }
    }
}
