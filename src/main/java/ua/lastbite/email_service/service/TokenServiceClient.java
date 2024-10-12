package ua.lastbite.email_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.lastbite.email_service.dto.token.TokenRequest;
import ua.lastbite.email_service.dto.token.TokenResponse;

import java.util.Optional;

@Service
public class TokenServiceClient {

    private final RestTemplate restTemplate;

    @Value("${TOKEN_SERVICE_URL}")
    private String tokenServiceUrl;

    @Autowired
    public TokenServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateToken(TokenRequest request) {
        String tokenUrl = tokenServiceUrl + "/api/tokens/generate";

        TokenResponse response = restTemplate.postForObject(tokenUrl, request, TokenResponse.class);

        return Optional.ofNullable(response)
                .map(TokenResponse::getTokenValue)
                .orElseThrow(() -> new RuntimeException("Could not generate token"));
    }
}
