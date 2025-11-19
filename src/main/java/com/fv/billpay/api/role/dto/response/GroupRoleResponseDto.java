package com.fv.billpay.api.role.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para roles de un grupo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupRoleResponseDto {
    private String id;
    private String name;
    private String description;
}
