package ua.lastbite.email_service.exception;

public class EmailSendingFailedException extends RuntimeException {
    public EmailSendingFailedException(String toEmail) {
        super("Failed to send email to " + toEmail);
    }
}
