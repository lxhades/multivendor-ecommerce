package com.auth_service.adapters.out.persistence.repository;

import com.auth_service.adapters.out.persistence.entity.UserEntity;
import com.auth_service.domain.model.enumtype.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = "select role from users where id = :id for update", nativeQuery = true)
    Optional<String> findRoleForUpdate(@Param("id") UUID id);

    @Modifying
    @Query("""
            update UserEntity u set u.role = :newRole, u.updatedAt = :now
            where u.id = :id
            """)
    int changeRole(@Param("id") UUID id, @Param("newRole") Role newRole, @Param("now") Instant now);
}
