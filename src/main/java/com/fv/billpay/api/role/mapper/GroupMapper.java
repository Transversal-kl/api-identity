package com.fv.billpay.api.role.mapper;

import com.fv.billpay.api.role.dto.response.GroupResponseDto;
import org.keycloak.representations.idm.GroupRepresentation;

public class GroupMapper {
    public static GroupResponseDto toResponseDto(GroupRepresentation group) {
        if (group == null) return null;
        
        GroupResponseDto dto = new GroupResponseDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        
        // GroupRepresentation usa attributes para la descripción
        if (group.getAttributes() != null && group.getAttributes().containsKey("description")) {
            var descriptions = group.getAttributes().get("description");
            if (descriptions != null && !descriptions.isEmpty()) {
                dto.setDescription(descriptions.get(0));
            }
        }
        
        return dto;
    }
}
