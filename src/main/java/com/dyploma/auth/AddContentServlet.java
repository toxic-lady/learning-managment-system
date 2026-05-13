package com.dyploma.auth;

import com.dyploma.utils.DatabaseUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import com.dyploma.utils.DatabaseUtils;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

@WebServlet("/teacher/addContent")
public class AddContentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String courseIdStr = request.getParameter("courseId");
        String type = request.getParameter("type"); // "Lecture" або "Assignment"
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String dueDate = request.getParameter("dueDate"); // для Assignment

        System.out.println("Отримано courseId: " + courseIdStr);
        System.out.println("Отримано type: " + type);
        System.out.println("Отримано title: " + title);
        System.out.println("Отримано description: " + description);
        System.out.println("Отримано dueDate: " + dueDate);

        if (courseIdStr == null || courseIdStr.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "courseId не передано");
            return;
        }

        int courseId = Integer.parseInt(courseIdStr);

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Assignments (CourseID, Title, Description, DueDate, Type) VALUES (?, ?, ?, ?, ?)"
             )) {

            stmt.setInt(1, courseId);
            stmt.setString(2, title);
            stmt.setString(3, description);

            if ("Assignment".equals(type) && dueDate != null && !dueDate.isEmpty()) {
                stmt.setDate(4, Date.valueOf(dueDate));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, type); // Lecture або Assignment

            stmt.executeUpdate();
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("Контент успішно додано");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Не вдалося додати контент");
        }
    }
}
