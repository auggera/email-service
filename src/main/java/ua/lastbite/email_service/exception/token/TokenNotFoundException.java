package ua.lastbite.email_service.exception.token;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(String tokenValue) {
        super("Token not found: " + tokenValue);
    }
}
