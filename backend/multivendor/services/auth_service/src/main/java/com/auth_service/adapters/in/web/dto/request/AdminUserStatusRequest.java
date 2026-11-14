package com.auth_service.adapters.in.web.dto.request;

import com.auth_service.domain.model.enumtype.UserStatus;
import jakarta.validation.constraints.NotNull;

public record AdminUserStatusRequest(@NotNull UserStatus status) {}
