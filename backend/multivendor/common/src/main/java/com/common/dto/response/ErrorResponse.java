package com.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final List<String> details;
    private final String path;

    public ErrorResponse(Instant now,int status, String error, String message, List<String> details, String path) {
        this.timestamp = now;
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
        this.path = path;
    }
}