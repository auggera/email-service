package ua.lastbite.email_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ua.lastbite.email_service.dto.token.TokenRequest;
import ua.lastbite.email_service.dto.token.TokenResponse;
import ua.lastbite.email_service.dto.token.TokenValidationRequest;
import ua.lastbite.email_service.dto.token.TokenValidationResponse;
import ua.lastbite.email_service.exception.ServiceUnavailableException;
import ua.lastbite.email_service.exception.token.TokenAlreadyUsedException;
import ua.lastbite.email_service.exception.token.TokenExpiredException;
import ua.lastbite.email_service.exception.token.TokenGenerationException;
import ua.lastbite.email_service.exception.token.TokenNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ActiveProfiles("test")
@SpringBootTest
public class TokenServiceClientTest {

    @MockBean
    RestTemplate restTemplate;

    @Autowired
    TokenServiceClient tokenServiceClient;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${token-service.url}")
    private String tokenServiceUrl;

    private MockRestServiceServer mockServer;
    private TokenRequest tokenRequest;
    private TokenResponse tokenResponse;
    private TokenValidationRequest tokenValidationRequest;
    private TokenValidationResponse expectedResponse;
    private String urlGenerateToken;
    private String urlValidateToken;

    @BeforeEach
    void setUp() {
        tokenRequest = new TokenRequest(1);
        tokenResponse = new TokenResponse("tokenValue123");
        tokenValidationRequest = new TokenValidationRequest("testToken123");
        expectedResponse = new TokenValidationResponse(true, 1);
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        urlGenerateToken = tokenServiceUrl + "/api/tokens/generate";
        urlValidateToken = tokenServiceUrl + "/api/tokens/validate";

    }

    @Test
    void testGenerateToken() throws JsonProcessingException {
        Mockito.when(restTemplate.postForObject(urlGenerateToken, tokenRequest, TokenResponse.class))
                .thenReturn(tokenResponse);

        final String expectedResponse = tokenResponse.getTokenValue();

        mockServer.expect(ExpectedCount.once(), requestTo(urlGenerateToken))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        String actualResponse = tokenServiceClient.generateToken(tokenRequest);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testGenerateTokenException() {

        Mockito.when(restTemplate.postForObject(urlGenerateToken, null, TokenResponse.class))
                .thenThrow(TokenGenerationException.class);

        TokenGenerationException exception = assertThrows(TokenGenerationException.class, () -> tokenServiceClient.generateToken(tokenRequest));

        assertEquals("Could not generate token", exception.getMessage());
    }

    @Test
    void testValidateTokenSuccessfully() {
        Mockito.when(restTemplate.postForObject(urlValidateToken, tokenValidationRequest, TokenValidationResponse.class)).thenReturn(expectedResponse);

        mockServer.expect(ExpectedCount.once(), requestTo(urlValidateToken))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"valid\":true, \"userId\":1}", MediaType.APPLICATION_JSON));

        TokenValidationResponse actualResponse = tokenServiceClient.verifyToken(tokenValidationRequest);

        assertNotNull(actualResponse, "Response should not be null");
        assertEquals(expectedResponse, actualResponse);
        assertTrue(actualResponse.isValid());
    }

    @Test
    void verifyTokenNotFound() {

        Mockito.when(restTemplate.postForObject(urlValidateToken, tokenValidationRequest, TokenValidationResponse.class))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Exception occurred", null, null, null));

        TokenNotFoundException exception = assertThrows(TokenNotFoundException.class, () -> tokenServiceClient.verifyToken(tokenValidationRequest));

        assertEquals(exception.getMessage(), "Token not found: " + tokenValidationRequest.getTokenValue());
    }

    @Test
    void verifyTokenExpired() {

        Mockito.when(restTemplate.postForObject(urlValidateToken, tokenValidationRequest, TokenValidationResponse.class))
                .thenThrow(HttpClientErrorException.Gone.create(HttpStatus.GONE, "Exception occurred", null, null, null));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> tokenServiceClient.verifyToken(tokenValidationRequest));

        assertEquals(exception.getMessage(), "Token expired: " + tokenValidationRequest.getTokenValue());
    }

    @Test
    void verifyTokenIsAlreadyUsed() {

        Mockito.when(restTemplate.postForObject(urlValidateToken, tokenValidationRequest, TokenValidationResponse.class))
                .thenThrow(HttpClientErrorException.Conflict.create(HttpStatus.CONFLICT, "Exception occurred", null, null, null));

        TokenAlreadyUsedException exception = assertThrows(TokenAlreadyUsedException.class, () -> tokenServiceClient.verifyToken(tokenValidationRequest));

        assertEquals(exception.getMessage(), "Token already used: " + tokenValidationRequest.getTokenValue());
    }

    @Test
    void verifyTokenServiceUnavailable() {

        Mockito.when(restTemplate.postForObject(urlValidateToken, tokenValidationRequest, TokenValidationResponse.class))
                .thenThrow(HttpServerErrorException.InternalServerError.class);

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class, () -> tokenServiceClient.verifyToken(tokenValidationRequest));

        assertEquals(exception.getMessage(), "Token service is currently unavailable");
    }

    @Test
    void verifyTokenRestClientException() {

        Mockito.when(restTemplate.postForObject(urlValidateToken, tokenValidationRequest, TokenValidationResponse.class))
                .thenThrow(RestClientException.class);

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class, () -> tokenServiceClient.verifyToken(tokenValidationRequest));

        assertEquals(exception.getMessage(), "Unexpected error during token validation");
    }
}
