package com.fv.billpay.api.role.exception;

import com.fv.billpay.api.role.utils.Process;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        return Process.error(exception.getMessage());
    }
}
