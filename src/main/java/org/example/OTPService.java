package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class OTPService {
    private final Map<String, Integer> otpCache = new HashMap<>();

    public boolean authenticateWithOTP(String senderEmail) {
        Scanner scanner = new Scanner(System.in);

        // Check if OTP has already been sent to the senderEmail
        if (!otpCache.containsKey(senderEmail)) {
            int otp = generateOTP(); // Generate a new OTP
            otpCache.put(senderEmail, otp);

            // Simulate sending OTP via Email
            EmailService emailService = new EmailService("aniketchourasiya1632000@gmail.com", "iieg irch mlpr akdv");
            emailService.sendEmail(senderEmail, "OTP for Authentication", "Your OTP is: " + otp);

            System.out.println("📧 OTP has been sent to your email.");
        }

        System.out.print("Enter the OTP sent to your email: ");
        int enteredOtp = scanner.nextInt();

        return validateOTP(senderEmail, enteredOtp);
    }

    private int generateOTP() {
        Random random = new Random();
        return 100000 + random.nextInt(900000); // Generate a 6-digit OTP
    }

    private boolean validateOTP(String senderEmail, int enteredOtp) {
        return otpCache.getOrDefault(senderEmail, -1) == enteredOtp;
    }
}
