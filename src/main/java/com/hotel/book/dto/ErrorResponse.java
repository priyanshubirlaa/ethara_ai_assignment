package com.hotel.book.dto;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 * Standard error response returned by the API for all failures.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Time when the error occurred (UTC).
     */
    private Instant timestamp;

    /**
     * HTTP status code (e.g. 400, 404, 500).
     */
    private int status;

    /**
     * Short, human-readable error type (e.g. "Bad Request", "Not Found").
     */
    private String error;

    /**
     * More detailed error message.
     */
    private String message;

    /**
     * Request path that triggered the error.
     */
    private String path;

    /**
     * Optional map of field-level validation errors.
     * Only included when validation errors occur.
     */
    private Map<String, String> errors;
}

