package com.minidmart.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Loads DB config from db.properties on the classpath, with DB_URL / DB_USER /
 * DB_PASSWORD / DB_DRIVER environment variables taking precedence when set —
 * so a hosting platform (Render, Railway, etc.) can inject credentials without
 * a properties file ever containing a real secret. Hands out plain JDBC
 * connections; deliberately no connection pool, as this app is sized for an
 * assessment/demo load, not production traffic.
 */
public final class DBUtil {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        URL = envOr("DB_URL", props.getProperty("db.url"));
        USER = envOr("DB_USER", props.getProperty("db.user"));
        PASSWORD = envOr("DB_PASSWORD", props.getProperty("db.password"));
        String driver = envOr("DB_DRIVER", props.getProperty("db.driver"));

        if (URL == null || USER == null) {
            throw new ExceptionInInitializerError(
                    "No DB configuration found. Either set DB_URL/DB_USER/DB_PASSWORD environment variables, "
                            + "or copy db.properties.example to src/main/resources/db.properties and fill it in.");
        }
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String envOr(String envName, String fallback) {
        String value = System.getenv(envName);
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
