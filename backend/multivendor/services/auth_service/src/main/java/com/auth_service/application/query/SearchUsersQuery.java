package com.auth_service.application.query;

public record SearchUsersQuery(
        String keyword,
        int page,
        int size
) {
    public SearchUsersQuery {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        if ((long) page * size > Integer.MAX_VALUE) throw new IllegalArgumentException("Trang vượt giới hạn hỗ trợ");
    }
}
