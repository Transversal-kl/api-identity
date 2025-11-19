package com.fv.billpay.api.role.repository.Impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ForbiddenException;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.representations.idm.GroupRepresentation;

import com.fv.billpay.api.role.repository.IGroupRepository;
import com.fv.billpay.api.role.utils.KeycloakAdminProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class GroupRepositoryImpl implements IGroupRepository {
    private static final Logger LOG = Logger.getLogger(GroupRepositoryImpl.class);

    @Inject
    KeycloakAdminProvider keycloakAdminProvider;
    
    @Override
    public boolean createGroup(String name, String description) {
        try {
            GroupsResource groups = keycloakAdminProvider.getGroupsResource();
            GroupRepresentation group = new GroupRepresentation();
            group.setName(name);
            
            // Keycloak maneja la descripción como atributo
            Map<String, List<String>> attributes = new HashMap<>();
            if (description != null && !description.isBlank()) {
                attributes.put("description", Collections.singletonList(description));
            }
            group.setAttributes(attributes);
            
            groups.add(group);
            LOG.infof("Grupo creado exitosamente: %s", name);
            return true;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para crear el grupo '%s': %s", name, e.getMessage());
            return false;
        } catch (jakarta.ws.rs.ClientErrorException e) {
            if (e.getResponse().getStatus() == 409) {
                LOG.warnf("El grupo '%s' ya existe en Keycloak", name);
            } else {
                LOG.errorf(e, "Error del cliente al crear el grupo '%s': HTTP %d", name, e.getResponse().getStatus());
            }
            return false;
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al crear el grupo '%s'", name);
            return false;
        }
    }

    @Override
    public boolean updateGroup(String groupId, String newName, String newDescription) {
        try {
            GroupsResource groups = keycloakAdminProvider.getGroupsResource();
            GroupRepresentation group = groups.group(groupId).toRepresentation();
            
            if (newName != null && !newName.isBlank()) {
                group.setName(newName);
            }
            
            // Actualizar descripción en atributos
            Map<String, List<String>> attributes = group.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            if (newDescription != null) {
                attributes.put("description", Collections.singletonList(newDescription));
            }
            group.setAttributes(attributes);
            
            groups.group(groupId).update(group);
            LOG.infof("Grupo actualizado exitosamente: %s (ID: %s)", newName, groupId);
            return true;
        } catch (NotFoundException e) {
            LOG.warnf("El grupo con ID '%s' no existe en Keycloak", groupId);
            return false;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para actualizar el grupo con ID '%s': %s", groupId, e.getMessage());
            return false;
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al actualizar el grupo con ID '%s'", groupId);
            return false;
        }
    }

    @Override
    public boolean deleteGroup(String groupId) {
        try {
            GroupsResource groups = keycloakAdminProvider.getGroupsResource();
            groups.group(groupId).remove();
            LOG.infof("Grupo eliminado exitosamente: ID %s", groupId);
            return true;
        } catch (NotFoundException e) {
            LOG.warnf("El grupo con ID '%s' no existe en Keycloak", groupId);
            return false;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para eliminar el grupo con ID '%s': %s", groupId, e.getMessage());
            return false;
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al eliminar el grupo con ID '%s'", groupId);
            return false;
        }
    }

    @Override
    public Optional<GroupRepresentation> getGroup(String groupId) {
        try {
            GroupsResource groups = keycloakAdminProvider.getGroupsResource();
            GroupRepresentation group = groups.group(groupId).toRepresentation();
            LOG.debugf("Grupo obtenido por ID: %s", groupId);
            return Optional.ofNullable(group);
        } catch (NotFoundException e) {
            LOG.debugf("El grupo con ID '%s' no existe en Keycloak", groupId);
            return Optional.empty();
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para consultar el grupo con ID '%s': %s", groupId, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al obtener el grupo con ID '%s'", groupId);
            return Optional.empty();
        }
    }

    @Override
    public Optional<GroupRepresentation> getGroupByName(String groupName) {
        try {
            GroupsResource groups = keycloakAdminProvider.getGroupsResource();
            // Keycloak no tiene búsqueda directa por nombre, debemos filtrar
            List<GroupRepresentation> allGroups = groups.groups();
            Optional<GroupRepresentation> foundGroup = allGroups.stream()
                .filter(g -> groupName.equals(g.getName()))
                .findFirst();
            
            if (foundGroup.isPresent()) {
                LOG.debugf("Grupo encontrado por nombre: %s", groupName);
            } else {
                LOG.debugf("El grupo '%s' no existe en Keycloak", groupName);
            }
            return foundGroup;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para buscar el grupo '%s': %s", groupName, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al buscar el grupo '%s'", groupName);
            return Optional.empty();
        }
    }

    @Override
    public List<GroupRepresentation> getAllGroups(int first, int max) {
        try {
            GroupsResource groups = keycloakAdminProvider.getGroupsResource();
            // Con Admin Client 26.x compatible con Server 26.x, podemos usar groups() directamente
            List<GroupRepresentation> allGroups = groups.groups();
            
            // Aplicar paginación manual
            int fromIndex = Math.min(first, allGroups.size());
            int toIndex = Math.min(first + max, allGroups.size());
            List<GroupRepresentation> groupList = allGroups.subList(fromIndex, toIndex);
            
            LOG.debugf("Obtenidos %d grupos de %d totales (offset: %d, limit: %d)", 
                      groupList.size(), allGroups.size(), first, max);
            return groupList;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para listar grupos: %s", e.getMessage());
            return List.of();
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al listar grupos");
            return List.of();
        }
    }

    @Override
    public long countGroups() {
        try {
            GroupsResource groups = keycloakAdminProvider.getGroupsResource();
            // Con Admin Client 26.x, groups().size() funciona correctamente
            int count = groups.groups().size();
            LOG.debugf("Total de grupos en Keycloak: %d", count);
            return count;
        } catch (ForbiddenException e) {
            LOG.errorf("Permisos insuficientes para contar grupos: %s", e.getMessage());
            return 0;
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado al contar grupos");
            return 0;
        }
    }
}
