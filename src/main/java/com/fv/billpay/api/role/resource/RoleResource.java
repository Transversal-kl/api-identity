package com.fv.billpay.api.role.resource;

import com.fv.billpay.api.role.dto.request.RoleRequestDto;
import com.fv.billpay.api.role.dto.response.RoleResponseDto;
import com.fv.billpay.api.role.service.IRoleService;
import com.fv.billpay.api.role.utils.Process;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleResource {
    @Inject
    IRoleService service;

    @POST
    public Response create(@Valid RoleRequestDto dto) {
        return Process.ok(service.create(dto));
    }

    @PUT
    @Path("/{roleName}")
    public Response update(@PathParam("roleName") String rolName, @Valid RoleRequestDto dto) {
        return Process.ok(service.update(dto));
    }

    @DELETE
    @Path("/{rolName}")
    public Response delete(@PathParam("rolName") String rolName) {
        boolean deleted = service.delete(rolName);
        if (deleted) return Process.ok("Eliminado correctamente");
        return Process.notFound("No se encontró el rol");
    }

    @GET
    @Path("/{rolName}")
    public Response getById(@PathParam("rolName") String rolName) {
        return Process.ok(service.getById(rolName));
    }

    @GET
    public Response getAll(@QueryParam("page") @DefaultValue("0") int page,
                           @QueryParam("size") @DefaultValue("10") int size) {
        List<RoleResponseDto> roles = service.getAll(page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("roles", roles);
        result.put("total", service.count());
        return Process.ok(result);
    }
}
