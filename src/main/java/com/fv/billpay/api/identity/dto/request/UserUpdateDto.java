package com.fv.billpay.api.identity.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la actualización de usuarios.
 * Todos los campos son opcionales excepto aquellos que se validan cuando están presentes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    @Size(min = 3, max = 255, message = "El username debe tener entre 3 y 255 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El username solo puede contener letras, números, puntos, guiones y guiones bajos")
    private String username;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;

    @Size(max = 25, message = "El nombre no puede exceder 25 caracteres")
    private String firstName;

    @Size(max = 25, message = "El apellido no puede exceder 25 caracteres")
    private String lastName;

    private Boolean enabled;
}
