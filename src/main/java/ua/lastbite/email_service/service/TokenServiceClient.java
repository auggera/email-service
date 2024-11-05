package ua.lastbite.email_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

import java.util.Optional;

@Service
public class TokenServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceClient.class);
    private final RestTemplate restTemplate;

    @Value("${token-service.url}")
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
                .orElseThrow(() -> new TokenGenerationException("Could not generate token"));
    }

    public TokenValidationResponse verifyToken(TokenValidationRequest request) {
        LOGGER.info("Validating token");
        String urlRequest = tokenServiceUrl + "/api/tokens/validate";

        try {
            return restTemplate.postForObject(urlRequest, request, TokenValidationResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.error("Token not found for request: {}", request.getTokenValue());
            throw new TokenNotFoundException(request.getTokenValue());
        } catch (HttpClientErrorException.Gone e) {
            LOGGER.error("Token expired for request: {}", request.getTokenValue());
            throw new TokenExpiredException(request.getTokenValue());
        } catch (HttpClientErrorException.Conflict e) {
            LOGGER.error("Token already used for request: {}", request.getTokenValue());
            throw new TokenAlreadyUsedException(request.getTokenValue());
        } catch (HttpServerErrorException e) {
            LOGGER.error("Service unavailable for token validation request: {}", request.getTokenValue(), e);
            throw new ServiceUnavailableException("Token service is currently unavailable");
        } catch (RestClientException e) {
            LOGGER.error("Unexpected error during token validation request: {}", request.getTokenValue(), e);
            throw new ServiceUnavailableException("Unexpected error during token validation");
        }
    }
}
