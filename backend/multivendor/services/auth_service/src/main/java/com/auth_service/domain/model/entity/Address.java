package com.auth_service.domain.model.entity;

import com.auth_service.domain.model.vo.Phone;

import java.util.Objects;

public class Address {
    private final Long addressId;

    private String receiverName;
    private Phone phone;
    private String location;
    private String detail;
    private boolean isDefault;

    public Address(
            Long addressId,
            String receiverName,
            Phone phone,
            String location,
            String detail,
            boolean isDefault
    ) {
        if (receiverName == null || receiverName.isBlank()) {
            throw new IllegalArgumentException(
                    "Receiver name must not be blank"
            );
        }

        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "Location must not be blank"
            );
        }

        if (detail == null || detail.isBlank()) {
            throw new IllegalArgumentException(
                    "Address detail must not be blank"
            );
        }

        this.addressId = addressId;
        this.receiverName = receiverName;
        this.phone = Objects.requireNonNull(phone, "Phone must not be null");
        this.location = location;
        this.detail = detail;
        this.isDefault = isDefault;
    }

    public void update(
            String receiverName,
            Phone phone,
            String location,
            String detail
    ) {
        if (receiverName == null || receiverName.isBlank()) {
            throw new IllegalArgumentException(
                    "Receiver name must not be blank"
            );
        }

        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "Location must not be blank"
            );
        }

        if (detail == null || detail.isBlank()) {
            throw new IllegalArgumentException(
                    "Address detail must not be blank"
            );
        }

        this.receiverName = receiverName;
        this.phone = Objects.requireNonNull(phone, "Phone must not be null");
        this.location = location;
        this.detail = detail;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Long getAddressId() {
        return addressId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public Phone getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public String getDetail() {
        return detail;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address other)) return false;
        // Address là Entity trong DDD — so sánh theo định danh (addressId), không theo giá trị field.
        return addressId != null && addressId.equals(other.addressId);
    }

    @Override
    public int hashCode() {
        return addressId != null ? addressId.hashCode() : 0;
    }
}
