package com.auth_service.domain.model.aggregate;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.auth_service.domain.model.entity.Address;
import com.auth_service.domain.model.enumtype.Role;
import com.auth_service.domain.model.enumtype.UserStatus;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.PasswordHash;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.vo.UserId;

import java.time.Instant;
import java.util.*;

public class User {
    private final UserId userId;
    private Email email;
    private Phone phone;
    private PasswordHash passwordHash;
    private UserStatus status;
    private Role role;
    private final List<Address> addresses;
    private Instant createdAt;
    private Instant updatedAt;

    private User(UserId userId, Email email, Phone phone, PasswordHash passwordHash) {
        this.userId = Objects.requireNonNull(userId, "Mã người dùng không được để trống");
        this.email = Objects.requireNonNull(email, "Email không được để trống");
        this.phone = Objects.requireNonNull(phone, "Số điện thoại không được để trống");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Mã băm mật khẩu không được để trống");
        this.status = UserStatus.ACTIVE;
        this.role = Role.BUYER;

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
                                    Role role,
                                    List<Address> addresses,
                                    Instant createdAt,
                                    Instant updatedAt) {

        User user = new User(userId, email, phone, passwordHash);

        user.status = Objects.requireNonNull(status, "Trạng thái không được để trống");

        user.role = Objects.requireNonNull(role, "Vai trò không được để trống");

        user.addresses.clear();
        user.addresses.addAll(Objects.requireNonNull(addresses, "Danh sách địa chỉ không được để trống"));

        user.createdAt = Objects.requireNonNull(createdAt, "Thời điểm tạo không được để trống");
        user.updatedAt = Objects.requireNonNull(updatedAt, "Thời điểm cập nhật không được để trống");

        return user;
    }

    public void changePassword(PasswordHash newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash, "Mã băm mật khẩu không được để trống");
        this.updatedAt = Instant.now();
    }

    public void addAddress(Address address) {
        Objects.requireNonNull(address, "Địa chỉ không được để trống");
        if (address.isDefault()) {
            this.addresses.forEach(item -> item.setDefault(false));
        }
        this.addresses.add(address);
        this.updatedAt = Instant.now();
    }

    public void updateAddress(Long addressId, String receiverName, Phone phone, String location, String detail, boolean isDefault) {
        Objects.requireNonNull(addressId, "Mã địa chỉ không được để trống");

        Address address = this.addresses.stream()
                .filter(item -> addressId.equals(item.getAddressId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.ADDRESS_NOT_FOUND));

        if (isDefault) {
            this.addresses.forEach(item -> item.setDefault(false));
        }
        address.update(receiverName, phone, location, detail);
        address.setDefault(isDefault);
        this.updatedAt = Instant.now();
    }

    public void deleteAddress(Long addressId) {
        Objects.requireNonNull(addressId, "Mã địa chỉ không được để trống");
        Address address = this.addresses.stream()
                .filter(item -> addressId.equals(item.getAddressId()))
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.ADDRESS_NOT_FOUND));
        this.addresses.remove(address);
        this.updatedAt = Instant.now();
    }

    public void lock() {
        if (this.status == UserStatus.LOCKED) {
            throw new IllegalStateException("Tài khoản đã bị khóa");
        }
        this.status = UserStatus.LOCKED;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("Tài khoản đã ở trạng thái hoạt động");
        }
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public UserId getUserId() {
        return userId;
    }

    public Email getEmail() {
        return email;
    }

    public Phone getPhone() {
        return phone;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Role getRole() {
        return role;
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }


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
                ", role=" + role +
                ", addresses=" + addresses +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
