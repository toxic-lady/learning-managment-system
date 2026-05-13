package com.dyploma.auth;

import com.dyploma.utils.DatabaseUtils;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

@WebServlet("/user/getMyCourses")
public class GetMyCoursesServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("UserID") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int studentId = (int) session.getAttribute("UserID");
        List<Map<String, Object>> coursesWithDetails = new ArrayList<>();

        try (Connection conn = DatabaseUtils.getConnection()) {
            String sql = "SELECT c.CourseID, c.CourseName, c.Description, c.TeacherID, u.FullName AS TeacherName " +
                    "FROM Courses c " +
                    "JOIN Enrollments e ON c.CourseID = e.CourseID " +
                    "LEFT JOIN Users u ON c.TeacherID = u.UserID " +
                    "WHERE e.StudentID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int courseId = rs.getInt("CourseID");

                        Map<String, Object> course = new HashMap<>();
                        course.put("courseID", courseId);
                        course.put("courseName", rs.getString("CourseName"));
                        course.put("description", rs.getString("Description"));
                        course.put("teacherID", rs.getInt("TeacherID"));
                        course.put("teacherName", rs.getString("TeacherName"));

                        List<Map<String, Object>> assignments = new ArrayList<>();
                        List<Map<String, Object>> lectures = new ArrayList<>();

                        String sqlAssignments = "SELECT a.AssignmentID, a.Title, a.Description, a.DueDate, a.Type, " +
                                "s.SubmissionID, s.Content AS SubmittedContent, s.SubmissionDate, " +
                                "g.Grade, g.Feedback " +
                                "FROM Assignments a " +
                                "LEFT JOIN Submissions s ON a.AssignmentID = s.AssignmentID AND s.StudentID = ? " +
                                "LEFT JOIN Grades g ON g.SubmissionID = s.SubmissionID " +
                                "WHERE a.CourseID = ? " +
                                "ORDER BY a.DueDate";
                        try (PreparedStatement stmtA = conn.prepareStatement(sqlAssignments)) {
                            stmtA.setInt(1, studentId);
                            stmtA.setInt(2, courseId);
                            try (ResultSet rsA = stmtA.executeQuery()) {
                                while (rsA.next()) {
                                    String type = rsA.getString("Type");
                                    if ("Lecture".equalsIgnoreCase(type)) {
                                        Map<String, Object> lecture = new HashMap<>();
                                        lecture.put("lectureID", rsA.getInt("AssignmentID"));
                                        lecture.put("title", rsA.getString("Title"));
                                        lecture.put("description", rsA.getString("Description"));
                                        lecture.put("dueDate", rsA.getDate("DueDate"));
                                        lectures.add(lecture);
                                    } else if ("Assignment".equalsIgnoreCase(type)) {
                                        Map<String, Object> assignment = new HashMap<>();
                                        assignment.put("assignmentID", rsA.getInt("AssignmentID"));
                                        assignment.put("title", rsA.getString("Title"));
                                        assignment.put("description", rsA.getString("Description"));
                                        assignment.put("dueDate", rsA.getDate("DueDate"));

                                        String submittedContent = rsA.getString("SubmittedContent");
                                        assignment.put("status", submittedContent != null ? "здано" : "не виконано");
                                        assignment.put("submittedContent", submittedContent);
                                        assignment.put("submissionDate", rsA.getTimestamp("SubmissionDate"));

                                        // Нові поля: оцінка та коментар
                                        assignment.put("grade", rsA.getObject("Grade"));
                                        assignment.put("feedback", rsA.getString("Feedback"));

                                        assignments.add(assignment);
                                    }
                                }
                            }
                        }

                        course.put("assignments", assignments);
                        course.put("lectures", lectures);

                        coursesWithDetails.add(course);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(coursesWithDetails));
        out.flush();
    }
}
