package com.fv.billpay.api.role.resource;

import com.fv.billpay.api.role.dto.request.RoleRequestDto;
import com.fv.billpay.api.role.dto.response.PagedResponse;
import com.fv.billpay.api.role.dto.response.RoleResponseDto;
import com.fv.billpay.api.role.service.IRoleService;
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

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleResource {
    @Inject
    IRoleService service;

    @POST
    @RolesAllowed({"admin_role"})
    public Response create(@Valid @ConvertGroup(to = RoleRequestDto.CreateValidation.class) RoleRequestDto dto) {
        return Process.ok(service.create(dto));
    }

    @PUT
    @Path("/{roleName}")
    @RolesAllowed({"admin_role"})
    public Response update(
            @PathParam("roleName")
            @NotBlank(message = "El nombre del rol es requerido")
            @Pattern(regexp = "^[a-zA-Z0-9_-]{3,50}$", message = "Formato de rol inválido. Solo alfanuméricos, guiones y guiones bajos (3-50 caracteres)")
            String roleName,
            @Valid @ConvertGroup(to = RoleRequestDto.UpdateValidation.class) RoleRequestDto dto) {
        return Process.ok(service.update(roleName, dto));
    }

    @DELETE
    @Path("/{roleName}")
    @RolesAllowed({"admin_role"})
    public Response delete(
            @PathParam("roleName")
            @NotBlank(message = "El nombre del rol es requerido")
            @Pattern(regexp = "^[a-zA-Z0-9_-]{3,50}$", message = "Formato de rol inválido. Solo alfanuméricos, guiones y guiones bajos (3-50 caracteres)")
            String roleName) {
        boolean deleted = service.delete(roleName);
        if (deleted) return Process.ok("Eliminado correctamente");
        return Process.notFound("No se encontró el rol");
    }

    @GET
    @Path("/{roleName}")
    @RolesAllowed({"admin_role"})
    public Response getById(
            @PathParam("roleName")
            @NotBlank(message = "El nombre del rol es requerido")
            @Pattern(regexp = "^[a-zA-Z0-9_-]{3,50}$", message = "Formato de rol inválido. Solo alfanuméricos, guiones y guiones bajos (3-50 caracteres)")
            String roleName) {
        return Process.ok(service.getById(roleName));
    }

    @GET
    @RolesAllowed({"admin_role"})
    public Response getAll(
            @QueryParam("page") @DefaultValue("0") @Min(value = 0, message = "La página debe ser mayor o igual a 0") int page,
            @QueryParam("size") @DefaultValue("10") @Min(value = 1, message = "El tamaño debe ser al menos 1") @Max(value = 100, message = "El tamaño máximo es 100") int size) {
        PagedResponse<RoleResponseDto> pagedResponse = service.getAll(page, size);
        return Process.ok(pagedResponse);
    }
}
