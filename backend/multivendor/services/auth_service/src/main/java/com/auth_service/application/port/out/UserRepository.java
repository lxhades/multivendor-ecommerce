package com.auth_service.application.port.out;

import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.enumtype.Role;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.UserId;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(Email email);

    User save(User user);

    boolean changeRole(UserId userId, Role newRole);

    Optional<Role> findRoleForUpdate(UserId userId);

    boolean existsById(UserId userId);

    boolean existsByEmail(Email email);

    List<User> findAll();

    void deleteById(UserId userId);
}
