package com.dyploma.auth;

import com.dyploma.utils.DatabaseUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;

import java.io.*;


@WebServlet("/teacher/updateGrade")
public class UpdateGradeServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int submissionId = Integer.parseInt(request.getParameter("submissionId"));
            String grade = request.getParameter("grade");
            String feedback = request.getParameter("feedback");

            response.setContentType("text/plain; charset=UTF-8");

            try (Connection conn = DatabaseUtils.getConnection()) {
                String sql = "UPDATE Submissions SET Grade = ?, Feedback = ? WHERE SubmissionID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, grade);
                    stmt.setString(2, feedback);
                    stmt.setInt(3, submissionId);
                    int updated = stmt.executeUpdate();

                    PrintWriter out = response.getWriter();
                    if (updated > 0) {
                        out.print("Оцінка оновлена");
                    } else {
                        response.sendError(404, "Здача не знайдена");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Помилка: " + e.getMessage());
        }
    }
}
