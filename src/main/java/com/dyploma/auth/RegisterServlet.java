package com.dyploma.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=LearningManagmentSystem;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "katrusia1928";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String fullName = request.getParameter("FullName");
        String email = request.getParameter("Email");
        String password = request.getParameter("PasswordHash");

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Перевірка наявності користувача
            String checkQuery = "SELECT * FROM Users WHERE Email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                out.println("<script>alert('Користувач уже існує'); window.location='index.jsp';</script>");
            } else {
                String insertQuery = "INSERT INTO Users (FullName, Email, PasswordHash, Role) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, fullName);
                insertStmt.setString(2, email);
                insertStmt.setString(3, password);
                insertStmt.setString(4, "Student");

                int rows = insertStmt.executeUpdate();

                if (rows > 0) {
                    out.println("<script>alert('Дякуємо за реєстрацію!'); window.location='index.jsp';</script>");
                } else {
                    out.println("<script>alert('Помилка при реєстрації.'); window.location='index.jsp';</script>");
                }
                insertStmt.close();
            }

            rs.close();
            checkStmt.close();
            conn.close();
        } catch (Exception e) {
            out.println("<h3>Помилка з'єднання з БД: " + e.getMessage() + "</h3>");
        }
    }
}
