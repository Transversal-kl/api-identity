package com.fv.billpay.api.role.utils;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utilidad para validación de UUIDs.
 * Proporciona métodos para validar formato UUID antes de parsear.
 */
public final class UuidValidator {
    
    // Patrón regex para UUID versión 4 (estándar)
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );
    
    // Constructor privado para prevenir instanciación
    private UuidValidator() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }
    
    /**
     * Valida si una cadena tiene formato UUID válido.
     * 
     * @param uuid String a validar
     * @return true si tiene formato UUID válido, false en caso contrario
     */
    public static boolean isValidUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return false;
        }
        return UUID_PATTERN.matcher(uuid.trim()).matches();
    }
    
    /**
     * Valida y parsea un UUID de forma segura.
     * 
     * @param uuid String a parsear
     * @return UUID parseado
     * @throws IllegalArgumentException si el formato es inválido
     */
    public static UUID parseUuid(String uuid) {
        if (!isValidUuid(uuid)) {
            throw new IllegalArgumentException(
                String.format("Formato de UUID inválido: '%s'. Formato esperado: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", 
                    uuid != null ? uuid : "null")
            );
        }
        return UUID.fromString(uuid.trim());
    }
    
    /**
     * Intenta parsear un UUID de forma segura sin lanzar excepción.
     * 
     * @param uuid String a parsear
     * @return UUID parseado o null si el formato es inválido
     */
    public static UUID tryParseUuid(String uuid) {
        try {
            return isValidUuid(uuid) ? UUID.fromString(uuid.trim()) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
