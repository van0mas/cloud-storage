package org.example.cloudstorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {
    @NotBlank
    @Size(min = 5, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$")
    private String username;

    @NotBlank
    @Size(min = 5, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>[\\\\]/`~+=-_']*$")
    private String password;
}
