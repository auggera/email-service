package ua.lastbite.email_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmailRequest {

    @NotBlank
    private String toEmail;

    @NotBlank
    private String subject;

    @NotBlank
    private String body;
}
