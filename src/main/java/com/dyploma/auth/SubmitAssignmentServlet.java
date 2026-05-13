package com.dyploma.auth;

import com.dyploma.utils.DatabaseUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.nio.file.Paths;
import java.sql.*;
import java.util.UUID;

@WebServlet("/SubmitAssignmentServlet")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1 MB
        maxFileSize = 10 * 1024 * 1024,  // 10 MB
        maxRequestSize = 50 * 1024 * 1024 // 50 MB
)
public class SubmitAssignmentServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("UserID") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int studentId = (int) session.getAttribute("UserID");
        int assignmentID = Integer.parseInt(request.getParameter("assignmentID"));
        String content = request.getParameter("content");

        // 1. Зберегти файл, якщо він є
        Part filePart = request.getPart("file");
        String filePath = null;

        if (filePart != null && filePart.getSize() > 0) {
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String uniqueFileName = UUID.randomUUID() + "_" + fileName;

            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdir();

            filePath = UPLOAD_DIR + File.separator + uniqueFileName; // відносний шлях для збереження в БД
            filePart.write(uploadPath + File.separator + uniqueFileName);
        }

        try (Connection conn = DatabaseUtils.getConnection()) {
            // 2. Отримати CourseID
            int courseId = -1;
            PreparedStatement selectStmt = conn.prepareStatement(
                    "SELECT CourseID FROM Assignments WHERE AssignmentID = ?"
            );
            selectStmt.setInt(1, assignmentID);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                courseId = rs.getInt("CourseID");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Завдання не знайдено");
                return;
            }

            // 3. Зберегти submission з файлом (якщо є)
            PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO Submissions (AssignmentID, StudentID, SubmissionDate, Content, CourseID, FilePath) " +
                            "VALUES (?, ?, GETDATE(), ?, ?, ?)"
            );
            insertStmt.setInt(1, assignmentID);
            insertStmt.setInt(2, studentId);
            insertStmt.setString(3, content);
            insertStmt.setInt(4, courseId);
            insertStmt.setString(5, filePath); // може бути null, якщо файл не завантажено

            insertStmt.executeUpdate();
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Помилка при збереженні відповіді");
        }
    }
}
