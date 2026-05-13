package com.dyploma.auth;

import com.dyploma.utils.DatabaseUtils;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/admin/deleteCourse")
public class AdminDeleteCourseServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("courseId");
        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Не вказано ID курсу.");
            return;
        }

        int courseId;
        try {
            courseId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неправильний формат ID курсу.");
            return;
        }

        try (Connection conn = DatabaseUtils.getConnection()) {
            conn.setAutoCommit(false); // Починаємо транзакцію

            try {
                // Видалення залежних даних
                PreparedStatement deleteSubmissions = conn.prepareStatement("DELETE FROM Submissions WHERE CourseID = ?");
                deleteSubmissions.setInt(1, courseId);
                deleteSubmissions.executeUpdate();

                PreparedStatement deleteGrades = conn.prepareStatement("DELETE FROM Grades WHERE CourseID = ?");
                deleteGrades.setInt(1, courseId);
                deleteGrades.executeUpdate();

                PreparedStatement deleteAssignments = conn.prepareStatement("DELETE FROM Assignments WHERE CourseID = ?");
                deleteAssignments.setInt(1, courseId);
                deleteAssignments.executeUpdate();

                PreparedStatement deleteEnrollments = conn.prepareStatement("DELETE FROM Enrollments WHERE CourseID = ?");
                deleteEnrollments.setInt(1, courseId);
                deleteEnrollments.executeUpdate();

                PreparedStatement deleteCourse = conn.prepareStatement("DELETE FROM Courses WHERE CourseID = ?");
                deleteCourse.setInt(1, courseId);
                int affected = deleteCourse.executeUpdate();

                if (affected == 0) {
                    conn.rollback();
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Курс не знайдено.");
                    return;
                }

                conn.commit();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Курс успішно видалено");

            } catch (SQLException ex) {
                conn.rollback(); // Відкат транзакції при помилці
                ex.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Помилка при видаленні курсу.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Проблема з базою даних.");
        }
    }
}
