package ua.lastbite.email_service.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.lastbite.email_service.dto.email.EmailRequest;
import ua.lastbite.email_service.dto.email.EmailVerificationRequest;
import ua.lastbite.email_service.dto.token.TokenValidationRequest;
import ua.lastbite.email_service.service.EmailService;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailController.class);
    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        LOGGER.info("Received request to send email to: {}", request.getToEmail());
        emailService.sendSimpleEmail(request);

        return ResponseEntity.ok("Email sent successfully");
    }

    @PostMapping("/send-verification")
    public ResponseEntity<String> sendVerificationEmail(@RequestBody EmailVerificationRequest request) {
        LOGGER.info("Received an email verification request for user ID: {}", request.getUserId());
        emailService.sendVerificationEmail(request);

        return ResponseEntity.ok("Email Verification sent successfully");
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody TokenValidationRequest request) {
        LOGGER.info("Received request to verify email for token: {}", request.getTokenValue());
        emailService.verifyEmail(request);
        LOGGER.info("Email verification successful for token: {}", request.getTokenValue());
        return ResponseEntity.ok("Email successfully verified");
    }
}
