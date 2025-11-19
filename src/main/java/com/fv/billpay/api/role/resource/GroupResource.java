package com.fv.billpay.api.role.resource;

import com.fv.billpay.api.role.dto.request.GroupRequestDto;
import com.fv.billpay.api.role.dto.response.PagedResponse;
import com.fv.billpay.api.role.dto.response.GroupResponseDto;
import com.fv.billpay.api.role.service.IGroupService;
import com.fv.billpay.api.role.utils.Process;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupResource {
    @Inject
    IGroupService service;

    @POST
    @RolesAllowed({"admin_groups"})
    public Response create(@Valid @ConvertGroup(to = GroupRequestDto.CreateValidation.class) GroupRequestDto dto) {
        return Process.ok(service.create(dto));
    }

    @PUT
    @Path("/{groupId}")
    @RolesAllowed({"admin_groups"})
    public Response update(
            @PathParam("groupId")
            @NotBlank(message = "El ID del grupo es requerido")
            String groupId,
            @Valid @ConvertGroup(to = GroupRequestDto.UpdateValidation.class) GroupRequestDto dto) {
        return Process.ok(service.update(groupId, dto));
    }

    @DELETE
    @Path("/{groupId}")
    @RolesAllowed({"admin_groups"})
    public Response delete(
            @PathParam("groupId")
            @NotBlank(message = "El ID del grupo es requerido")
            String groupId) {
        boolean deleted = service.delete(groupId);
        if (deleted) return Process.ok("Eliminado correctamente");
        return Process.notFound("No se encontró el grupo");
    }

    @GET
    @Path("/{groupId}")
    @RolesAllowed({"admin_groups"})
    public Response getById(
            @PathParam("groupId")
            @NotBlank(message = "El ID del grupo es requerido")
            String groupId) {
        return Process.ok(service.getById(groupId));
    }

    @GET
    @Path("/name/{groupName}")
    @RolesAllowed({"admin_groups"})
    public Response getByName(
            @PathParam("groupName")
            @NotBlank(message = "El nombre del grupo es requerido")
            @Pattern(regexp = "^[a-zA-Z0-9_-]{3,50}$", message = "Formato de grupo inválido. Solo alfanuméricos, guiones y guiones bajos (3-50 caracteres)")
            String groupName) {
        return Process.ok(service.getByName(groupName));
    }

    @GET
    @RolesAllowed({"admin_groups"})
    public Response getAll(
            @QueryParam("page") @DefaultValue("0") @Min(value = 0, message = "La página debe ser mayor o igual a 0") int page,
            @QueryParam("size") @DefaultValue("10") @Min(value = 1, message = "El tamaño debe ser al menos 1") @Max(value = 100, message = "El tamaño máximo es 100") int size) {
        PagedResponse<GroupResponseDto> pagedResponse = service.getAll(page, size);
        return Process.ok(pagedResponse);
    }
}
