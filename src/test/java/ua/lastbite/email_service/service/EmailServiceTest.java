package ua.lastbite.email_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import ua.lastbite.email_service.dto.email.EmailRequest;
import ua.lastbite.email_service.dto.email.EmailVerificationRequest;
import ua.lastbite.email_service.dto.token.TokenRequest;
import ua.lastbite.email_service.dto.token.TokenValidationRequest;
import ua.lastbite.email_service.dto.token.TokenValidationResponse;
import ua.lastbite.email_service.dto.user.UserEmailResponseDto;
import ua.lastbite.email_service.exception.EmailSendingFailedException;
import ua.lastbite.email_service.exception.ServiceUnavailableException;
import ua.lastbite.email_service.exception.UserNotFoundException;
import ua.lastbite.email_service.exception.token.TokenAlreadyUsedException;
import ua.lastbite.email_service.exception.token.TokenExpiredException;
import ua.lastbite.email_service.exception.token.TokenGenerationException;
import ua.lastbite.email_service.exception.token.TokenNotFoundException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private TokenServiceClient tokenServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Value("${app.verification.base-url}")
    private String verificationBaseUrl;

    @Value("${app.verification.verify-url}")
    private String verificationVerifyUrl;

    private TokenValidationRequest tokenValidationRequest;
    private TokenValidationResponse tokenValidationResponse;
    private EmailVerificationRequest emailVerificationRequest;
    private UserEmailResponseDto userEmailResponseDto;
    private EmailRequest emailRequest;

    private static final Integer USER_ID = 1;
    private static final String TOKEN = "tokenValue123";
    private static final String EMAIL = "email@example.com";

    @BeforeEach
    void setUp() {
        tokenValidationRequest = new TokenValidationRequest(TOKEN);
        tokenValidationResponse = new TokenValidationResponse(true, USER_ID);
        emailVerificationRequest = new EmailVerificationRequest(USER_ID);
        userEmailResponseDto = new UserEmailResponseDto(EMAIL, false);
        emailRequest = new EmailRequest("recipient@example.com", "Test Subject", "Test Body");
    }

    @Test
    void testVerifyEmailSuccess() {

        Mockito.doReturn(tokenValidationResponse)
                .when(tokenServiceClient).verifyToken(tokenValidationRequest);

        emailService.verifyEmail(tokenValidationRequest);

        Mockito.verify(tokenServiceClient, Mockito.times(1)).verifyToken(tokenValidationRequest);
        Mockito.verify(userServiceClient, Mockito.times(1)).markEmailAsVerified(USER_ID);
    }

    @Test
    void testVerifyEmailTokenNotFound() {

        Mockito.when(tokenServiceClient.verifyToken(tokenValidationRequest))
                .thenThrow(new TokenNotFoundException(TOKEN));

        TokenNotFoundException exception = assertThrows(TokenNotFoundException.class, () -> emailService.verifyEmail(tokenValidationRequest));

        assertEquals("Token not found: " + tokenValidationRequest.getTokenValue(), exception.getMessage());

        Mockito.verify(tokenServiceClient, Mockito.times(1)).verifyToken(tokenValidationRequest);
        Mockito.verify(userServiceClient, Mockito.never()).markEmailAsVerified(USER_ID);
    }

    @Test
    void testVerifyEmailTokenExpired() {

        Mockito.when(tokenServiceClient.verifyToken(tokenValidationRequest))
                .thenThrow(new TokenExpiredException(TOKEN));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> emailService.verifyEmail(tokenValidationRequest));

        assertEquals("Token expired: " + tokenValidationRequest.getTokenValue(), exception.getMessage());

        Mockito.verify(tokenServiceClient, Mockito.times(1)).verifyToken(tokenValidationRequest);
        Mockito.verify(userServiceClient, Mockito.never()).markEmailAsVerified(USER_ID);
    }

    @Test
    void testVerifyEmailTokenAlreadyUsed() {

        Mockito.when(tokenServiceClient.verifyToken(tokenValidationRequest))
                .thenThrow(new TokenAlreadyUsedException(TOKEN));

        TokenAlreadyUsedException exception = assertThrows(TokenAlreadyUsedException.class, () -> emailService.verifyEmail(tokenValidationRequest));

        assertEquals("Token already used: " + tokenValidationRequest.getTokenValue(), exception.getMessage());

        Mockito.verify(tokenServiceClient, Mockito.times(1)).verifyToken(tokenValidationRequest);
        Mockito.verify(userServiceClient, Mockito.never()).markEmailAsVerified(USER_ID);
    }

    @Test
    void testVerifyEmailServiceUnavailable() {
        Mockito.when(tokenServiceClient.verifyToken(tokenValidationRequest))
                .thenThrow(ServiceUnavailableException.class);

        assertThrows(ServiceUnavailableException.class, () -> emailService.verifyEmail(tokenValidationRequest));

        Mockito.verify(tokenServiceClient, Mockito.times(1)).verifyToken(tokenValidationRequest);
        Mockito.verify(userServiceClient, Mockito.never()).markEmailAsVerified(USER_ID);
    }

    @Test
    void testVerifyEmailUserNotFound() {

        Mockito.when(tokenServiceClient.verifyToken(tokenValidationRequest))
                .thenReturn(tokenValidationResponse);

        Mockito.doThrow(new UserNotFoundException(USER_ID))
                .when(userServiceClient).markEmailAsVerified(USER_ID);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> emailService.verifyEmail(tokenValidationRequest));

        assertEquals("User with ID 1 not found", exception.getMessage());

        Mockito.verify(tokenServiceClient, Mockito.times(1)).verifyToken(tokenValidationRequest);
        Mockito.verify(userServiceClient, Mockito.times(1)).markEmailAsVerified(USER_ID);
    }

    @Test
    void testVerifyEmailFailedMarkAsVerified() {

        Mockito.when(tokenServiceClient.verifyToken(tokenValidationRequest))
                .thenReturn(tokenValidationResponse);

        Mockito.doThrow(ServiceUnavailableException.class)
                .when(userServiceClient).markEmailAsVerified(USER_ID);

        assertThrows(ServiceUnavailableException.class, () -> emailService.verifyEmail(tokenValidationRequest));

        Mockito.verify(tokenServiceClient, Mockito.times(1)).verifyToken(tokenValidationRequest);
        Mockito.verify(userServiceClient, Mockito.times(1)).markEmailAsVerified(USER_ID);
    }

    @Test
    void sendSimpleEmailSuccessfulSend() {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailRequest.getToEmail());
        message.setSubject(emailRequest.getSubject());
        message.setText(emailRequest.getBody());

        emailService.sendSimpleEmail(emailRequest);

        Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));
    }

    @Test
    void sendSimpleEmail_EmailSendingFailedException() {

        Mockito.doThrow(new MailException("Failed to send email") {}).when(mailSender)
                .send(Mockito.any(SimpleMailMessage.class));

        EmailSendingFailedException exception = assertThrows(EmailSendingFailedException.class,
                () -> emailService.sendSimpleEmail(emailRequest));

        assertEquals("Failed to send email to recipient@example.com", exception.getMessage());
    }

    @Test
    void sendVerificationEmailSuccess() {

    }

    @Test
    void sendVerificationEmail_Successful() {

        Mockito.when(userServiceClient.getEmailInfoByUserId(USER_ID)).thenReturn(userEmailResponseDto);
        Mockito.when(tokenServiceClient.generateToken(new TokenRequest(USER_ID))).thenReturn(TOKEN);

        String expectedVerificationUrl = verificationBaseUrl + verificationVerifyUrl + "?token=" + TOKEN;
        String expectedBody = "Please click the following link to verify your email: " + expectedVerificationUrl;

        emailService.sendVerificationEmail(emailVerificationRequest);

        Mockito.verify(userServiceClient, Mockito.times(1)).getEmailInfoByUserId(USER_ID);
        Mockito.verify(tokenServiceClient, Mockito.times(1)).generateToken(new TokenRequest(USER_ID));

        Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.argThat((SimpleMailMessage message) ->
                Objects.requireNonNull(message.getTo())[0].equals(EMAIL) &&
                        Objects.equals(message.getSubject(), "Email Verification") &&
                        Objects.equals(message.getText(), expectedBody)
        ));
    }

    @Test
    void sendVerificationEmail_TokenGenerationFailed() {

        Mockito.when(userServiceClient.getEmailInfoByUserId(USER_ID)).thenReturn(userEmailResponseDto);
        Mockito.doThrow(new TokenGenerationException("Failed to generate token")).when(tokenServiceClient).generateToken(Mockito.any());

        TokenGenerationException exception = assertThrows(TokenGenerationException.class,
                () -> emailService.sendVerificationEmail(emailVerificationRequest));

        assertEquals("Failed to generate token", exception.getMessage());
        Mockito.verify(userServiceClient, Mockito.times(1)).getEmailInfoByUserId(USER_ID);
        Mockito.verify(mailSender, Mockito.never()).send(Mockito.any(SimpleMailMessage.class));
    }
}
