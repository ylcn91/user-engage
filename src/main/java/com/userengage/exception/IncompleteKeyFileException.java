package com.userengage.exception;

import lombok.Getter;

@Getter
public class IncompleteKeyFileException extends RuntimeException {

    private final String keyLocation;
    private final int bytesRead;
    private final int expectedBytes;

    public IncompleteKeyFileException(String keyLocation, int bytesRead, int expectedBytes) {
        super(String.format("Failed to read the entire key file: %d bytes read, expected %d, at location: %s",
                bytesRead, expectedBytes, keyLocation));
        this.keyLocation = keyLocation;
        this.bytesRead = bytesRead;
        this.expectedBytes = expectedBytes;
    }
}
