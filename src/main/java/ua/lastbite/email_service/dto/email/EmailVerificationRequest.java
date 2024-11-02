package ua.lastbite.email_service.dto.email;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class EmailVerificationRequest {

    @NotNull(message = "User ID cannot be empty")
    private Integer userId;
}
