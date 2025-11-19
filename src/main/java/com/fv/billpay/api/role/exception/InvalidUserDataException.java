package com.fv.billpay.api.role.exception;

/**
 * Excepción lanzada cuando los datos del usuario son inválidos.
 */
public class InvalidUserDataException extends RuntimeException {
    
    public InvalidUserDataException(String message) {
        super("Datos de usuario inválidos: " + message);
    }
    
    public InvalidUserDataException(String message, Throwable cause) {
        super("Datos de usuario inválidos: " + message, cause);
    }
}
