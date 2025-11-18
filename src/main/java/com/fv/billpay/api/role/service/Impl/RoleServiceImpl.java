package com.fv.billpay.api.role.service.Impl;

import com.fv.billpay.api.role.dto.request.RoleRequestDto;
import com.fv.billpay.api.role.dto.response.RoleResponseDto;
import com.fv.billpay.api.role.mapper.RoleMapper;
import com.fv.billpay.api.role.repository.IRoleRepository;
import com.fv.billpay.api.role.service.IRoleService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleServiceImpl implements IRoleService {
    @Inject
    IRoleRepository repository;

    @Override
    public RoleResponseDto create(RoleRequestDto dto) {
        repository.createRole(dto.getName(), dto.getDescription());
        return RoleMapper.toResponseDto(dto.getName(), dto.getDescription());
    }

    @Override
    public RoleResponseDto update(RoleRequestDto dto) {
        // En Keycloak, los roles se identifican por nombre, no por id
        repository.updateRole(dto.getName(), dto.getDescription());
        return RoleMapper.toResponseDto(dto.getName(), dto.getDescription());
    }

    @Override
    public boolean delete(String roleName) {
        if (roleName == null || roleName.isBlank()) return false;
        return repository.deleteRole(roleName);
    }

    @Override
    public RoleResponseDto getById(String roleName) {
        if (roleName == null || roleName.isBlank()) return null;
        return repository.getRole(roleName)
            .map(role -> RoleMapper.toResponseDto(role.getName(), role.getDescription()))
            .orElse(null);
    }

    @Override
    public List<RoleResponseDto> getAll(int page, int size) {
        int offset = page * size;
        return repository.getAllRoles(offset, size).stream()
                .map(role -> RoleMapper.toResponseDto(role.getName(), role.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return repository.countRoles();
    }
}
