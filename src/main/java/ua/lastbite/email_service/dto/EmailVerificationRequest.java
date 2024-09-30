package ua.lastbite.email_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmailVerificationRequest {

    @NotBlank(message = "Email cannot be empty")
    private String toEmail;

    @NotBlank(message = "token cannot be empty")
    private String token;
}
