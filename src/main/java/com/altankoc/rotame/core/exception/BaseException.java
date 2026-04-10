package com.altankoc.rotame.core.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

    protected BaseException(String message) {
        super(message);
    }
}