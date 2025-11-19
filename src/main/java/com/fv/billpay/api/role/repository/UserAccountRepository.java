package com.fv.billpay.api.role.repository;

import com.fv.billpay.api.role.entity.UserAccount;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio reactivo para gestión de usuarios en PostgreSQL.
 */
@ApplicationScoped
public class UserAccountRepository implements PanacheRepositoryBase<UserAccount, UUID> {

    /**
     * Busca un usuario por su username.
     */
    public Uni<UserAccount> findByUsername(String username) {
        return find("username", username).firstResult();
    }

    /**
     * Busca un usuario por su email.
     */
    public Uni<UserAccount> findByEmail(String email) {
        return find("email", email).firstResult();
    }

    /**
     * Obtiene todos los usuarios paginados.
     */
    public Uni<List<UserAccount>> findAllPaginated(int page, int size) {
        return findAll().page(Page.of(page, size)).list();
    }

    /**
     * Cuenta el total de usuarios.
     */
    public Uni<Long> countAll() {
        return count();
    }

    /**
     * Actualiza solo la imagen de perfil de un usuario.
     */
    public Uni<Integer> updateProfileImage(UUID userId, byte[] profileImage) {
        return update("profileImage = ?1 where id = ?2", profileImage, userId);
    }
}
