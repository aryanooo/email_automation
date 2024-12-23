package org.example;
import java.sql.Timestamp;
import java.util.*;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class EmailApprovalApp {
    public static void main(String args[]) {
        HttpServer server = null;

        try {
            // Start HTTP server
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", new HttpHandler() {
                public void handle(HttpExchange exchange) throws IOException {
                    String html = "<html>" +
                            "<head><title>Email Approval System</title>" +
                            "<style>" +
                            "body { font-family: Arial, sans-serif; margin: 40px; }" +
                            ".container { max-width: 600px; margin: 0 auto; }" +
                            ".form-group { margin-bottom: 15px; }" +
                            "label { display: block; margin-bottom: 5px; }" +
                            "input[type='text'], input[type='email'], textarea {" +
                            "    width: 100%; padding: 8px;" +
                            "    border: 1px solid #ddd;" +
                            "    border-radius: 4px;" +
                            "}" +
                            "button {" +
                            "    background-color: #4CAF50;" +
                            "    color: white;" +
                            "    padding: 10px 15px;" +
                            "    border: none;" +
                            "    border-radius: 4px;" +
                            "    cursor: pointer;" +
                            "}" +
                            "</style></head>" +
                            "<body>" +
                            "<div class='container'>" +
                            "<h1>Email Approval System</h1>" +
                            "<form action='/send' method='post'>" +
                            "<div class='form-group'>" +
                            "<label>Recipient Email:</label>" +
                            "<input type='email' name='recipientEmail' required>" +
                            "</div>" +
                            "<div class='form-group'>" +
                            "<label>Subject:</label>" +
                            "<input type='text' name='subject' required>" +
                            "</div>" +
                            "<div class='form-group'>" +
                            "<label>Message:</label>" +
                            "<textarea name='message' rows='4' required></textarea>" +
                            "</div>" +
                            "<button type='submit'>Send Approval Request</button>" +
                            "</form>" +
                            "</div>" +
                            "</body></html>";

                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, html.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(html.getBytes());
                    os.close();
                }
            });

            server.createContext("/send", new HttpHandler() {
                public void handle(HttpExchange exchange) throws IOException {
                    if ("POST".equals(exchange.getRequestMethod())) {
                        // Get form data
                        String requestBody = new String(exchange.getRequestBody().readAllBytes());
                        Map<String, String> formData = parseFormData(requestBody);

                        String recipientEmail = formData.get("recipientEmail");
                        String subject = formData.get("subject");
                        String bodyMessage = formData.get("message");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                        // Static Sender Email and Password
                        String senderEmail = "aniketchourasiya1632000@gmail.com";
                        String senderPassword = "iieg irch mlpr akdv";

                        // Generate approval email body
                        String htmlContent = generateApprovalEmailBody(bodyMessage, senderEmail);

                        // Initialize services
                        EmailService emailService = new EmailService(senderEmail, senderPassword);
                        GmailReader gmailReader = new GmailReader(senderEmail, senderPassword);

                        boolean responseReceived = false;

                        // Retry logic for sending email and checking responses
                        for (int attempt = 1; attempt <= 3; attempt++) {
                            System.out.println("\n🔄 Attempt " + attempt + " to send email...");
                            emailService.sendEmail(recipientEmail, subject, htmlContent);

                            System.out.println("⏳ Waiting for recipient's response...");
                            try {
                                Thread.sleep(2000); // Wait for 2 seconds
                            } catch (InterruptedException e) {
                                System.err.println("⚠️ Error while waiting: " + e.getMessage());
                            }

                            responseReceived = gmailReader.checkForApprovalResponse(recipientEmail);
                            if (responseReceived) {
                                break;
                            }
                        }

                        String response = responseReceived ? 
                            "Email sent and response received successfully!" :
                            "No response received after 3 attempts.";

                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                }
            });

            server.start();
            System.out.println("Server started on port 8080");
            System.out.println("Open http://localhost:8080 in your browser");

            // Keep the server running
            while (true) {
                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error starting the server: " + e.getMessage());
        } finally {
            if (server != null) {
                server.stop(0);
                System.out.println("Server stopped.");
            }
        }
    }

    private static Map<String, String> parseFormData(String formData) {
        Map<String, String> result = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1].replace("+", " "));
            }
        }
        return result;
    }

    // Method to generate email body with approval/rejection buttons
    private static String generateApprovalEmailBody(String bodyMessage, String senderEmail) {
        return "<html>" +
                "<body>" +
                "<h3>" + bodyMessage + "</h3>" +
                "<p style=\"text-align:center;\">" +
                "<a href=\"mailto:" + senderEmail +
                "?subject=Approval Response&body=Your%20changes%20are%20Approved%20by%20Client\" " +
                "style=\"padding:10px;background-color:green;color:white;text-decoration:none;border-radius:5px;display:inline-block;margin-right:20px;\">Approve</a>" +
                "<a href=\"mailto:" + senderEmail +
                "?subject=Approval Response&body=Your%20changes%20are%20Rejected%20by%20Client\" " +
                "style=\"padding:10px;background-color:red;color:white;text-decoration:none;border-radius:5px;display:inline-block;\">Reject</a>" +
                "</p>" +
                "</body>" +
                "</html>";
    }
}