package com.auth_service.domain.model.aggregate;

import com.auth_service.domain.model.enumtype.Role;
import com.auth_service.domain.model.entity.Address;
import com.auth_service.domain.model.enumtype.UserStatus;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.PasswordHash;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.vo.UserId;
import com.auth_service.domain.exception.AddressNotFoundException;


import java.security.AuthProvider;
import java.time.Instant;
import java.util.*;

public class User {
    private final UserId userId;
    private Email email;
    private Phone phone;
    private PasswordHash passwordHash;
    private UserStatus status;
    private final Set<Role> roles;
    private final List<Address> addresses;
    private Instant createdAt;
    private Instant updatedAt;

    private User(UserId userId, Email email, Phone phone, PasswordHash passwordHash) {
        this.userId = Objects.requireNonNull(userId, "User id must not be null");
        this.email = Objects.requireNonNull(email, "Email must not be null");
        this.phone = Objects.requireNonNull(phone, "Phone must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash must not be null");
        this.status = UserStatus.ACTIVE;
        this.roles = new HashSet<>();
        this.roles.add(Role.BUYER);

        this.addresses = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }


    public static User register(Email email, Phone phone, PasswordHash passwordHash) {
        return new User(UserId.generate(), email, phone, passwordHash);
    }

    public static User reconstitute(UserId userId,
                                    Email email,
                                    Phone phone,
                                    PasswordHash passwordHash,
                                    UserStatus status,
                                    Set<Role> roles,
                                    List<Address> addresses,
                                    Instant createdAt,
                                    Instant updatedAt) {

        User user = new User(userId, email, phone, passwordHash);

        user.status = Objects.requireNonNull(status, "Status must not be null");

        user.roles.clear();
        user.roles.addAll(Objects.requireNonNull(roles, "Roles must not be null"));
        if (user.roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }

        user.addresses.clear();
        user.addresses.addAll(Objects.requireNonNull(addresses, "Addresses must not be null"));

        user.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null");
        user.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt must not be null");

        return user;
    }
    public void changePassword(PasswordHash newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash, "Password hash must not be null");
        this.updatedAt = Instant.now();
    }


    public void addAddress(Address address) {
        Objects.requireNonNull(address, "Address must not be null");
        if (address.isDefault()) {
            this.addresses.forEach(item -> item.setDefault(false));
        }
        this.addresses.add(address);
        this.updatedAt = Instant.now();
    }

    public void updateAddress(Long addressId, String receiverName, Phone phone, String location, String detail, boolean isDefault) {
        Objects.requireNonNull(addressId, "Address id must not be null");

        Address address = this.addresses.stream()
                .filter(item -> addressId.equals(item.getAddressId()))
                .findFirst()
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        if (isDefault) {
            this.addresses.forEach(item -> item.setDefault(false));
        }
        address.update(receiverName, phone, location, detail);
        address.setDefault(isDefault);
        this.updatedAt = Instant.now();
    }
    public void deleteAddress(Long addressId){
        Objects.requireNonNull(addressId, "Address id must not be null");
        Address address = this.addresses.stream()
                .filter(item -> addressId.equals(item.getAddressId()))
                .findFirst()
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        this.addresses.remove(address);
        this.updatedAt = Instant.now();
    }

    public void assignRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null");
        this.roles.add(role);
        this.updatedAt = Instant.now();
    }

    public void revokeRole(Role role) {
        if (this.roles.size() == 1 && this.roles.contains(role)) {
            throw new IllegalStateException("User must have at least one role");
        }
        this.roles.remove(role);
        this.updatedAt = Instant.now();
    }

    public void lock() {
        if (this.status == UserStatus.LOCKED) {
            throw new IllegalStateException("User is already locked");
        }
        this.status = UserStatus.LOCKED;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already active");
        }
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public UserId getUserId() { return userId; }
    public Email getEmail() { return email; }
    public Phone getPhone() { return phone; }
    public PasswordHash getPasswordHash() { return passwordHash; }
    public UserStatus getStatus() { return status; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
    public List<Address> getAddresses() { return Collections.unmodifiableList(addresses); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", email=" + email +
                ", phone=" + phone +
                ", passwordHash=" + passwordHash +
                ", status=" + status +
                ", roles=" + roles +
                ", addresses=" + addresses +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
