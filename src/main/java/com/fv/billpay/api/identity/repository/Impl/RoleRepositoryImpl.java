package com.fv.billpay.api.identity.repository.Impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ForbiddenException;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;

import com.fv.billpay.api.identity.repository.IRoleRepository;
import com.fv.billpay.api.identity.utils.KeycloakAdminProvider;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RoleRepositoryImpl implements IRoleRepository {
    private static final Logger LOG = Logger.getLogger(RoleRepositoryImpl.class);

    @Inject
    KeycloakAdminProvider keycloakAdminProvider;
    
    @Override
    public boolean createRole(String name, String description) {
        try {
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            RoleRepresentation role = new RoleRepresentation();
            role.setName(name);
            role.setDescription(description);
            roles.create(role);
            LOG.infof("Rol creado exitosamente: %s", name);
            return true;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para crear el rol '%s': %s", name, e.getMessage());
            return false;
        } catch (jakarta.ws.rs.ClientErrorException e) {
            if (e.getResponse().getStatus() == 409) {
                LOG.warnf("El rol '%s' ya existe en Keycloak", name);
            } else {
                LOG.errorf(e, "Error del cliente al crear el rol '%s': HTTP %d", name, e.getResponse().getStatus());
            }
            return false;
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al crear el rol '%s'", name);
            return false;
        }
    }

    @Override
    public boolean updateRole(String roleName, String newDescription) {
        try {
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            RoleRepresentation role = roles.get(roleName).toRepresentation();
            role.setDescription(newDescription);
            roles.get(roleName).update(role);
            LOG.infof("Rol actualizado exitosamente: %s", roleName);
            return true;
        } catch (NotFoundException e) {
            LOG.warnf("El rol '%s' no existe en Keycloak", roleName);
            return false;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para actualizar el rol '%s': %s", roleName, e.getMessage());
            return false;
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al actualizar el rol '%s'", roleName);
            return false;
        }
    }

    @Override
    public boolean deleteRole(String roleName) {
        try {
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            roles.get(roleName).remove();
            LOG.infof("Rol eliminado exitosamente: %s", roleName);
            return true;
        } catch (NotFoundException e) {
            LOG.warnf("El rol '%s' no existe en Keycloak", roleName);
            return false;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para eliminar el rol '%s': %s", roleName, e.getMessage());
            return false;
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al eliminar el rol '%s'", roleName);
            return false;
        }
    }

    @Override
    public Optional<RoleRepresentation> getRole(String roleName) {
        try {
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            RoleRepresentation role = roles.get(roleName).toRepresentation();
            LOG.debugf("Rol obtenido: %s", roleName);
            return Optional.ofNullable(role);
        } catch (NotFoundException e) {
            LOG.debugf("El rol '%s' no existe en Keycloak", roleName);
            return Optional.empty();
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para consultar el rol '%s': %s", roleName, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al obtener el rol '%s'", roleName);
            return Optional.empty();
        }
    }

    @Override
    public List<RoleRepresentation> getAllRoles(int first, int max) {
        try {
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            // Usar paginación nativa de Keycloak en lugar de stream().skip().limit()
            List<RoleRepresentation> roleList = roles.list(first, max);
            LOG.debugf("Obtenidos %d roles (offset: %d, limit: %d)", roleList.size(), first, max);
            return roleList;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para listar roles: %s", e.getMessage());
            return List.of();
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al listar roles");
            return List.of();
        }
    }

    @Override
    public long countRoles() {
        try {
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            // Keycloak no tiene método count() directo, debemos obtener todos y contar
            int total = roles.list().size();
            LOG.debugf("Total de roles: %d", total);
            return total;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para contar roles: %s", e.getMessage());
            return 0;
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al contar roles");
            return 0;
        }
    }
}
