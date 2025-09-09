
package com.fv.billpay.api.role.repository.Impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;

import com.fv.billpay.api.role.repository.IRoleRepository;
import com.fv.billpay.api.role.utils.KeycloakAdminProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleRepositoryImpl implements IRoleRepository {

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
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateRole( String roleName, String newDescription) {
        try {
        RolesResource roles = keycloakAdminProvider.getRolesResource();
        RoleRepresentation role = roles.get(roleName).toRepresentation();
        role.setDescription(newDescription);
        roles.get(roleName).update(role);
        return true;
    } catch (Exception e) {
        return false;
    }
    }

    @Override
    public boolean deleteRole(String roleName) {
        try {
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            roles.get(roleName).remove();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<RoleRepresentation> getRole(String roleName) {
        try {
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            RoleRepresentation role = roles.get(roleName).toRepresentation();
            return Optional.ofNullable(role);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<RoleRepresentation> getAllRoles(int first, int max) {
        try {
            RolesResource roles = keycloakAdminProvider.getRolesResource();
            return roles.list().stream()
                    .skip(first)
                    .limit(max)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }
}
