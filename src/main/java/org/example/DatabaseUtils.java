/*
* package org.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DatabaseUtils {

    // Database connection details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/emailautomation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";



    // Method to save email logs to the database
    public static void saveEmailToDatabase(String recipientEmail, String subject, String status, Timestamp timestamp ) {
        // SQL query for inserting email log
        String query = "INSERT INTO email_logs (recipient_email, subject,status,timestamp) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            // Use PreparedStatement to prevent SQL injection
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, recipientEmail); // Recipient email
            statement.setString(2, subject);        // Subject of the email

            statement.setString(3, status);
            statement.setTimestamp(4, timestamp);
            // Status (e.g., "Sent", "Failed")

            // Execute the query
            statement.executeUpdate();
            //System.out.println("✔️ Email logged for recipient: " + recipientEmail);
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace(); // Optional: Useful for debugging stack trace
        }
    }
}

* */