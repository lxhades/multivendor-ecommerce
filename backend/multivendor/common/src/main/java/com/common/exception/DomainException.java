package com.common.exception;

import lombok.Getter;

import java.util.Objects;

@Getter
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode) {
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
    }

    @Override
    public String getMessage() {
        return errorCode.name();
    }
}
