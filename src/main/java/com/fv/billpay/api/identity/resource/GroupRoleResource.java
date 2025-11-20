package com.fv.billpay.api.identity.resource;

import com.fv.billpay.api.identity.dto.request.GroupRoleRequestDto;
import com.fv.billpay.api.identity.dto.response.GroupRoleResponseDto;
import com.fv.billpay.api.identity.dto.response.PagedResponse;
import com.fv.billpay.api.identity.service.IGroupRoleService;
import com.fv.billpay.api.identity.utils.Process;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
     * Obtener roles asignados a un grupo con paginación
     * GET /groups/{groupId}/roles?page=0&size=10
     */
    @GET
    @RolesAllowed({"admin_groups"})
    public Response getGroupRoles(
            @PathParam("groupId")
            @NotBlank(message = "El ID del grupo es requerido")
            String groupId,
            @QueryParam("page")
            @DefaultValue("0")
            @Min(value = 0, message = "La página debe ser mayor o igual a 0")
            int page,
            @QueryParam("size")
            @DefaultValue("10")
            @Min(value = 1, message = "El tamaño debe ser al menos 1")
            @Max(value = 100, message = "El tamaño máximo es 100")
            int size) {
        PagedResponse<GroupRoleResponseDto> roles = service.getGroupRoles(groupId, page, size);
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
