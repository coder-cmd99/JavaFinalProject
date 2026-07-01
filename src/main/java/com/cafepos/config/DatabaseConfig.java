package com.cafepos.config;

/**
 * Central database connection configuration.
 * Modify DB_HOST, DB_NAME, DB_USER, DB_PASSWORD to match your local MySQL setup.
 */
public final class DatabaseConfig {

    // ── Change these to match your MySQL environment ──────────────────────────
    public static final String DB_HOST     = "localhost";
    public static final int    DB_PORT     = 3306;
    public static final String DB_NAME     = "cafe_pos";
    public static final String DB_USER     = "root";
    public static final String DB_PASSWORD = "123";          // set your MySQL password
    // ─────────────────────────────────────────────────────────────────────────

    public static final String JDBC_URL =
        "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    public static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    // Connection pool settings
    public static final int POOL_INITIAL_SIZE = 2;
    public static final int POOL_MAX_SIZE      = 10;

    // Tax rate applied to every order (8 %)
    public static final double TAX_RATE = 0.08;

    private DatabaseConfig() {}
}
