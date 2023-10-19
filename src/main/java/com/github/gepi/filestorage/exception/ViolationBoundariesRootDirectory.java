package com.github.gepi.filestorage.exception;

public class ViolationBoundariesRootDirectory extends FileServiceException {
    public ViolationBoundariesRootDirectory() {
        super("Cannot store file outside root directory.");
    }
}
