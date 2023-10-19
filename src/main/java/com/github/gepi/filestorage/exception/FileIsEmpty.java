package com.github.gepi.filestorage.exception;

public class FileIsEmpty extends FileServiceException {
    public FileIsEmpty() {
        super("Uploaded file is empty");
    }
}
