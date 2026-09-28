package com.auth_service.adapters.out.persistence.mapper;

import com.auth_service.adapters.out.persistence.entity.AddressEntity;
import com.auth_service.domain.model.entity.Address;
import com.auth_service.domain.model.vo.Phone;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressEntity toEntity(Address address) {
        if (address == null) {
            return null;
        }

        AddressEntity entity = new AddressEntity();
        entity.setId(address.getAddressId());
        entity.setReceiverName(address.getReceiverName());
        entity.setPhone(address.getPhone().value());
        entity.setLocation(address.getLocation());
        entity.setDetail(address.getDetail());
        entity.setDefault(address.isDefault());
        return entity;
    }

    public Address toDomain(AddressEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Address(
                entity.getId(),
                entity.getReceiverName(),
                new Phone(entity.getPhone()),
                entity.getLocation(),
                entity.getDetail(),
                entity.isDefault()
        );
    }
}
