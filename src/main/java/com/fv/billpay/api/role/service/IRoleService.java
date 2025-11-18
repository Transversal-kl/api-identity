package com.fv.billpay.api.role.service;

import com.fv.billpay.api.role.dto.request.RoleRequestDto;
import com.fv.billpay.api.role.dto.response.RoleResponseDto;
import java.util.List;

public interface IRoleService {
    RoleResponseDto create(RoleRequestDto dto);
    RoleResponseDto update(String roleName, RoleRequestDto dto);
    boolean delete(String roleName);
    RoleResponseDto getById(String roleName);
    List<RoleResponseDto> getAll(int page, int size);
    long count();
}
