package com.auth_service.application.port.in;

import java.util.List;

public record AdminUserListResult(
        List<AdminUserResult> users,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
