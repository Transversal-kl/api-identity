package com.fv.billpay.api.identity.repository;

import org.keycloak.representations.idm.RoleRepresentation;
import java.util.List;

/**
 * Repositorio para gestionar roles asignados a grupos en Keycloak
 */
public interface IGroupRoleRepository {
    
    /**
     * Asigna un rol realm a un grupo
     * @param groupId ID del grupo
     * @param roleName Nombre del rol a asignar
     * @return true si se asignó correctamente
     */
    boolean assignRoleToGroup(String groupId, String roleName);
    
    /**
     * Desasigna un rol realm de un grupo
     * @param groupId ID del grupo
     * @param roleName Nombre del rol a desasignar
     * @return true si se desasignó correctamente
     */
    boolean removeRoleFromGroup(String groupId, String roleName);
    
    /**
     * Obtiene todos los roles realm asignados a un grupo
     * @param groupId ID del grupo
     * @return Lista de roles asignados
     */
    List<RoleRepresentation> getGroupRoles(String groupId);
    
    /**
     * Obtiene todos los roles realm disponibles (no asignados) para un grupo
     * @param groupId ID del grupo
     * @return Lista de roles disponibles
     */
    List<RoleRepresentation> getAvailableRoles(String groupId);
}
