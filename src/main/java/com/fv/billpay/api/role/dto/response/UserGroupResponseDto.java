package com.fv.billpay.api.role.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para grupos de un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupResponseDto {
    private String id;
    private String name;
    private String path;
}
