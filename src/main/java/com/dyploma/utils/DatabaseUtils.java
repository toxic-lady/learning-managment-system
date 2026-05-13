package com.dyploma.utils;

import java.sql.*;

public class DatabaseUtils {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=LearningManagmentSystem;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "katrusia1928";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static int getUserIdByEmail(Connection conn, String email) throws SQLException {
        String sql = "SELECT UserID FROM Users WHERE Email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("UserID");
            } else {
                throw new SQLException("User not found: " + email);
            }
        }
    }
}

