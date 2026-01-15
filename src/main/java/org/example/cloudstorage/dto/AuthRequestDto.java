package org.example.cloudstorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import static org.example.cloudstorage.config.AppConstants.Validation.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {

    @NotBlank(message = FIELD_REQUIRED)
    @Size(min = USERNAME_MIN, max = USERNAME_MAX, message = USERNAME_SIZE_MSG)
    @Pattern(regexp = USERNAME_REGEXP, message = USERNAME_PATTERN_MSG)
    private String username;

    @NotBlank(message = FIELD_REQUIRED)
    @Size(min = PASSWORD_MIN, max = PASSWORD_MAX, message = PASSWORD_SIZE_MSG)
    @Pattern(regexp = PASSWORD_REGEXP, message = PASSWORD_PATTERN_MSG)
    private String password;
}
