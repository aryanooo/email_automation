package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OTPService {
    private static final String SENDER_EMAIL = "aniketchourasiya1632000@gmail.com";
    private static final String SENDER_PASSWORD = "iieg irch mlpr akdv";
    private Map<String, String> otpStore = new HashMap<>();

    public boolean sendOTP(String email) {
        try {
            String otp = generateOTP();
            otpStore.put(email, otp);

            EmailService tempEmailService = new EmailService(SENDER_EMAIL, SENDER_PASSWORD);
            String subject = "Your OTP for Email Approval System";
            String message = "<html><body>" +
                    "<h2>Email Approval System - OTP Verification</h2>" +
                    "<p>Your OTP is: <b style='font-size: 24px;'>" + otp + "</b></p>" +
                    "<p>This OTP will expire in 5 minutes.</p>" +
                    "</body></html>";

            boolean emailSent = tempEmailService.sendEmail(email, subject, message);
            if (!emailSent) {
                System.out.println(" if (!emailSent) is false hence red txt");
                otpStore.remove(email);  // Remove OTP if email failed
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyOTP(String email, String userInputOTP) {
        String storedOTP = otpStore.get(email);
        if (!otpStore.isEmpty()) {  // Check if the map is not empty
            otpStore.forEach((key, value) -> {
                System.out.println("--Email: " + key + ", OTP: " + value);
            });
        } else {
            System.out.println("The OTP store is empty.");
        }
        System.out.println("--Verifying OTP for email: " + email);
        System.out.println("--User input OTP: " + userInputOTP + " | Stored OTP: " + storedOTP); // Debugging
        if (storedOTP != null && storedOTP.equals(userInputOTP)) {
            otpStore.remove(email); // Remove used OTP
            return true;
        }
        return false;
    }

    private String generateOTP() {
        Random random = new Random();
        int rand = random.nextInt(1000000);
        System.out.println("Otp is = "+rand);
        return String.format("%06d", rand);
    }

    // Getter for sender email (for display purposes)
    public static String getSenderEmail() {
        return SENDER_EMAIL;
    }
}
