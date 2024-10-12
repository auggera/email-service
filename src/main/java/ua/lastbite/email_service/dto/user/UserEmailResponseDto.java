package ua.lastbite.email_service.dto.user;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class UserEmailResponseDto {
    private String email;
    private boolean verified;
}
