package com.fv.billpay.api.role.repository.Impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ForbiddenException;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;

import com.fv.billpay.api.role.repository.IGroupRoleRepository;
import com.fv.billpay.api.role.utils.KeycloakAdminProvider;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GroupRoleRepositoryImpl implements IGroupRoleRepository {
    private static final Logger LOG = Logger.getLogger(GroupRoleRepositoryImpl.class);

    @Inject
    KeycloakAdminProvider keycloakAdminProvider;

    @Override
    public boolean assignRoleToGroup(String groupId, String roleName) {
        try {
            GroupsResource groups = keycloakAdminProvider.getGroupsResource();
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            
            // Obtener el grupo
            GroupResource group = groups.group(groupId);
            
            // Obtener el rol
            RoleRepresentation role = roles.get(roleName).toRepresentation();
            
            // Asignar el rol al grupo (realm role)
            RoleMappingResource roleMappingResource = group.roles();
            RoleScopeResource realmRoles = roleMappingResource.realmLevel();
            realmRoles.add(Collections.singletonList(role));
            
            LOG.infof("Rol '%s' asignado exitosamente al grupo con ID: %s", roleName, groupId);
            return true;
        } catch (NotFoundException e) {
            LOG.warnf("El grupo con ID '%s' o el rol '%s' no existe en Keycloak", groupId, roleName);
            return false;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para asignar el rol '%s' al grupo '%s': %s", 
                      roleName, groupId, e.getMessage());
            return false;
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al asignar el rol '%s' al grupo '%s'", roleName, groupId);
            return false;
        }
    }

    @Override
    public boolean removeRoleFromGroup(String groupId, String roleName) {
        try {
            GroupsResource groups = keycloakAdminProvider.getGroupsResource();
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            
            // Obtener el grupo
            GroupResource group = groups.group(groupId);
            
            // Obtener el rol
            RoleRepresentation role = roles.get(roleName).toRepresentation();
            
            // Desasignar el rol del grupo (realm role)
            RoleMappingResource roleMappingResource = group.roles();
            RoleScopeResource realmRoles = roleMappingResource.realmLevel();
            realmRoles.remove(Collections.singletonList(role));
            
            LOG.infof("Rol '%s' desasignado exitosamente del grupo con ID: %s", roleName, groupId);
            return true;
        } catch (NotFoundException e) {
            LOG.warnf("El grupo con ID '%s' o el rol '%s' no existe en Keycloak", groupId, roleName);
            return false;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para desasignar el rol '%s' del grupo '%s': %s", 
                      roleName, groupId, e.getMessage());
            return false;
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al desasignar el rol '%s' del grupo '%s'", roleName, groupId);
            return false;
        }
    }

    @Override
    public List<RoleRepresentation> getGroupRoles(String groupId) {
        try {
            GroupsResource groups = keycloakAdminProvider.getGroupsResource();
            GroupResource group = groups.group(groupId);
            
            // Obtener roles realm asignados al grupo
            RoleMappingResource roleMappingResource = group.roles();
            RoleScopeResource realmRoles = roleMappingResource.realmLevel();
            List<RoleRepresentation> assignedRoles = realmRoles.listAll();
            
            LOG.debugf("Obtenidos %d roles asignados al grupo con ID: %s", 
                      assignedRoles.size(), groupId);
            return assignedRoles;
        } catch (NotFoundException e) {
            LOG.warnf("El grupo con ID '%s' no existe en Keycloak", groupId);
            return List.of();
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para listar roles del grupo '%s': %s", 
                      groupId, e.getMessage());
            return List.of();
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al obtener roles del grupo '%s'", groupId);
            return List.of();
        }
    }

    @Override
    public List<RoleRepresentation> getAvailableRoles(String groupId) {
        try {
            GroupsResource groups = keycloakAdminProvider.getGroupsResource();
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            
            // Obtener el grupo
            GroupResource group = groups.group(groupId);
            
            // Obtener roles asignados al grupo
            RoleMappingResource roleMappingResource = group.roles();
            RoleScopeResource realmRoles = roleMappingResource.realmLevel();
            List<RoleRepresentation> assignedRoles = realmRoles.listAll();
            
            // Obtener todos los roles del realm
            List<RoleRepresentation> allRoles = roles.list();
            
            // Filtrar roles no asignados
            List<String> assignedRoleNames = assignedRoles.stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());
            
            List<RoleRepresentation> availableRoles = allRoles.stream()
                .filter(role -> !assignedRoleNames.contains(role.getName()))
                .collect(Collectors.toList());
            
            LOG.debugf("Obtenidos %d roles disponibles para el grupo con ID: %s", 
                      availableRoles.size(), groupId);
            return availableRoles;
        } catch (NotFoundException e) {
            LOG.warnf("El grupo con ID '%s' no existe en Keycloak", groupId);
            return List.of();
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para listar roles disponibles del grupo '%s': %s", 
                      groupId, e.getMessage());
            return List.of();
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al obtener roles disponibles del grupo '%s'", groupId);
            return List.of();
        }
    }
}
