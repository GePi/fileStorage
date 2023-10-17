package com.github.gepi.filestorage.exception;

public class UploadedFileIsEmpty extends FileServiceException {
    public UploadedFileIsEmpty() {
        super("Uploaded file is empty");
    }
}
