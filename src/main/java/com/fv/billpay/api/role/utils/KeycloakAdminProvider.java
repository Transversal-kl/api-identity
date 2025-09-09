package com.fv.billpay.api.role.utils;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RolesResource;

@ApplicationScoped
public class KeycloakAdminProvider {
    private Keycloak keycloak;

    @ConfigProperty(name = "keycloak.auth-server-url")
    String serverUrl;
    @ConfigProperty(name = "keycloak.realm")
    String realm;
    @ConfigProperty(name = "keycloak.resource")
    String clientId;
    @ConfigProperty(name = "keycloak.credentials.secret", defaultValue = "")
    String clientSecret;
    @ConfigProperty(name = "keycloak.admin.username")
    String adminUsername;
    @ConfigProperty(name = "keycloak.admin.password")
    String adminPassword;

    @PostConstruct
    void init() {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(clientId)
                .clientSecret(clientSecret.isEmpty() ? null : clientSecret)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    public RolesResource getRolesResource() {
        return keycloak.realm(realm).roles();
    }

    public Keycloak getKeycloak() {
        return keycloak;
    }
}
