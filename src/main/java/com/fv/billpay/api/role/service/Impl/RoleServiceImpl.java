package com.fv.billpay.api.role.service.Impl;

import com.fv.billpay.api.role.dto.request.RoleRequestDto;
import com.fv.billpay.api.role.dto.response.PagedResponse;
import com.fv.billpay.api.role.dto.response.RoleResponseDto;
import com.fv.billpay.api.role.mapper.RoleMapper;
import com.fv.billpay.api.role.repository.IRoleRepository;
import com.fv.billpay.api.role.service.IRoleService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleServiceImpl implements IRoleService {
    @Inject
    IRoleRepository repository;

    @Override
    public RoleResponseDto create(RoleRequestDto dto) {
        boolean created = repository.createRole(dto.getName(), dto.getDescription());
        if (!created) {
            throw new WebApplicationException(
                "No se pudo crear el rol. Puede que ya exista o no tenga permisos suficientes.",
                Response.Status.CONFLICT
            );
        }
        return RoleMapper.toResponseDto(dto.getName(), dto.getDescription());
    }

    @Override
    public RoleResponseDto update(String roleName, RoleRequestDto dto) {
        // Usar el roleName del path como identificador, no el del DTO
        // El DTO solo proporciona la nueva descripción
        boolean updated = repository.updateRole(roleName, dto.getDescription());
        if (!updated) {
            throw new NotFoundException("Rol no encontrado o sin permisos para actualizarlo: " + roleName);
        }
        return RoleMapper.toResponseDto(roleName, dto.getDescription());
    }

    @Override
    public boolean delete(String roleName) {
        if (roleName == null || roleName.isBlank()) return false;
        return repository.deleteRole(roleName);
    }

    @Override
    public RoleResponseDto getById(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("El nombre del rol no puede estar vacío");
        }
        return repository.getRole(roleName)
            .map(role -> RoleMapper.toResponseDto(role.getName(), role.getDescription()))
            .orElseThrow(() -> new NotFoundException("Rol no encontrado: " + roleName));
    }

    @Override
    public PagedResponse<RoleResponseDto> getAll(int page, int size) {
        int offset = page * size;
        List<RoleResponseDto> roles = repository.getAllRoles(offset, size).stream()
                .map(role -> RoleMapper.toResponseDto(role.getName(), role.getDescription()))
                .collect(Collectors.toList());
        long total = repository.countRoles();
        return new PagedResponse<>(roles, total, page, size);
    }
}
