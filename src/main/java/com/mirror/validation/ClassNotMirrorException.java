package com.mirror.validation;

public class ClassNotMirrorException extends RuntimeException {

    public ClassNotMirrorException(Class<?> cls) {
        super(cls.getName());
    }
}
