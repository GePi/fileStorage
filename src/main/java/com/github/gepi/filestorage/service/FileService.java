package com.github.gepi.filestorage.service;

import com.github.gepi.filestorage.dto.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileService {
    List<FileInfo> getFileList(String pathString, Boolean deepDive);

    Path save(MultipartFile multipartFile, String destinationFilePath, String destinationFileName) throws IOException;

    void delete(String path) throws IOException;

    void hardDelete(String path) throws IOException;
}
