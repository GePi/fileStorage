package com.github.gepi.filestorage.exception;

public class DirectoryIsNotEmpty extends FileServiceException {
    public DirectoryIsNotEmpty() {
        super("Directory is not empty");
    }
}
