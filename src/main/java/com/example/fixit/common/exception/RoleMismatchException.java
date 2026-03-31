package com.example.fixit.common.exception;

import com.example.fixit.module.user.entity.Role;

public class RoleMismatchException extends RuntimeException {

    public RoleMismatchException(Long userId, Role expected, Role actual) {
        super("Role mismatch for user " + userId + ": header says " + expected + " but stored role is " + actual);
    }
}
