package com.lezh1n.goodminton_shop_api.exceptions;

import java.sql.SQLException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<Void>> handlingRuntimeException(RuntimeException exception) {

        // Check if it's a wrapped database exception
        Throwable cause = exception.getCause();
        if (cause instanceof DataIntegrityViolationException) {
            return handlingDataIntegrityViolationException((DataIntegrityViolationException) cause);
        }

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.<Void>builder()
                        .code(ErrorCode.SYSTEM_INTERNAL_ERROR.getCode())
                        .message(ErrorCode.SYSTEM_INTERNAL_ERROR.getMessage())
                        .build());
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handlingAuthorizationDeniedException(AuthorizationDeniedException exception) {
        ErrorCode errorCode = ErrorCode.AUTH_UNAUTHORIZED;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleJsonParseException(HttpMessageNotReadableException exception) {
        ErrorCode errorCode = ErrorCode.ENUM_INVALID_VALUE;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingValidation(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = ErrorCode.SYSTEM_UNKNOWN_ERROR;

        FieldError fieldError = exception.getFieldError();
        if (fieldError != null) {
            String enumKey = fieldError.getDefaultMessage();
            if (enumKey != null) {
                try {
                    errorCode = ErrorCode.valueOf(enumKey);
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown error code: {}", enumKey);
                    errorCode = ErrorCode.SYSTEM_UNKNOWN_ERROR;
                }
            }
        }
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class,
            InternalAuthenticationServiceException.class })
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(Exception ex) {
        return ResponseEntity.status(ErrorCode.AUTH_INVALID_CREDENTIALS.getStatus())
                .body(ApiResponse.<Void>builder()
                        .code(ErrorCode.AUTH_INVALID_CREDENTIALS.getCode())
                        .message(ErrorCode.AUTH_INVALID_CREDENTIALS.getMessage())
                        .build());
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<Void>> handlingDataIntegrityViolationException(
            DataIntegrityViolationException exception) {
        log.error("Database constraint violation: ", exception);

        String message = exception.getMessage();
        ErrorCode errorCode = ErrorCode.DATABASE_CONSTRAINT_VIOLATION;

        // Parse specific constraint violations
        if (message != null) {
            if (message.contains("uq_variant")) {
                errorCode = ErrorCode.VARIANT_DUPLICATE_COMBINATION;
            } else if (message.contains("variant_image_public_id_key")) {
                errorCode = ErrorCode.VARIANT_IMAGE_PUBLIC_ID_DUPLICATE;
            } else if (message.contains("fk_inventory_variant") || message.contains("fk_items_variant")) {
                errorCode = ErrorCode.VARIANT_IN_USE;
            } else if (message.contains("duplicate key")) {
                errorCode = ErrorCode.DATABASE_DUPLICATE_KEY;
            } else if (message.contains("foreign key")) {
                errorCode = ErrorCode.DATABASE_FOREIGN_KEY_VIOLATION;
            } else if (message.contains("unique constraint")) {
                errorCode = ErrorCode.DATABASE_UNIQUE_CONSTRAINT_VIOLATION;
            }
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = SQLException.class)
    ResponseEntity<ApiResponse<Void>> handlingSQLException(SQLException exception) {
        log.error("SQL Exception: ", exception);

        ErrorCode errorCode = ErrorCode.DATABASE_CONSTRAINT_VIOLATION;

        // Handle specific SQL error codes
        String sqlState = exception.getSQLState();
        if ("23505".equals(sqlState)) { // Unique constraint violation
            errorCode = ErrorCode.DATABASE_UNIQUE_CONSTRAINT_VIOLATION;
        } else if ("23503".equals(sqlState)) { // Foreign key constraint violation
            errorCode = ErrorCode.DATABASE_FOREIGN_KEY_VIOLATION;
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = JpaSystemException.class)
    ResponseEntity<ApiResponse<Void>> handlingJpaSystemException(JpaSystemException exception) {
        log.error("JPA System Exception: ", exception);

        // Check if it's caused by a constraint violation
        Throwable cause = exception.getCause();
        if (cause instanceof DataIntegrityViolationException) {
            return handlingDataIntegrityViolationException((DataIntegrityViolationException) cause);
        }

        return ResponseEntity
                .status(ErrorCode.SYSTEM_INTERNAL_ERROR.getStatus())
                .body(ApiResponse.<Void>builder()
                        .code(ErrorCode.SYSTEM_INTERNAL_ERROR.getCode())
                        .message(ErrorCode.SYSTEM_INTERNAL_ERROR.getMessage())
                        .build());
    }
}
