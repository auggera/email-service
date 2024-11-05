package ua.lastbite.email_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ua.lastbite.email_service.dto.email.EmailRequest;
import ua.lastbite.email_service.dto.email.EmailVerificationRequest;
import ua.lastbite.email_service.dto.token.TokenRequest;
import ua.lastbite.email_service.dto.token.TokenValidationRequest;
import ua.lastbite.email_service.exception.token.TokenAlreadyUsedException;
import ua.lastbite.email_service.exception.token.TokenExpiredException;
import ua.lastbite.email_service.exception.token.TokenGenerationException;
import ua.lastbite.email_service.exception.token.TokenNotFoundException;
import ua.lastbite.email_service.service.EmailService;
import ua.lastbite.email_service.exception.*;

import java.util.concurrent.CompletableFuture;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@WebMvcTest(EmailController.class)
public class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailService emailService;

    private static final String TOKEN = "tokenValue123";
    private static final Integer USER_ID = 1;
    private static final String EMAIL = "email@example.com";
    private EmailRequest emailRequest;
    private EmailVerificationRequest emailVerificationRequest;
    private TokenValidationRequest tokenValidationRequest;
    private TokenRequest tokenRequest;

    @BeforeEach
    void setUp() {
        tokenValidationRequest = new TokenValidationRequest(TOKEN);
        emailRequest = new EmailRequest(EMAIL, "Test Subject", "Test Body");
        emailVerificationRequest = new EmailVerificationRequest(USER_ID);
        tokenRequest = new TokenRequest(USER_ID);
    }

    @Test
    void testVerifyEmailSuccess() throws Exception {

        Mockito.doNothing().when(emailService).verifyEmail(tokenValidationRequest);

        mockMvc.perform(post("/api/emails/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email successfully verified"));
    }
    @Test
    void testVerifyEmail_UserNotFound() throws Exception {

        Mockito.doThrow(new UserNotFoundException(USER_ID)).when(emailService).verifyEmail(tokenValidationRequest);

        mockMvc.perform(post("/api/emails/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

    @Test
    void testVerifyEmailTokenNotFound() throws Exception {

        Mockito.doThrow(new TokenNotFoundException(TOKEN)).when(emailService).verifyEmail(tokenValidationRequest);

        mockMvc.perform(post("/api/emails/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Token not found: " + TOKEN));
    }

    @Test
    void testVerifyEmailTokenExpired() throws Exception {

        Mockito.doThrow(new TokenExpiredException(TOKEN)).when(emailService).verifyEmail(tokenValidationRequest);

        mockMvc.perform(post("/api/emails/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isGone())
                .andExpect(content().string("Token expired: " + TOKEN));
    }

    @Test
    void testVerifyEmailTokenAlreadyUsed() throws Exception {

        Mockito.doThrow(new TokenAlreadyUsedException(TOKEN)).when(emailService).verifyEmail(tokenValidationRequest);

        mockMvc.perform(post("/api/emails/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Token already used: " + TOKEN));
    }

    @Test
    void testVerifyEmailServiceUnavailable() throws Exception {

        Mockito.doThrow(new ServiceUnavailableException("Service is currently unavailable")).when(emailService).verifyEmail(tokenValidationRequest);

        mockMvc.perform(post("/api/emails/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("Service is currently unavailable"));
    }

    @Test
    void sendEmailSuccessful() throws Exception {

        Mockito.doReturn(CompletableFuture.completedFuture(null))
                .when(emailService).sendSimpleEmail(emailRequest);

        mockMvc.perform(post("/api/emails/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email sent successfully"));
    }

    @Test
    void sendEmailSendingFailedException() throws Exception {

        CompletableFuture<Void> failedFuture = CompletableFuture.failedFuture(new EmailSendingFailedException(EMAIL));

        Mockito.when(emailService.sendSimpleEmail(emailRequest)).thenReturn(failedFuture);

        mockMvc.perform(post("/api/emails/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to send email to " + EMAIL));
    }

    @Test
    void sendVerificationEmailSuccess() throws Exception {

        Mockito.doNothing().when(emailService).sendVerificationEmail(emailVerificationRequest);

        mockMvc.perform(post("/api/emails/send-verification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emailVerificationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email Verification sent successfully"));
    }

    @Test
    void sendVerificationEmailAlreadyVerified() throws Exception {

        Mockito.doThrow(new EmailAlreadyVerifiedException())
                .when(emailService).sendVerificationEmail(emailVerificationRequest);

        mockMvc.perform(post("/api/emails/send-verification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emailVerificationRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already verified"));
    }

    @Test
    void sendVerificationEmailTokenGenerationFailed() throws Exception {

        Mockito.doThrow(new TokenGenerationException("Could not generate token"))
                .when(emailService).sendVerificationEmail(emailVerificationRequest);

        mockMvc.perform(post("/api/emails/send-verification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Could not generate token"));
    }
}
