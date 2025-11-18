package com.fv.billpay.api.role.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.jboss.logging.Logger;

/**
 * Proveedor del cliente administrativo de Keycloak usando Client Credentials Grant.
 * 
 * Esta clase gestiona la conexión con Keycloak Admin API usando un Service Account
 * con permisos completos de realm-management para gestionar roles, grupos y usuarios.
 * 
 * @author API Role Team
 * @version 2.0.0
 * @since 2025-11-18
 */
@ApplicationScoped
public class KeycloakAdminProvider {
    private static final Logger LOG = Logger.getLogger(KeycloakAdminProvider.class);
    
    private volatile Keycloak keycloak;
    private final Object lock = new Object();

    @ConfigProperty(name = "keycloak.auth-server-url")
    String serverUrl;
    
    @ConfigProperty(name = "keycloak.realm")
    String realm;
    
    @ConfigProperty(name = "keycloak.client-id")
    String clientId;
    
    @ConfigProperty(name = "keycloak.client-secret")
    String clientSecret;

    @PostConstruct
    void init() {
        initializeKeycloakClient();
    }
    
    /**
     * Inicializa el cliente de Keycloak Admin usando Client Credentials Grant.
     * Este método puede ser invocado múltiples veces para reconectar en caso de error.
     */
    private void initializeKeycloakClient() {
        synchronized (lock) {
            if (this.keycloak != null) {
                try {
                    this.keycloak.close();
                    LOG.debug("Cliente Keycloak anterior cerrado correctamente");
                } catch (Exception e) {
                    LOG.warn("Error al cerrar el cliente Keycloak anterior", e);
                }
            }
            
            try {
                this.keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
                
                LOG.infof("Cliente Keycloak inicializado correctamente [realm=%s, clientId=%s]", 
                         realm, clientId);
            } catch (Exception e) {
                LOG.errorf(e, "Error crítico al inicializar el cliente Keycloak");
                throw new IllegalStateException("No se pudo inicializar el cliente Keycloak", e);
            }
        }
    }

    /**
     * Obtiene el recurso de gestión de roles del realm.
     * Implementa retry automático en caso de error de conexión.
     * 
     * @return RolesResource para operaciones CRUD de roles
     * @throws RuntimeException si no se puede obtener el recurso después del retry
     */
    public RolesResource getRolesResource() {
        try {
            return keycloak.realm(realm).roles();
        } catch (Exception e) {
            LOG.warnf("Error al obtener RolesResource, reintentando con nueva conexión: %s", e.getMessage());
            try {
                initializeKeycloakClient();
                return keycloak.realm(realm).roles();
            } catch (Exception retryException) {
                LOG.errorf(retryException, "Error crítico al obtener RolesResource después del retry");
                throw new RuntimeException("No se pudo conectar con Keycloak Admin API", retryException);
            }
        }
    }

    /**
     * Obtiene el recurso de gestión de grupos del realm.
     * Implementa retry automático en caso de error de conexión.
     * 
     * @return GroupsResource para operaciones CRUD de grupos
     * @throws RuntimeException si no se puede obtener el recurso después del retry
     */
    public GroupsResource getGroupsResource() {
        try {
            return keycloak.realm(realm).groups();
        } catch (Exception e) {
            LOG.warnf("Error al obtener GroupsResource, reintentando con nueva conexión: %s", e.getMessage());
            try {
                initializeKeycloakClient();
                return keycloak.realm(realm).groups();
            } catch (Exception retryException) {
                LOG.errorf(retryException, "Error crítico al obtener GroupsResource después del retry");
                throw new RuntimeException("No se pudo conectar con Keycloak Admin API", retryException);
            }
        }
    }

    /**
     * Obtiene la instancia del cliente Keycloak.
     * Úsalo para acceder a otras APIs de Keycloak (grupos, usuarios, etc.)
     * 
     * @return instancia de Keycloak Admin Client
     */
    public Keycloak getKeycloak() {
        return keycloak;
    }
    
    /**
     * Cierra la conexión con Keycloak al destruir el bean.
     */
    @PreDestroy
    void cleanup() {
        if (keycloak != null) {
            try {
                keycloak.close();
                LOG.info("Cliente Keycloak cerrado correctamente");
            } catch (Exception e) {
                LOG.warn("Error al cerrar el cliente Keycloak", e);
            }
        }
    }
}
