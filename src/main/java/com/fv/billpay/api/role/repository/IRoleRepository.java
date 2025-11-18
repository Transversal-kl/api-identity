package com.fv.billpay.api.role.repository;

import org.keycloak.representations.idm.RoleRepresentation;
import java.util.List;
import java.util.Optional;

public interface IRoleRepository {
    boolean createRole(String name, String description);
    boolean updateRole(String roleName, String newDescription);
    boolean deleteRole(String roleName);
    Optional<RoleRepresentation> getRole(String roleName);
    List<RoleRepresentation> getAllRoles(int first, int max);
    long countRoles();
}
