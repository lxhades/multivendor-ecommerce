package com.auth_service.adapters.out.persistence.adapter;

import com.auth_service.adapters.out.persistence.entity.UserEntity;
import com.auth_service.adapters.out.persistence.mapper.UserMapper;
import com.auth_service.adapters.out.persistence.repository.UserJpaRepository;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository,
                                  UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId userId) {
        return userJpaRepository
                .findById(userId.value())
                .map(userMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(userMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(Email email) {
        return userJpaRepository
                .findByEmail(email.getValue())
                .map(userMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UserId userId) {
        return userJpaRepository.existsById(userId.value());
    }
    @Override
    @Transactional
    public void deleteById(UserId userId) {
        userJpaRepository.deleteById(userId.value());
    }
}
