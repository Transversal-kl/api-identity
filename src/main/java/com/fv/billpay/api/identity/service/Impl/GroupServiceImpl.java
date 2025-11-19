package com.fv.billpay.api.identity.service.Impl;

import com.fv.billpay.api.identity.dto.request.GroupRequestDto;
import com.fv.billpay.api.identity.dto.response.PagedResponse;
import com.fv.billpay.api.identity.dto.response.GroupResponseDto;
import com.fv.billpay.api.identity.mapper.GroupMapper;
import com.fv.billpay.api.identity.repository.IGroupRepository;
import com.fv.billpay.api.identity.service.IGroupService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GroupServiceImpl implements IGroupService {
    @Inject
    IGroupRepository repository;

    @Override
    public GroupResponseDto create(GroupRequestDto dto) {
        boolean created = repository.createGroup(dto.getName(), dto.getDescription());
        if (!created) {
            throw new WebApplicationException(
                "No se pudo crear el grupo. Puede que ya exista o no tenga permisos suficientes.",
                Response.Status.CONFLICT
            );
        }
        
        // Obtener el grupo recién creado para retornar su ID
        return repository.getGroupByName(dto.getName())
            .map(GroupMapper::toResponseDto)
            .orElseThrow(() -> new WebApplicationException(
                "El grupo fue creado pero no se pudo recuperar",
                Response.Status.INTERNAL_SERVER_ERROR
            ));
    }

    @Override
    public GroupResponseDto update(String groupId, GroupRequestDto dto) {
        // Verificar que el grupo existe antes de actualizar
        repository.getGroup(groupId)
            .orElseThrow(() -> new NotFoundException("Grupo no encontrado: " + groupId));
        
        boolean updated = repository.updateGroup(groupId, dto.getName(), dto.getDescription());
        if (!updated) {
            throw new NotFoundException("Grupo no encontrado o sin permisos para actualizarlo: " + groupId);
        }
        
        return repository.getGroup(groupId)
            .map(GroupMapper::toResponseDto)
            .orElseThrow(() -> new NotFoundException("Grupo no encontrado después de actualizar: " + groupId));
    }

    @Override
    public boolean delete(String groupId) {
        if (groupId == null || groupId.isBlank()) return false;
        return repository.deleteGroup(groupId);
    }

    @Override
    public GroupResponseDto getById(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("El ID del grupo no puede estar vacío");
        }
        return repository.getGroup(groupId)
            .map(GroupMapper::toResponseDto)
            .orElseThrow(() -> new NotFoundException("Grupo no encontrado con ID: " + groupId));
    }

    @Override
    public GroupResponseDto getByName(String groupName) {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("El nombre del grupo no puede estar vacío");
        }
        return repository.getGroupByName(groupName)
            .map(GroupMapper::toResponseDto)
            .orElseThrow(() -> new NotFoundException("Grupo no encontrado con nombre: " + groupName));
    }

    @Override
    public PagedResponse<GroupResponseDto> getAll(int page, int size) {
        int offset = page * size;
        List<GroupResponseDto> groups = repository.getAllGroups(offset, size).stream()
                .map(GroupMapper::toResponseDto)
                .collect(Collectors.toList());
        long total = repository.countGroups();
        return new PagedResponse<>(groups, total, page, size);
    }
}
