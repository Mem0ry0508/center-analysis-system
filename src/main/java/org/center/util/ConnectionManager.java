package org.center.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionManager {

    private static final Properties CONFIG = loadConfig();

    private ConnectionManager() {
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        Path configPath = Path.of("config.properties");
        if (Files.exists(configPath)) {
            try (InputStream in = Files.newInputStream(configPath)) {
                props.load(in);
                return props;
            } catch (IOException e) {
                throw new IllegalStateException("無法讀取 config.properties", e);
            }
        }
        try (InputStream in = ConnectionManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "找不到 config.properties，請複製 config.properties.example 為 config.properties 並填入連線資訊");
            }
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("無法讀取 config.properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = trim(CONFIG.getProperty("db.url"));
        String user = trim(CONFIG.getProperty("db.user"));
        String password = trim(CONFIG.getProperty("db.password"));
        return DriverManager.getConnection(url, user, password);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
