package ua.lastbite.email_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Email Verification";
        String verificationUrl = "http://localhost:8080/api/users/verify-email?token=" + token;
        String body = "Please click the following link to verify your email: " + verificationUrl;

        sendSimpleEmail(toEmail, subject, body);
    }
}
