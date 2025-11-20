package com.fv.billpay.api.identity.service.Impl;

import com.fv.billpay.api.identity.dto.request.GroupRoleRequestDto;
import com.fv.billpay.api.identity.dto.response.GroupRoleResponseDto;
import com.fv.billpay.api.identity.dto.response.PagedResponse;
import com.fv.billpay.api.identity.mapper.GroupRoleMapper;
import com.fv.billpay.api.identity.repository.IGroupRepository;
import com.fv.billpay.api.identity.repository.IGroupRoleRepository;
import com.fv.billpay.api.identity.service.IGroupRoleService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GroupRoleServiceImpl implements IGroupRoleService {

    @Inject
    IGroupRoleRepository groupRoleRepository;
    
    @Inject
    IGroupRepository groupRepository;

    @Override
    public List<GroupRoleResponseDto> assignRolesToGroup(String groupId, GroupRoleRequestDto dto) {
        // Verificar que el grupo existe
        groupRepository.getGroup(groupId)
            .orElseThrow(() -> new NotFoundException("Grupo no encontrado: " + groupId));
        
        List<String> failedRoles = new ArrayList<>();
        
        for (String roleName : dto.getRoleNames()) {
            boolean assigned = groupRoleRepository.assignRoleToGroup(groupId, roleName);
            if (!assigned) {
                failedRoles.add(roleName);
            }
        }
        
        // Si hubo fallos, lanzar excepción con detalles
        if (!failedRoles.isEmpty()) {
            throw new WebApplicationException(
                String.format("No se pudieron asignar los siguientes roles: %s. " +
                             "Verifica que existan y tengas permisos suficientes.", 
                             String.join(", ", failedRoles)),
                Response.Status.BAD_REQUEST
            );
        }
        
        // Retornar los roles actualmente asignados al grupo
        return getAllGroupRoles(groupId);
    }

    @Override
    public boolean removeRolesFromGroup(String groupId, GroupRoleRequestDto dto) {
        // Verificar que el grupo existe
        groupRepository.getGroup(groupId)
            .orElseThrow(() -> new NotFoundException("Grupo no encontrado: " + groupId));
        
        List<String> failedRoles = new ArrayList<>();
        
        for (String roleName : dto.getRoleNames()) {
            boolean removed = groupRoleRepository.removeRoleFromGroup(groupId, roleName);
            if (!removed) {
                failedRoles.add(roleName);
            }
        }
        
        // Si hubo fallos, lanzar excepción con detalles
        if (!failedRoles.isEmpty()) {
            throw new WebApplicationException(
                String.format("No se pudieron desasignar los siguientes roles: %s. " +
                             "Verifica que estén asignados al grupo y tengas permisos suficientes.", 
                             String.join(", ", failedRoles)),
                Response.Status.BAD_REQUEST
            );
        }
        
        return true;
    }

    @Override
    public PagedResponse<GroupRoleResponseDto> getGroupRoles(String groupId, int page, int size) {
        // Validar parámetros de paginación
        if (page < 0 || size <= 0) {
            throw new WebApplicationException(
                String.format("Parámetros de paginación inválidos (page=%d, size=%d)", page, size),
                Response.Status.BAD_REQUEST
            );
        }
        
        // Verificar que el grupo existe
        groupRepository.getGroup(groupId)
            .orElseThrow(() -> new NotFoundException("Grupo no encontrado: " + groupId));
        
        // Obtener todos los roles y convertir a DTOs
        List<GroupRoleResponseDto> allRoles = groupRoleRepository.getGroupRoles(groupId).stream()
            .map(GroupRoleMapper::toResponseDto)
            .collect(Collectors.toList());
        
        // Aplicar paginación manual
        int totalElements = allRoles.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);
        
        List<GroupRoleResponseDto> pagedContent = fromIndex < totalElements
            ? allRoles.subList(fromIndex, toIndex)
            : List.of();
        
        return new PagedResponse<>(
            pagedContent,
            totalElements,
            page,
            size
        );
    }

    /**
     * Método privado para obtener todos los roles sin paginación (uso interno)
     */
    private List<GroupRoleResponseDto> getAllGroupRoles(String groupId) {
        return groupRoleRepository.getGroupRoles(groupId).stream()
            .map(GroupRoleMapper::toResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<GroupRoleResponseDto> getAvailableRoles(String groupId) {
        // Verificar que el grupo existe
        groupRepository.getGroup(groupId)
            .orElseThrow(() -> new NotFoundException("Grupo no encontrado: " + groupId));
        
        return groupRoleRepository.getAvailableRoles(groupId).stream()
            .map(GroupRoleMapper::toResponseDto)
            .collect(Collectors.toList());
    }
}
