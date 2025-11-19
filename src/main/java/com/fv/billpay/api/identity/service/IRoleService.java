package com.fv.billpay.api.identity.service;

import com.fv.billpay.api.identity.dto.request.RoleRequestDto;
import com.fv.billpay.api.identity.dto.response.PagedResponse;
import com.fv.billpay.api.identity.dto.response.RoleResponseDto;

public interface IRoleService {
    RoleResponseDto create(RoleRequestDto dto);
    RoleResponseDto update(String roleName, RoleRequestDto dto);
    boolean delete(String roleName);
    RoleResponseDto getById(String roleName);
    PagedResponse<RoleResponseDto> getAll(int page, int size);
}
