package com.auth_service.application.usecase;

import com.auth_service.application.port.in.AdminUserResult;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.domain.model.aggregate.User;
import com.auth_service.domain.model.enumtype.Role;
import com.auth_service.domain.model.enumtype.UserStatus;
import com.auth_service.domain.model.vo.Email;
import com.auth_service.domain.model.vo.Phone;
import com.auth_service.domain.model.vo.UserId;
import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ManageAdminUserUseCase {
    private final UserRepository users;

    public ManageAdminUserUseCase(UserRepository users) { this.users = users; }

    @Transactional(readOnly = true)
    public void requireActiveAdmin(String actorId) {
        User actor = users.findById(id(actorId)).orElseThrow(() -> new DomainException(ErrorCode.ACCESS_DENIED));
        if (actor.getRole() != Role.ADMIN || actor.getStatus() != UserStatus.ACTIVE) throw new DomainException(ErrorCode.ACCESS_DENIED);
    }

    @Transactional
    public AdminUserResult update(String actorId, String targetId, String email, String phone) {
        requireActiveAdmin(actorId);
        User user = targetForUpdate(targetId);
        Email nextEmail = Email.of(email);
        users.findByEmail(nextEmail).ifPresent(existing -> {
            if (!existing.getUserId().equals(user.getUserId())) throw new DomainException(ErrorCode.EMAIL_ALREADY_EXISTS);
        });
        user.updateContact(nextEmail, Phone.of(phone));
        return result(users.save(user));
    }

    @Transactional
    public AdminUserResult changeStatus(String actorId, String targetId, UserStatus status) {
        requireActiveAdmin(actorId);
        if (status == null) throw new IllegalArgumentException("Trạng thái là bắt buộc");
        if (id(actorId).equals(id(targetId)) && status == UserStatus.LOCKED) throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể tự cấm tài khoản đang đăng nhập");
        User user = targetForUpdate(targetId);
        if (user.getStatus() != status) {
            if (status == UserStatus.LOCKED) user.lock(); else user.activate();
            user = users.save(user);
        }
        return result(user);
    }

    @Transactional
    public void delete(String actorId, String targetId) {
        requireActiveAdmin(actorId);
        if (id(actorId).equals(id(targetId))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể tự xóa tài khoản đang đăng nhập");
        User target = targetForUpdate(targetId);
        users.deleteById(target.getUserId());
    }

    private User targetForUpdate(String value) {
        UserId userId = id(value);
        users.findRoleForUpdate(userId).orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));
        return users.findById(userId).orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));
    }
    private UserId id(String value) { return UserId.of(UUID.fromString(value)); }
    private AdminUserResult result(User user) {
        return new AdminUserResult(user.getUserId().toString(), user.getEmail().getValue(), user.getPhone().value(), user.getStatus(), user.getRole());
    }
}
