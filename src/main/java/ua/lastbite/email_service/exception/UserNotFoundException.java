package ua.lastbite.email_service.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(int id) {
        super("User with ID " + id + " not found");
    }
}
