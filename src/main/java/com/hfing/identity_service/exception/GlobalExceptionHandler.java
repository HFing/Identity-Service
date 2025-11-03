package com.hfing.identity_service.exception;

import com.hfing.identity_service.dto.request.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException e) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UncaughtException.getCode());
        apiResponse.setMessage(ErrorCode.UncaughtException.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException e) {
        ErrorCode errorCode = e.getCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException e) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String enumKey = e.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVAID_KEY;

        Map<String, Object> attributes =null;
        try {
            errorCode = ErrorCode.valueOf(enumKey);

            var constraintViolation = e.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            attributes =  constraintViolation.getConstraintDescriptor().getAttributes();
            log.info( attributes.toString());
        } catch (IllegalArgumentException ex) {
            
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(Objects.nonNull(attributes) ?
                mapAttributeMessage(errorCode.getMessage(), attributes)
                : errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttributeMessage(String message, Map<String, Object> attributes) {

        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE)) ;
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }


}
