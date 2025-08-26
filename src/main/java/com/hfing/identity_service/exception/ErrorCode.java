package com.hfing.identity_service.exception;

public enum ErrorCode {
    INVAID_KEY(1001, "Invalid Key"),
    UncaughtException(9999, "An unexpected error occurred"),
    USER_EXISTS(1002, "User already exists"),
    USERNAME_INVALID(1003, "Username Must be between 4 and 50 characters"),
    INVAID_PASSWORD(1004, "Password must be between 8 and 20 characters"),
    USER_NOT_FOUND(1005, "User not found"),
    UNAUTHENTICATED(1006, "Unauthenticated"),
    ;

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
