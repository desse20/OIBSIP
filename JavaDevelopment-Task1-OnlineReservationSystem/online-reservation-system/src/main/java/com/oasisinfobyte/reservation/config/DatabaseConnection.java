package com.oasisinfobyte.reservation.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized JDBC connection manager for MySQL database.
 * Loads configuration securely from external properties files or environment variables.
 */
public final class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/online_reservation_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";

    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    static {
        loadConfiguration();
    }

    private DatabaseConnection() {
        // Prevent direct instantiation
    }

    /**
     * Loads database configuration with fallback order:
     * 1. System properties (db.url, db.user, db.password)
     * 2. Environment variables (DB_URL, DB_USER, DB_PASSWORD)
     * 3. External db.properties in current working directory
     * 4. Classpath resource /db.properties
     * 5. Built-in defaults
     */
    public static synchronized void loadConfiguration() {
        Properties props = new Properties();

        // 1. Try external db.properties in current directory
        File externalPropsFile = new File("db.properties");
        if (externalPropsFile.exists() && externalPropsFile.isFile()) {
            try (InputStream in = new FileInputStream(externalPropsFile)) {
                props.load(in);
                LOGGER.info("Loaded configuration from external db.properties: " + externalPropsFile.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to read external db.properties, checking classpath...", e);
            }
        }

        // 2. Try classpath resource if not loaded
        if (props.isEmpty()) {
            try (InputStream in = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
                if (in != null) {
                    props.load(in);
                    LOGGER.info("Loaded configuration from classpath resource /db.properties");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load /db.properties from classpath", e);
            }
        }

        // Resolve driver
        driver = getFirstNonEmpty(
                System.getProperty("db.driver"),
                System.getenv("DB_DRIVER"),
                props.getProperty("db.driver"),
                DEFAULT_DRIVER
        );

        // Resolve URL
        url = getFirstNonEmpty(
                System.getProperty("db.url"),
                System.getenv("DB_URL"),
                props.getProperty("db.url"),
                DEFAULT_URL
        );

        // Resolve User
        username = getFirstNonEmpty(
                System.getProperty("db.user"),
                System.getenv("DB_USER"),
                props.getProperty("db.user"),
                DEFAULT_USER
        );

        // Resolve Password
        password = getFirstNonEmpty(
                System.getProperty("db.password"),
                System.getenv("DB_PASSWORD"),
                props.getProperty("db.password"),
                ""
        );

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found: " + driver, e);
        }
    }

    /**
     * Obtains a new database connection from DriverManager.
     *
     * @return an active {@link Connection}
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database at: " + url + " as user: " + username, e);
            throw e;
        }
    }

    /**
     * Tests if the database connection can be established.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sets custom connection details (useful for testing or dynamic configuration).
     */
    public static synchronized void setCustomCredentials(String customUrl, String customUser, String customPassword) {
        url = customUrl;
        username = customUser;
        password = customPassword;
    }

    public static String getDatabaseUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }

    private static String getFirstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return "";
    }
}

