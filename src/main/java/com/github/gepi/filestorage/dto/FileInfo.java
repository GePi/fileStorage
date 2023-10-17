package com.github.gepi.filestorage.dto;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class FileInfo {
    private final String fullFileName;
    private final String fileName;
    private final long size;

    public FileInfo(String fullFileName, String fileName, long size) {
        this.fullFileName = fullFileName.replace("\\", "/");
        this.fileName = fileName;
        this.size = size;
    }

    public FileInfo(Path path, long size) {
        this(path.toString(), path.getFileName().toString(), size);
    }

}
