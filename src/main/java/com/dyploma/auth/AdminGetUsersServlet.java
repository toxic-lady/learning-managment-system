package com.dyploma.auth;
import com.dyploma.utils.DatabaseUtils;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/admin/getUsers")
public class AdminGetUsersServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roleFilter = request.getParameter("role");
        response.setContentType("application/json;charset=UTF-8");

        try (Connection conn = DatabaseUtils.getConnection()) {
            String sql = "SELECT UserID, FullName, Email, Role FROM Users";
            if (roleFilter != null && !roleFilter.isEmpty()) {
                sql += " WHERE Role = ?";
            }
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (roleFilter != null && !roleFilter.isEmpty()) {
                stmt.setString(1, roleFilter);
            }
            ResultSet rs = stmt.executeQuery();

            StringBuilder json = new StringBuilder("[");
            while (rs.next()) {
                json.append(String.format("{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"role\":\"%s\"},",
                        rs.getInt("UserID"), rs.getString("FullName"), rs.getString("Email"), rs.getString("Role")));
            }
            if (json.charAt(json.length() - 1) == ',') json.setLength(json.length() - 1);
            json.append("]");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(json.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}