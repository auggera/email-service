package ua.lastbite.email_service.exception.token;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String tokenValue) {
        super("Token expired: " + tokenValue);
    }
}
