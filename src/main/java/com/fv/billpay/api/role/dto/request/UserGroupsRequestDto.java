package com.fv.billpay.api.role.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para asignar/desasignar grupos a un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupsRequestDto {
    
    @NotEmpty(message = "La lista de IDs de grupos no puede estar vacía")
    @Size(min = 1, max = 50, message = "Debe proporcionar entre 1 y 50 grupos")
    private List<String> groupIds;
}
