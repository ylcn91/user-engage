package com.userengage.exception;

import lombok.Getter;

@Getter
public class KeyFileNotFoundException extends RuntimeException {

    private final String keyLocation;

    public KeyFileNotFoundException(String keyLocation) {
        super(String.format("Key file not found at location: %s", keyLocation));
        this.keyLocation = keyLocation;
    }
}
