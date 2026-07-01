package com.cafepos.dao;

import com.cafepos.config.ConnectionPool;
import com.cafepos.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Data Access Object for {@link User} – handles login authentication.
 */
public class UserDAO {

    private static final Logger LOG = Logger.getLogger(UserDAO.class.getName());

    /**
     * Validates credentials against the database.
     *
     * @param username plain username
     * @param password plain password (demo uses plain text; use BCrypt in production)
     * @return the matching {@link User}, or {@code null} if credentials are invalid
     */
    public User authenticate(String username, String password) {
        final String sql =
            "SELECT id, username, full_name, role " +
            "FROM users " +
            "WHERE username = ? AND password = ? AND active = 1";

        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setFullName(rs.getString("full_name"));
                        user.setRole(rs.getString("role"));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            LOG.severe("Authentication error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { ConnectionPool.getInstance().releaseConnection(conn); }
                catch (SQLException ignored) {}
            }
        }
        return null;
    }
}
