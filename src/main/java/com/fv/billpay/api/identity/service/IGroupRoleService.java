package com.fv.billpay.api.identity.service;

import com.fv.billpay.api.identity.dto.request.GroupRoleRequestDto;
import com.fv.billpay.api.identity.dto.response.GroupRoleResponseDto;
import com.fv.billpay.api.identity.dto.response.PagedResponse;

import java.util.List;

/**
 * Servicio para gestionar roles de grupos
 */
public interface IGroupRoleService {
    
    /**
     * Asigna múltiples roles a un grupo
     * @param groupId ID del grupo
     * @param dto DTO con lista de nombres de roles
     * @return Lista de roles asignados exitosamente
     */
    List<GroupRoleResponseDto> assignRolesToGroup(String groupId, GroupRoleRequestDto dto);
    
    /**
     * Desasigna múltiples roles de un grupo
     * @param groupId ID del grupo
     * @param dto DTO con lista de nombres de roles
     * @return true si todos los roles fueron desasignados
     */
    boolean removeRolesFromGroup(String groupId, GroupRoleRequestDto dto);
    
    /**
     * Obtiene todos los roles asignados a un grupo con paginación
     * @param groupId ID del grupo
     * @param page Número de página (inicia en 0)
     * @param size Tamaño de página
     * @return Respuesta paginada con roles asignados
     */
    PagedResponse<GroupRoleResponseDto> getGroupRoles(String groupId, int page, int size);
    
    /**
     * Obtiene todos los roles disponibles (no asignados) para un grupo
     * @param groupId ID del grupo
     * @return Lista de roles disponibles
     */
    List<GroupRoleResponseDto> getAvailableRoles(String groupId);
}
