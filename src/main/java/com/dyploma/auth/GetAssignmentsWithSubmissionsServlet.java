package com.dyploma.auth;

import com.dyploma.models.Assignment;
import com.dyploma.models.Submission;

import com.dyploma.utils.DatabaseUtils;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;


@WebServlet("/teacher/getAssignmentsWithSubmissions")
public class GetAssignmentsWithSubmissionsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer teacherId = (Integer) request.getSession().getAttribute("UserID");
        if (teacherId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Користувач не авторизований");
            return;
        }

        String courseIdParam = request.getParameter("courseId");
        if (courseIdParam == null || courseIdParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Не передано courseId");
            return;
        }

        int courseId;
        try {
            courseId = Integer.parseInt(courseIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Невірний формат courseId");
            return;
        }

        response.setContentType("application/json; charset=UTF-8");

        String sql = "SELECT a.AssignmentID, a.Title, a.Description, " +
                "s.SubmissionID, s.StudentID, s.SubmissionDate, " +
                "s.Content, s.Grade, s.Feedback, " +
                "u.FullName " +
                "FROM Assignments a " +
                "JOIN Courses c ON a.CourseID = c.CourseID " +
                "LEFT JOIN Submissions s ON a.AssignmentID = s.AssignmentID " +
                "LEFT JOIN Users u ON s.StudentID = u.UserID " +
                "WHERE c.TeacherID = ? AND c.CourseID = ? " +
                "ORDER BY a.AssignmentID DESC";

        try (
                Connection conn = DatabaseUtils.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, teacherId);
            stmt.setInt(2, courseId);

            try (ResultSet rs = stmt.executeQuery()) {

                Map<Integer, Assignment> assignments = new LinkedHashMap<>();

                while (rs.next()) {
                    int assignmentId = rs.getInt("AssignmentID");

                    Assignment assignment = assignments.get(assignmentId);
                    if (assignment == null) {
                        assignment = new Assignment();
                        assignment.setAssignmentID(assignmentId);
                        assignment.setTitle(rs.getString("Title"));
                        assignment.setDescription(rs.getString("Description"));
                        assignment.setSubmissions(new ArrayList<>());
                        assignments.put(assignmentId, assignment);
                    }

                    Integer submissionId = rs.getObject("SubmissionID") != null ? rs.getInt("SubmissionID") : null;
                    if (submissionId != null) {
                        Submission submission = new Submission();
                        submission.setSubmissionID(submissionId);
                        submission.setStudentId(rs.getInt("StudentID"));
                        submission.setStudentName(rs.getString("FullName"));
                        Date submissionDate = rs.getDate("SubmissionDate");
                        submission.setSubmissionDate(submissionDate); // Якщо в класі дозволено null
                        //submission.setSubmissionDate(rs.getDate("SubmissionDate"));
                        submission.setContent(rs.getString("Content"));
                        submission.setGrade(rs.getString("Grade"));
                        submission.setFeedback(rs.getString("Feedback"));
                        assignment.getSubmissions().add(submission);
                    }
                }

                String json = new Gson().toJson(assignments.values());
                response.getWriter().write(json);
                PrintWriter out = response.getWriter();
                out.print(json);
                out.flush();
                out.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(500, "Помилка бази даних: " + e.getMessage());
        }
    }

}
