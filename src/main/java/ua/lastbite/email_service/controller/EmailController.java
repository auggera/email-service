package ua.lastbite.email_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.lastbite.email_service.dto.EmailRequest;
import ua.lastbite.email_service.dto.EmailVerificationRequest;
import ua.lastbite.email_service.service.EmailService;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    @Autowired
    EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        emailService.sendSimpleEmail(request);

        return ResponseEntity.ok("Email sent successfully");
    }

    @PostMapping("/send-verification")
    public ResponseEntity<String> sendVerificationEmail(@RequestBody EmailVerificationRequest request) {
        emailService.sendVerificationEmail(request);

        return ResponseEntity.ok("Email Verification sent successfully");
    }
}
