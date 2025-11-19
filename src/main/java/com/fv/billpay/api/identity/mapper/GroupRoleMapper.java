package com.fv.billpay.api.identity.mapper;

import com.fv.billpay.api.identity.dto.response.GroupRoleResponseDto;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * Mapper para convertir RoleRepresentation a GroupRoleResponseDto
 */
public class GroupRoleMapper {
    
    public static GroupRoleResponseDto toResponseDto(RoleRepresentation role) {
        if (role == null) return null;
        
        GroupRoleResponseDto dto = new GroupRoleResponseDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        
        return dto;
    }
}
