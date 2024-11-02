package ua.lastbite.email_service.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.lastbite.email_service.dto.email.EmailRequest;
import ua.lastbite.email_service.dto.email.EmailVerificationRequest;
import ua.lastbite.email_service.dto.token.TokenValidationRequest;
import ua.lastbite.email_service.exception.*;
import ua.lastbite.email_service.exception.token.*;
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

        try {
            emailService.verifyEmail(request);
            LOGGER.info("Email verification successful for token: {}", request.getTokenValue());
            return ResponseEntity.ok("Email successfully verified");
        } catch (UserNotFoundException e) {
            LOGGER.error("User not found for token: {}", request.getTokenValue(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (InvalidTokenException e) {
            LOGGER.error("Invalid token provided: {}", request.getTokenValue(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
        } catch (TokenAlreadyUsedException e) {
            LOGGER.error("Token already used: {}", request.getTokenValue(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Token already used");
        } catch (TokenExpiredException e) {
            LOGGER.error("Token expired: {}", request.getTokenValue(), e);
            return ResponseEntity.status(HttpStatus.GONE).body("Token expired");
        } catch (ServiceUnavailableException e) {
            LOGGER.error("Service unavailable for email verification with token: {}", request.getTokenValue(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service is temporarily unavailable");
        } catch (Exception e) {
            LOGGER.error("Unexpected error during email verification for token: {}", request.getTokenValue(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
}
