package com.fv.billpay.api.role.resource;

import com.fv.billpay.api.role.dto.request.GroupRoleRequestDto;
import com.fv.billpay.api.role.dto.response.GroupRoleResponseDto;
import com.fv.billpay.api.role.service.IGroupRoleService;
import com.fv.billpay.api.role.utils.Process;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Resource para gestionar roles de grupos
 * Path: /groups/{groupId}/roles
 */
@Path("/groups/{groupId}/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupRoleResource {

    @Inject
    IGroupRoleService service;

    /**
     * Asignar roles a un grupo
     * POST /groups/{groupId}/roles
     */
    @POST
    @RolesAllowed({"admin_groups"})
    public Response assignRolesToGroup(
            @PathParam("groupId")
            @NotBlank(message = "El ID del grupo es requerido")
            String groupId,
            @Valid GroupRoleRequestDto dto) {
        List<GroupRoleResponseDto> roles = service.assignRolesToGroup(groupId, dto);
        return Process.ok(roles);
    }

    /**
     * Desasignar roles de un grupo
     * DELETE /groups/{groupId}/roles
     */
    @DELETE
    @RolesAllowed({"admin_groups"})
    public Response removeRolesFromGroup(
            @PathParam("groupId")
            @NotBlank(message = "El ID del grupo es requerido")
            String groupId,
            @Valid GroupRoleRequestDto dto) {
        service.removeRolesFromGroup(groupId, dto);
        return Process.ok("Roles desasignados correctamente");
    }

    /**
     * Obtener roles asignados a un grupo
     * GET /groups/{groupId}/roles
     */
    @GET
    @RolesAllowed({"admin_groups"})
    public Response getGroupRoles(
            @PathParam("groupId")
            @NotBlank(message = "El ID del grupo es requerido")
            String groupId) {
        List<GroupRoleResponseDto> roles = service.getGroupRoles(groupId);
        return Process.ok(roles);
    }

    /**
     * Obtener roles disponibles (no asignados) para un grupo
     * GET /groups/{groupId}/roles/available
     */
    @GET
    @Path("/available")
    @RolesAllowed({"admin_groups"})
    public Response getAvailableRoles(
            @PathParam("groupId")
            @NotBlank(message = "El ID del grupo es requerido")
            String groupId) {
        List<GroupRoleResponseDto> roles = service.getAvailableRoles(groupId);
        return Process.ok(roles);
    }
}
