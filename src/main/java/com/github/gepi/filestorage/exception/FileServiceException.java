package com.github.gepi.filestorage.exception;

public class FileServiceException extends RuntimeException {
    public FileServiceException(Exception exception) {
        super(exception);
    }
    public FileServiceException(String message) {
        super(message);
    }
}
