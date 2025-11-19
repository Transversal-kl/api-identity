package com.fv.billpay.api.role.mapper;

import com.fv.billpay.api.role.dto.response.GroupResponseDto;
import org.keycloak.representations.idm.GroupRepresentation;

public class GroupMapper {
    public static GroupResponseDto toResponseDto(GroupRepresentation group) {
        if (group == null) return null;
        
        GroupResponseDto dto = new GroupResponseDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        
        // Admin Client 26.x tiene campo description directo
        String description = group.getDescription();
        
        // Fallback a attributes si description directo es null
        if (description == null && group.getAttributes() != null && group.getAttributes().containsKey("description")) {
            var descriptions = group.getAttributes().get("description");
            if (descriptions != null && !descriptions.isEmpty()) {
                description = descriptions.get(0);
            }
        }
        
        dto.setDescription(description);
        return dto;
    }
}
