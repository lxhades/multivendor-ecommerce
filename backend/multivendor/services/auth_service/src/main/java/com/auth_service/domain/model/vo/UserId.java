package com.auth_service.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public final class UserId {

    private final UUID value;

    private UserId(UUID value) {
        this.value = Objects.requireNonNull(value, "UserId cannot be null");
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof UserId other)) {
            return false;
        }

        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}