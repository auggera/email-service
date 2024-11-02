package ua.lastbite.email_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import ua.lastbite.email_service.dto.token.TokenValidationRequest;
import ua.lastbite.email_service.dto.token.TokenValidationResponse;
import ua.lastbite.email_service.exception.ServiceUnavailableException;
import ua.lastbite.email_service.exception.UserNotFoundException;
import ua.lastbite.email_service.exception.token.TokenAlreadyUsedException;
import ua.lastbite.email_service.exception.token.TokenExpiredException;
import ua.lastbite.email_service.exception.token.TokenNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private TokenServiceClient tokenServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private EmailService emailService;

    private TokenValidationRequest tokenValidationRequest;
    private TokenValidationResponse tokenValidationResponse;

    private static final Integer USER_ID = 1;
    private static final String TOKEN = "tokenValue123";

    @BeforeEach
    void setUp() {
        tokenValidationRequest = new TokenValidationRequest(TOKEN);
        tokenValidationResponse = new TokenValidationResponse(true, USER_ID);
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
}
