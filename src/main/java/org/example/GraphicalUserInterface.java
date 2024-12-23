package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.example.GmailReader.ApprovalResponse;

public class GraphicalUserInterface extends JFrame implements ActionListener {
    private static final String SENDER_EMAIL = "aniketchourasiya1632000@gmail.com";
    private static final String SENDER_PASSWORD = "iieg irch mlpr akdv";

    private JTextField otpField;
    private JButton verifyOTPButton;
    private JButton generateOTPButton;
    private JLabel statusLabel;
    private final OTPService otpService;
    private final EmailService emailService;
    private final GmailReader gmailReader;

    public GraphicalUserInterface() {
        this.otpService = new OTPService();
        this.emailService = new EmailService(SENDER_EMAIL, SENDER_PASSWORD);
        this.gmailReader = new GmailReader(SENDER_EMAIL, SENDER_PASSWORD);
        showOTPWindow();
    }

    private void showOTPWindow() {
        setTitle("Email Authentication");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel emailLabel = new JLabel("Sender Email:");
        JTextField emailDisplay = new JTextField(SENDER_EMAIL);
        emailDisplay.setEditable(false);
        emailDisplay.setMaximumSize(new Dimension(350, 30));

        JLabel passwordLabel = new JLabel("App Password:");
        JTextField passwordDisplay = new JTextField("********");
        passwordDisplay.setEditable(false);
        passwordDisplay.setMaximumSize(new Dimension(350, 30));

        generateOTPButton = new JButton("Generate OTP");
        generateOTPButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        generateOTPButton.addActionListener(e -> handleOTPGeneration());

        statusLabel = new JLabel("Click 'Generate OTP' to start verification");
        statusLabel.setForeground(Color.BLUE);

        JLabel instructionLabel = new JLabel("Enter OTP:");
        otpField = new JTextField(6);
        otpField.setMaximumSize(new Dimension(150, 30));
        otpField.setFont(new Font("Monospaced", Font.PLAIN, 20));
        otpField.setHorizontalAlignment(JTextField.CENTER);

        verifyOTPButton = new JButton("Verify OTP");
        verifyOTPButton.addActionListener(this);

        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        otpField.setAlignmentX(Component.CENTER_ALIGNMENT);
        verifyOTPButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(emailLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(emailDisplay);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        mainPanel.add(passwordLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(passwordDisplay);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(generateOTPButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        mainPanel.add(instructionLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(otpField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(verifyOTPButton);

        add(mainPanel);
        setVisible(true);
    }

    private void handleOTPGeneration() {
        generateOTPButton.setEnabled(false);
        statusLabel.setText("Sending OTP...");
        statusLabel.setForeground(Color.BLUE);

        new Thread(() -> {
            if (!emailService.testConnection()) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Failed to connect to email service. Check credentials.");
                    statusLabel.setForeground(Color.RED);
                    generateOTPButton.setEnabled(true);
                });
                return;
            }

            boolean otpSent = otpService.sendOTP(SENDER_EMAIL);

            SwingUtilities.invokeLater(() -> {
                if (otpSent) {
                    statusLabel.setText("OTP sent successfully! Check your email.");
                    statusLabel.setForeground(Color.GREEN);
                } else {
                    statusLabel.setText("Failed to send OTP. Try again.");
                    statusLabel.setForeground(Color.RED);
                    generateOTPButton.setEnabled(true);
                }
            });
        }).start();
    }

    private void showEmailCompositionWindow() {
        setTitle("Compose Approval Email");
        getContentPane().removeAll();
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField recipientField = new JTextField(20);
        JTextField subjectField = new JTextField(20);
        JTextArea bodyArea = new JTextArea(5, 20);
        JButton sendButton = new JButton("Send Approval Request");

        Dimension fieldSize = new Dimension(350, 30);
        recipientField.setMaximumSize(fieldSize);
        subjectField.setMaximumSize(fieldSize);
        bodyArea.setMaximumSize(new Dimension(350, 100));

        panel.add(new JLabel("Recipient Email:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(recipientField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(new JLabel("Subject:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(subjectField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(new JLabel("Message:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(new JScrollPane(bodyArea));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(sendButton);

        sendButton.addActionListener(e -> {
            String recipient = recipientField.getText();
            String subject = subjectField.getText();
            String message = bodyArea.getText();

            if (recipient.isEmpty() || subject.isEmpty() || message.isEmpty()) {
                showError("Please fill in all fields");
                return;
            }

            showProgressWindow(recipient, subject, message);
        });

        getContentPane().add(panel);
        revalidate();
        repaint();
    }

    private void showProgressWindow(String recipient, String subject, String message) {
        setTitle("Approval Progress");
        getContentPane().removeAll();
        setSize(450, 400);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel statusLabel = new JLabel("Waiting for response...");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.BLUE);
        panel.add(statusLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JTextArea progressArea = new JTextArea(15, 40);
        progressArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(progressArea);
        panel.add(scrollPane);

        getContentPane().add(panel);
        revalidate();
        repaint();

        new Thread(() -> {
            String htmlContent = generateApprovalEmailBody(message, SENDER_EMAIL);
            int maxAttempts = 3;
            boolean responseReceived = false;

            for (int attempt = 1; attempt <= maxAttempts && !responseReceived; attempt++) {
                updateProgress(progressArea, "\n🔄 Attempt " + attempt + " to send email...");
                emailService.sendEmail(recipient, subject, htmlContent);

                int finalAttempt = attempt;
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Checking for response (Attempt " + finalAttempt + "/" + maxAttempts + ")"));

                updateProgress(progressArea, "⏳ Waiting for recipient's response...");
                try {
                    Thread.sleep(5000);
                    System.out.println("\n--- Starting attempt " + attempt + " to check for response ---");

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Checking email for response...");
                        statusLabel.setForeground(Color.BLUE);
                    });

                    ApprovalResponse response = gmailReader.checkForApprovalResponseDetailed(recipient);
                    System.out.println("Response received: " + response.isReceived());
                    System.out.println("Response message: " + response.getMessage());

                    if (response.isReceived()) {
                        responseReceived = true;

                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText(response.getMessage());
                            statusLabel.setForeground(response.isApproved() ? Color.GREEN : Color.RED);

                            updateProgress(progressArea, "\n" + response.getMessage());
                            updateProgress(progressArea, "\n✨ Process completed successfully!");


                            JButton doneButton = new JButton("Done");
                            doneButton.addActionListener(e -> dispose());
                            panel.add(doneButton);
                            panel.revalidate();
                            panel.repaint();
                        });
                        break;
                    } else {
                        System.out.println("No response detected in attempt " + attempt);
                        int finalAttempt1 = attempt;
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("No response detected (Attempt " + finalAttempt1 + "/" + maxAttempts + ")");
                            statusLabel.setForeground(Color.ORANGE);
                        });
                        updateProgress(progressArea, "\nNo response detected yet...");
                    }
                } catch (InterruptedException e) {
                    System.err.println("Error during wait period: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Error occurred while checking!");
                        statusLabel.setForeground(Color.RED);
                    });
                    updateProgress(progressArea, "⚠️ Error while waiting: " + e.getMessage());
                }
            }

            if (!responseReceived) {
                System.out.println("All attempts completed without response");
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("No response received after all attempts");
                    statusLabel.setForeground(Color.RED);
                    updateProgress(progressArea, "\n❌ No response received after " + maxAttempts + " attempts.");

                    JButton retryButton = new JButton("Retry Approval Process");
                    retryButton.addActionListener(e -> showProgressWindow(recipient, subject, message));
                    panel.add(Box.createRigidArea(new Dimension(0, 10)));
                    panel.add(retryButton);
                    panel.revalidate();
                    panel.repaint();
                });
            }
        }).start();
    }

    private void updateProgress(JTextArea area, String message) {
        SwingUtilities.invokeLater(() -> {
            area.append(message + "\n");
            area.setCaretPosition(area.getDocument().getLength());
        });
    }

    private String generateApprovalEmailBody(String bodyMessage, String senderEmail) {
        return "<html>" +
                "<body>" +
                "<h3>" + bodyMessage + "</h3>" +
                "<p style=\"text-align:center;\">" +
                "<a href=\"mailto:" + senderEmail +
                "?subject=Response&body=Your%20changes%20are%20Approved%20by%20Client\" " +
                "style=\"padding:10px;background-color:green;color:white;text-decoration:none;border-radius:5px;display:inline-block;margin-right:20px;\">Approve</a>" +
                "<a href=\"mailto:" + senderEmail +
                "?subject=Response&body=Your%20changes%20are%20Rejected%20by%20Client\" " +
                "style=\"padding:10px;background-color:red;color:white;text-decoration:none;border-radius:5px;display:inline-block;\">Reject</a>" +
                "</p>" +
                "</body>" +
                "</html>";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == verifyOTPButton) {
            String userInputOTP = otpField.getText().trim();
            System.out.println("Verifying OTP: " + userInputOTP);

            if (otpService.verifyOTP(SENDER_EMAIL, userInputOTP)) {
                statusLabel.setText("OTP verified successfully!");
                statusLabel.setForeground(Color.GREEN);
                showEmailCompositionWindow();
            } else {
                statusLabel.setText("Invalid OTP. Please try again.");
                statusLabel.setForeground(Color.RED);
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(Color.LIGHT_GRAY);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new GraphicalUserInterface();
        });
    }
}