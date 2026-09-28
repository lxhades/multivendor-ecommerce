package com.auth_service.adapters.out.persistence.mapper;

import com.auth_service.adapters.out.persistence.entity.AddressEntity;
import com.auth_service.adapters.out.persistence.entity.UserEntity;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.PasswordHash;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.vo.UserId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;

@Component
public class UserMapper {

    private final AddressMapper addressMapper;

    public UserMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(user.getUserId().value());
        entity.setEmail(user.getEmail().getValue());
        entity.setPhone(user.getPhone().value());
        entity.setPasswordHash(user.getPasswordHash().value());
        entity.setStatus(user.getStatus());
        entity.setRoles(new HashSet<>(user.getRoles()));
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());

        var addressEntities = new ArrayList<AddressEntity>();
        for (var address : user.getAddresses()) {
            AddressEntity addressEntity = addressMapper.toEntity(address);
            addressEntity.setUser(entity);
            addressEntities.add(addressEntity);
        }
        entity.setAddresses(addressEntities);

        return entity;
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return User.reconstitute(
                UserId.of(entity.getId()),
                Email.of(entity.getEmail()),
                new Phone(entity.getPhone()),
                new PasswordHash(entity.getPasswordHash()),
                entity.getStatus(),
                new HashSet<>(entity.getRoles()),
                entity.getAddresses().stream()
                        .map(addressMapper::toDomain)
                        .toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
