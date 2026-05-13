package com.dyploma.auth;

import com.dyploma.utils.DatabaseUtils;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/admin/getCourses")
public class AdminGetCoursesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT CourseID, CourseName FROM Courses");
             ResultSet rs = stmt.executeQuery()) {

            JSONArray coursesArray = new JSONArray();

            while (rs.next()) {
                JSONObject course = new JSONObject();
                course.put("id", rs.getInt("CourseID"));
                course.put("name", rs.getString("CourseName"));
                coursesArray.put(course);
            }

            response.getWriter().write(coursesArray.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Помилка доступу до бази даних");
        }
    }
}
