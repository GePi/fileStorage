package com.github.gepi.filestorage.service;

import com.github.gepi.filestorage.dto.FileInfo;
import com.github.gepi.filestorage.exception.UploadedFileIsEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class FileServiceImpl implements FileService {
    private final Path root;

    public FileServiceImpl(@Value("${fileService.rootAbsolutePath}") String rootAbsolutePath) {
        this.root = Path.of(Objects.requireNonNull(rootAbsolutePath));
        if (!Files.exists(this.root)) {
            throw new IllegalArgumentException("The root path does not exist");
        }
    }

    @Override
    public List<FileInfo> getFileList(String pathString, Boolean deepDive) {
        List<FileInfo> fileInfos = new ArrayList<>();
        Path path = root.resolve(Path.of(removeFirstSlash(pathString)));
        if (!Files.exists(path)) {
            return fileInfos;
        }
        try (Stream<Path> pathStream = Files.walk(path, deepDive ? Integer.MAX_VALUE : 0)) {
            fileInfos = pathStream.filter(Files::isRegularFile)
                    .map(path1 -> new FileInfo(path.relativize(path1), fileSizeOrException(path1)))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileInfos;
    }

    @Override
    public String upload(MultipartFile uploadedFile, String destinationFilePath, String destinationFileName) throws IOException {
        if (uploadedFile.isEmpty()) {
            throw new UploadedFileIsEmpty();
        }

        Path targetPath = root.resolve(Path.of(removeFirstSlash(destinationFilePath))).normalize().toAbsolutePath();
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }

        targetPath = targetPath.resolve(destinationFileName);

        try (var uploadedFileInputStream = uploadedFile.getInputStream()) {
            Files.copy(uploadedFileInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return targetPath.relativize(root).toString();
    }

    private String removeFirstSlash(String pathString) {
        return pathString.startsWith("/") ? pathString.substring(1) : pathString;
    }

    private long fileSizeOrException(Path path1) {
        long size;
        try {
            size = Files.size(path1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return size;
    }
}
