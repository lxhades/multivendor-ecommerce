package com.auth_service.application.usecase;

import com.auth_service.application.port.in.AdminUserListResult;
import com.auth_service.application.port.in.AdminUserResult;
import com.auth_service.application.port.out.UserRepository;
import com.auth_service.application.query.SearchUsersQuery;
import com.auth_service.domain.model.aggregate.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListUsersUseCase {
    private final UserRepository userRepository;

    public ListUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AdminUserListResult execute(SearchUsersQuery query) {
        List<User> all = userRepository.findAll();
        String keyword = query.keyword() == null ? "" : query.keyword().trim().toLowerCase();

        List<User> filtered = all.stream()
                .filter(user -> keyword.isBlank()
                        || user.getEmail().getValue().toLowerCase().contains(keyword)
                        || user.getPhone().value().contains(keyword))
                .toList();

        int from = Math.min(query.page() * query.size(), filtered.size());
        int to = Math.min(from + query.size(), filtered.size());
        List<AdminUserResult> page = filtered.subList(from, to).stream()
                .map(this::toResult)
                .toList();

        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / query.size());
        return new AdminUserListResult(page, query.page(), query.size(), filtered.size(), totalPages);
    }
    private AdminUserResult toResult(User user) {
        if(user == null) {
            return null;
        }
        AdminUserResult adminUserResult = new AdminUserResult(
                user.getUserId().toString(),
                user.getEmail().getValue(),
                user.getPhone().value(),
                user.getStatus(),
                user.getRoles().stream()
                        .map(Enum::name)
                        .toList()

        );
        return adminUserResult;
    }
}
