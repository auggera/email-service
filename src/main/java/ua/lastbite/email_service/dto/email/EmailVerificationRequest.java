package ua.lastbite.email_service.dto.email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class EmailVerificationRequest {

    @NotBlank(message = "Email cannot be empty")
    private String toEmail;

    @NotBlank(message = "token cannot be empty")
    private String token;

    @NotNull(message = "User ID cannot be empty")
    private Integer userId;
}
