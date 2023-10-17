package com.github.gepi.filestorage.service;

import com.github.gepi.filestorage.dto.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    List<FileInfo> getFileList(String pathString, Boolean deepDive);

    String upload(MultipartFile multipartFile, String destinationFilePath, String destinationFileName) throws IOException;
}
