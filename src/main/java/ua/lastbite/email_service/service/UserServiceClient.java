package ua.lastbite.email_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;

import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.lastbite.email_service.dto.user.UserEmailResponseDto;
import ua.lastbite.email_service.exception.ServiceUnavailableException;
import ua.lastbite.email_service.exception.UserNotFoundException;

@Service
public class UserServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceClient.class);


    private final RestTemplate restTemplate;

    @Value("${token-service.url}")
    private String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserEmailResponseDto getEmailInfoByUserId(Integer id) {
        String url = userServiceUrl + "/api/email/" + id + "/info";

        try {
            LOGGER.info("Requesting info from user with ID: {}", id);
            UserEmailResponseDto responseDto =  restTemplate.getForObject(url, UserEmailResponseDto.class);

            if (responseDto == null) {
                LOGGER.error("Empty response received from user-service for ID: {}", id);
                throw new ServiceUnavailableException("Received empty response from user-service");
            }

            LOGGER.debug("User data retrieved: {}", responseDto);
            return responseDto;
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.error("User not found with ID: {}", id);
            throw new UserNotFoundException(id);
        } catch (RestClientException e) {
            LOGGER.error("Error occurred while calling user-service for ID: {}", id, e);
            throw new ServiceUnavailableException("Failed to communicate with user-service");
        }
    }
}
