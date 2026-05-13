package com.dyploma.auth;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.dyploma.utils.DatabaseUtils;

import java.io.IOException;
import java.sql.*;
import java.io.BufferedReader;

@WebServlet("/admin/createCourse")
public class AdminCreateCourseServlet extends HttpServlet {

    static class CourseRequest {
        String name;
        String description;
        String teacherEmail;
        String[] students;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain; charset=UTF-8");

        // Читання JSON із запиту
        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        Gson gson = new Gson();
        CourseRequest courseRequest = gson.fromJson(jsonBuffer.toString(), CourseRequest.class);

        if (courseRequest == null || courseRequest.name == null || courseRequest.teacherEmail == null || courseRequest.students == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Некоректні дані");
            return;
        }

        try (Connection conn = DatabaseUtils.getConnection()) {
            conn.setAutoCommit(false); // транзакція

            // Знайти ID викладача
            int teacherId = getUserIdByEmail(conn, courseRequest.teacherEmail, "Teacher");
            if (teacherId == -1) {
                conn.rollback();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Викладача не знайдено");
                return;
            }

            // Створити курс
            int courseId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Courses (CourseName, Description, TeacherID) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, courseRequest.name);
                ps.setString(2, courseRequest.description);
                ps.setInt(3, teacherId);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        courseId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().write("Не вдалося створити курс");
                        return;
                    }
                }
            }

            // Додати студентів до курсу
            try (PreparedStatement enrollStmt = conn.prepareStatement(
                    "INSERT INTO Enrollments (CourseID, StudentID) VALUES (?, ?)")) {

                for (String studentEmail : courseRequest.students) {
                    int studentId = getUserIdByEmail(conn, studentEmail, "Student");
                    if (studentId != -1) {
                        enrollStmt.setInt(1, courseId);
                        enrollStmt.setInt(2, studentId);
                        enrollStmt.addBatch();
                    }
                }
                enrollStmt.executeBatch();
            }

            conn.commit();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Курс створено успішно");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Помилка сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getUserIdByEmail(Connection conn, String email, String expectedRole) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT UserID, Role FROM Users WHERE Email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("Role");
                    if (role.equalsIgnoreCase(expectedRole)) {
                        return rs.getInt("UserID");
                    }
                }
            }
        }
        return -1; // не знайдено або роль не збігається
    }
}
