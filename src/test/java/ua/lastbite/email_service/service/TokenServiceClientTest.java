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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ua.lastbite.email_service.dto.token.TokenRequest;
import ua.lastbite.email_service.dto.token.TokenResponse;
import ua.lastbite.email_service.exception.token.TokenGenerationException;

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
    private String urlRequest;

    @BeforeEach
    void setUp() {
        tokenRequest = new TokenRequest(1);
        tokenResponse = new TokenResponse("tokenValue123");
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        urlRequest = tokenServiceUrl + "/api/tokens/generate";
    }

    @Test
    void testGenerateToken() throws JsonProcessingException {
        Mockito.when(restTemplate.postForObject(urlRequest, tokenRequest, TokenResponse.class))
                .thenReturn(tokenResponse);

        final String expectedResponse = tokenResponse.getTokenValue();

        mockServer.expect(ExpectedCount.once(), requestTo(urlRequest))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        String actualResponse = tokenServiceClient.generateToken(tokenRequest);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testGenerateTokenException() {

        Mockito.when(restTemplate.postForObject(urlRequest, null, TokenResponse.class))
                .thenThrow(TokenGenerationException.class);

        TokenGenerationException exception = assertThrows(TokenGenerationException.class, () -> tokenServiceClient.generateToken(tokenRequest));

        assertEquals("Could not generate token", exception.getMessage());
    }
}
