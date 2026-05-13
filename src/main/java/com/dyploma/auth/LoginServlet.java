package com.dyploma.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String login = request.getParameter("Email");
        String password = request.getParameter("PasswordHash");

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String dbURL = "jdbc:sqlserver://localhost:1433;databaseName=LearningManagmentSystem;encrypt=true;trustServerCertificate=true";
            String user = "sa";
            String dbPassword = "katrusia1928";
            connection = DriverManager.getConnection(dbURL, user, dbPassword);

            String sql = "SELECT * FROM Users WHERE Email = ? AND PasswordHash = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, login);
            statement.setString(2, password);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String role = resultSet.getString("Role");

                // Збереження користувача в сесії
                int UserID = resultSet.getInt("UserID"); // Отримання userId з бази
                request.getSession().setAttribute("Role", role); // ja vstavyla cej riadok

                HttpSession session = request.getSession();
                session.setAttribute("UserID", UserID);
                session.setAttribute("Email", login);
                session.setAttribute("Role", role);

                // Перенаправлення на сторінку відповідно до ролі
                switch (role) {
                    case "Student":
                        response.sendRedirect("studentProfile.html");
                        break;
                    case "Admin":
                        response.sendRedirect("adminProfile.html");
                        break;
                    case "Teacher":
                        response.sendRedirect("teacherProfile.html");
                        break;
                    default:
                        // Невідома роль
                        request.setAttribute("errorMessage", "Невідома роль користувача");
                        request.getRequestDispatcher("login.jsp").forward(request, response);
                }
            } else {
                // Невірний логін або пароль
                request.setAttribute("errorMessage", "Неправильний логін або пароль");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Помилка підключення до бази: " + e.getMessage());
            request.getRequestDispatcher("login.jsp").forward(request, response);
        } finally {
            try { if (resultSet != null) resultSet.close(); } catch (Exception ignored) {}
            try { if (statement != null) statement.close(); } catch (Exception ignored) {}
            try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        }
    }
}
