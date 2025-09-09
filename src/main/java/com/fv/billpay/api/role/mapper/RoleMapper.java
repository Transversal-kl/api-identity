package com.fv.billpay.api.role.mapper;

import com.fv.billpay.api.role.dto.response.RoleResponseDto;

public class RoleMapper {
    public static RoleResponseDto toResponseDto(String name, String description) {
        RoleResponseDto dto = new RoleResponseDto();
        dto.setName(name);
        dto.setDescription(description);
        return dto;
    }
}
