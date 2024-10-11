package ua.lastbite.email_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ua.lastbite.email_service.dto.EmailRequest;
import ua.lastbite.email_service.dto.EmailVerificationRequest;

@Service
public class EmailService {


    private final JavaMailSender mailSender;

    @Value("${EMAIL_SERVICE_}")
    private String verificationBaseUrl;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendSimpleEmail(EmailRequest request) {
        LOGGER.info("Sending email to {}", request.getToEmail());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getToEmail());
        message.setSubject(request.getSubject());
        message.setText(request.getBody());
        mailSender.send(message);
        LOGGER.info("Email sent successfully to {}", request.getToEmail());
    }

    public void sendVerificationEmail(EmailVerificationRequest request) {
        LOGGER.info("Processing email verification request.");
        String subject = "Email Verification";
        String verificationUrl = verificationBaseUrl + "/api/users/verify-email?token=" + request.getToken();
        String body = "Please click the following link to verify your email: " + verificationUrl;

        sendSimpleEmail(new EmailRequest(request.getToEmail(), subject, body));
    }
}
