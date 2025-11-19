package com.fv.billpay.api.role.service.Impl;

import com.fv.billpay.api.role.dto.request.GroupRoleRequestDto;
import com.fv.billpay.api.role.dto.response.GroupRoleResponseDto;
import com.fv.billpay.api.role.mapper.GroupRoleMapper;
import com.fv.billpay.api.role.repository.IGroupRepository;
import com.fv.billpay.api.role.repository.IGroupRoleRepository;
import com.fv.billpay.api.role.service.IGroupRoleService;

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
        return getGroupRoles(groupId);
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
    public List<GroupRoleResponseDto> getGroupRoles(String groupId) {
        // Verificar que el grupo existe
        groupRepository.getGroup(groupId)
            .orElseThrow(() -> new NotFoundException("Grupo no encontrado: " + groupId));
        
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
