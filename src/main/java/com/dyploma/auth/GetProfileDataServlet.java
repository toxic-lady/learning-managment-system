package com.dyploma.auth;

import com.dyploma.utils.DatabaseUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/user/getProfileData")
public class GetProfileDataServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userID") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Користувач не авторизований");
            return;
        }

        int userID = (int) session.getAttribute("UserID");

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT FullName, Email FROM Users WHERE UserID = ?")) {

            stmt.setInt(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JSONObject json = new JSONObject();
                    json.put("fullName", rs.getString("FullName"));
                    json.put("email", rs.getString("Email"));
                    response.getWriter().write(json.toString());
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Користувача не знайдено");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Помилка бази даних");
        }
    }
}
