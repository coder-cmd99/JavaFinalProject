package com.cafepos.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Simple connection pool that keeps a fixed number of reusable JDBC connections.
 * Thread-safe with synchronized borrow / return methods.
 */
public class ConnectionPool {

    private static final Logger LOG = Logger.getLogger(ConnectionPool.class.getName());
    private static ConnectionPool instance;

    private final List<Connection> pool = new ArrayList<>();
    private final List<Connection> usedConnections = new ArrayList<>();

    private ConnectionPool() throws SQLException {
        try {
            Class.forName(DatabaseConfig.DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found on classpath. "
                + "Add mysql-connector-j-*.jar to /lib and rebuild.", e);
        }
        for (int i = 0; i < DatabaseConfig.POOL_INITIAL_SIZE; i++) {
            pool.add(createConnection());
        }
    }

    /** Returns the singleton pool, creating it on first call. */
    public static synchronized ConnectionPool getInstance() throws SQLException {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    /** Borrow a connection. Grows the pool up to MAX_SIZE if necessary. */
    public synchronized Connection getConnection() throws SQLException {
        if (pool.isEmpty()) {
            if (usedConnections.size() < DatabaseConfig.POOL_MAX_SIZE) {
                pool.add(createConnection());
            } else {
                throw new SQLException("Maximum pool size reached – all connections are in use.");
            }
        }
        Connection conn = pool.remove(pool.size() - 1);
        // Validate; replace if stale
        try {
            if (!conn.isValid(2)) {
                conn.close();
                conn = createConnection();
            }
        } catch (SQLException ignored) {
            conn = createConnection();
        }
        usedConnections.add(conn);
        return conn;
    }

    /** Return a connection to the pool. */
    public synchronized boolean releaseConnection(Connection conn) {
        pool.add(conn);
        return usedConnections.remove(conn);
    }

    /** Closes all connections in the pool and resets the singleton. */
    public synchronized void shutdown() {
        for (Connection c : usedConnections) closeQuietly(c);
        for (Connection c : pool)           closeQuietly(c);
        usedConnections.clear();
        pool.clear();
        instance = null;
        LOG.info("Connection pool shut down.");
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
            DatabaseConfig.JDBC_URL,
            DatabaseConfig.DB_USER,
            DatabaseConfig.DB_PASSWORD);
    }

    private void closeQuietly(Connection c) {
        try { if (c != null && !c.isClosed()) c.close(); }
        catch (SQLException ignored) {}
    }
}
