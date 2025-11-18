package com.fv.billpay.api.role.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleRequestDto {
    // name es requerido solo para CREATE, en UPDATE se usa el parámetro del path
    @NotNull(message = "El nombre es obligatorio", groups = CreateValidation.class)
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String description;

    // Interfaces para grupos de validación
    public interface CreateValidation {}
    public interface UpdateValidation {}
}
