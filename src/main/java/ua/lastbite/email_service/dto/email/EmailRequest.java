package ua.lastbite.email_service.dto.email;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class EmailRequest {

    @NotBlank(message = "Email cannot be empty")
    private String toEmail;

    @NotBlank(message = "Subject cannot be empty")
    private String subject;

    @NotBlank(message = "Body cannot be empty")
    private String body;
}
