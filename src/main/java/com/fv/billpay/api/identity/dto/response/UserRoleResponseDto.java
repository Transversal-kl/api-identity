package com.fv.billpay.api.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para roles de un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleResponseDto {
    private String name;
    private String description;
}
