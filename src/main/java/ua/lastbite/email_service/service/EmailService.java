package ua.lastbite.email_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ua.lastbite.email_service.dto.email.EmailRequest;
import ua.lastbite.email_service.dto.email.EmailVerificationRequest;
import ua.lastbite.email_service.dto.token.TokenRequest;
import ua.lastbite.email_service.dto.user.UserEmailResponseDto;
import ua.lastbite.email_service.exception.EmailAlreadyVerifiedException;
import ua.lastbite.email_service.exception.EmailSendingFailedException;

@Service
public class EmailService {


    private final JavaMailSender mailSender;
    private final TokenServiceClient tokenServiceClient;
    private final UserServiceClient userServiceClient;

    @Value("${app.verification.base-url}")
    private String verificationBaseUrl;

    @Value("${app.verification.verify-url}")
    private String verificationVerifyUrl;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    public EmailService(JavaMailSender mailSender, TokenServiceClient tokenServiceClient, UserServiceClient userServiceClient) {
        this.mailSender = mailSender;
        this.tokenServiceClient = tokenServiceClient;
        this.userServiceClient = userServiceClient;
    }

    @Async
    public void sendSimpleEmail(EmailRequest request) {
        try {
            LOGGER.info("Sending email to {}", request.getToEmail());
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getToEmail());
            message.setSubject(request.getSubject());
            message.setText(request.getBody());
            mailSender.send(message);
            LOGGER.info("Email sent successfully to {}", request.getToEmail());
        } catch (MailException ex) {
            LOGGER.error("Failed to send email to {}: {}", request.getToEmail(), ex.getMessage());
            throw new EmailSendingFailedException("Failed to send email to " + request.getToEmail());
        }
    }

    public void sendVerificationEmail(EmailVerificationRequest request) {
        LOGGER.info("Processing email verification request.");

        UserEmailResponseDto responseDto = userServiceClient.getEmailInfoByUserId(request.getUserId());
        if (responseDto.isVerified()) {
            throw new EmailAlreadyVerifiedException();
        }

        LOGGER.info("Request generating token for user ID: {}", request.getUserId());
        String tokenValue = tokenServiceClient.generateToken(new TokenRequest(request.getUserId()));
        LOGGER.info("Successfully generated token: {}", tokenValue);

        String subject = "Email Verification";
        String verificationUrl = verificationBaseUrl + verificationVerifyUrl + "?token=" + tokenValue;
        String body = "Please click the following link to verify your email: " + verificationUrl;

        sendSimpleEmail(new EmailRequest(request.getToEmail(), subject, body));
    }
}
