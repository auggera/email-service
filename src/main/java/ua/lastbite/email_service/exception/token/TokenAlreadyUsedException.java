package ua.lastbite.email_service.exception.token;

public class TokenAlreadyUsedException extends RuntimeException {
    public TokenAlreadyUsedException(String tokenValue) {
        super("Token already used: " + tokenValue);
    }
}
