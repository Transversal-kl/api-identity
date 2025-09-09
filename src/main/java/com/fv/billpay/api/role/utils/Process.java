package com.fv.billpay.api.role.utils;

import jakarta.ws.rs.core.Response;

public class Process {
    public static Response ok(Object data) {
        return Response.ok(new StandardResponse("success", data)).build();
    }
    public static Response error(String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new StandardResponse("error", message)).build();
    }
    public static Response notFound(String message) {
        return Response.status(Response.Status.NOT_FOUND).entity(new StandardResponse("not_found", message)).build();
    }
    public static class StandardResponse {
        public String status;
        public Object data;
        public StandardResponse(String status, Object data) {
            this.status = status;
            this.data = data;
        }
    }
}
