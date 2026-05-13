package com.dyploma.auth;

import com.dyploma.utils.DatabaseUtils;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/student/getCourses")
public class StudentCoursesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("UserID") == null) {
            System.out.println("Session UserID: null (Користувач не авторизований)");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int studentId = (int) session.getAttribute("UserID");
        System.out.println("Session UserID: " + studentId); // Логування значення userId

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT CourseID, CourseName, Description FROM Courses JOIN Enrollments ON CourseId WHERE StudentID = ?")) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            JSONArray coursesArray = new JSONArray();

            while (rs.next()) {
                JSONObject course = new JSONObject();
                course.put("id", rs.getInt("CourseID"));
                course.put("name", rs.getString("CourseName"));
                course.put("description", rs.getString("Description"));
                coursesArray.put(course);
            }

            System.out.println("Знайдені курси: " + coursesArray.toString()); // Логування курсів
            response.getWriter().write(coursesArray.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Помилка доступу до бази даних");
        }
    }
}

