package com.dyploma.auth;

import com.dyploma.utils.DatabaseUtils;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/admin/addUser")
public class AdminAddUserServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=LearningManagmentSystem;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "katrusia1928";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String FullName = request.getParameter("FullName");
        String Email = request.getParameter("Email");
        String PasswordHash = request.getParameter("PasswordHash");
        String Role = request.getParameter("Role");

        try (Connection conn = DatabaseUtils.getConnection()) {
            String sql = "INSERT INTO Users (FullName, Email, PasswordHash, Role) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, FullName);
            stmt.setString(2, Email);
            stmt.setString(3, PasswordHash); // Хешування рекомендується
            stmt.setString(4, Role);
            stmt.executeUpdate();
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("Сервлет працює");
    }

}